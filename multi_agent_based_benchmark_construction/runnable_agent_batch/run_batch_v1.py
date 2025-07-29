# file path: runnable_agent_batch/run_batch_v1.py

"""
多进程批量运行脚本 - 专门处理Matlab项目
将1885个Matlab项目分成10个进程并行处理
"""

import argparse
import logging
import os
import sys
import time
import traceback
from pathlib import Path
from datetime import datetime
from typing import List, Dict, Optional, Tuple
import json
import tiktoken
import multiprocessing
from multiprocessing import Pool, Manager, Process
from runnable_agent_batch.run import TestDetectionPipeline


def setup_logging(process_id: int = 0):
    """为每个进程设置独立的日志"""
    log_format = f'%(asctime)s - Process-{process_id} - %(levelname)s - %(name)s - %(message)s'
    
    # 清除现有的处理器
    logger = logging.getLogger()
    for handler in logger.handlers[:]:
        logger.removeHandler(handler)
    
    # 设置新的处理器
    logging.basicConfig(
        format=log_format,
        datefmt='%m/%d/%Y %H:%M:%S',
        level=logging.INFO,
        handlers=[
            logging.StreamHandler(sys.stdout),
            logging.FileHandler(f'multiprocess_batch_debug_p{process_id}.log')
        ]
    )
    return logging.getLogger(__name__)


def run_single_project(args_tuple: Tuple) -> Dict:
    """运行单个项目的测试检测管道 - 用于多进程"""
    project_path, language, model_name, max_iterations, timeout_per_command, verbose, process_id, project_index, total_projects = args_tuple
    
    # 为每个进程设置独立的日志
    logger = setup_logging(process_id)
    
    project_name = project_path.name
    start_time = datetime.now()
    
    logger.info(f"🔍 [{project_index}/{total_projects}] Starting test detection for: {project_name}")
    
    result = {
        'process_id': process_id,
        'project_index': project_index,
        'project_name': project_name,
        'language': language,
        'project_path': str(project_path),
        'start_time': start_time.isoformat(),
        'status': 'unknown',
        'error_message': None,
        'iterations_completed': 0,
        'execution_time_seconds': 0,
        'copied_to_runnable': False
    }
    
    try:
        # 创建TestDetectionPipeline的参数
        pipeline_args = argparse.Namespace(
            model_name=model_name,
            repo_path=str(project_path),
            repo_language=language,
            max_iterations=max_iterations,
            timeout_per_command=timeout_per_command,
            verbose=verbose
        )
        
        # 初始化并运行管道
        pipeline = TestDetectionPipeline(pipeline_args, tiktoken.encoding_for_model("gpt-4"))
        final_status = pipeline.run()
        
        # 记录结果
        result['iterations_completed'] = pipeline.current_iteration
        result['status'] = final_status or 'failed'
        
        if final_status == 'success':
            logger.info(f"✅ [{project_index}/{total_projects}] Successfully processed: {project_name}")
            result['copied_to_runnable'] = True
        elif final_status == 'failed':
            logger.info(f"❌ [{project_index}/{total_projects}] Failed: {project_name}")
        elif final_status == 'interrupted':
            logger.info(f"⏸️ [{project_index}/{total_projects}] Interrupted: {project_name}")
        else:
            logger.warning(f"❓ [{project_index}/{total_projects}] Unknown status: {project_name}")

    except KeyboardInterrupt:
        logger.info(f"❌ [{project_index}/{total_projects}] Interrupted by user: {project_name}")
        result['error_message'] = "Interrupted by user"
        result['status'] = 'interrupted'
        
    except Exception as e:
        error_msg = f"Failed to process project {project_name}: {str(e)}"
        logger.error(f"❌ [{project_index}/{total_projects}] {error_msg}")
        logger.error(f"Full traceback: {traceback.format_exc()}")
        result['error_message'] = str(e)
        result['status'] = 'failed'
    
    finally:
        end_time = datetime.now()
        result['end_time'] = end_time.isoformat()
        result['execution_time_seconds'] = (end_time - start_time).total_seconds()
        
        logger.info(f"📋 [{project_index}/{total_projects}] Completed {project_name}: {result['status']} ({result['execution_time_seconds']:.1f}s)")
    
    return result


class MultiProcessBatchRunner:
    def __init__(self, args):
        self.args = args
        self.language = "Matlab"  # 固定为Matlab
        self.num_processes = args.num_processes
        
        # 使用与原脚本相同的目录结构
        self.primary_filter_base = Path("primary_filter_v3_1").resolve()
        self.runnable_filter_base = Path("runnable_filter_1").resolve()
        
        # 验证目录存在
        if not self.primary_filter_base.exists():
            raise FileNotFoundError(f"Primary filter directory does not exist: {self.primary_filter_base}")
        
        # 创建runnable_filter目录
        self.runnable_filter_base.mkdir(parents=True, exist_ok=True)
        
        # 设置批处理输出目录
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        self.batch_output_dir = Path("multiprocess_batch_logs") / f"matlab_batch_{timestamp}"
        self.batch_output_dir.mkdir(parents=True, exist_ok=True)
        
        # 初始化批处理摘要
        self.batch_summary = {
            'start_time': datetime.now().isoformat(),
            'language': self.language,
            'num_processes': self.num_processes,
            'total_projects': 0,
            'successful_projects': 0,
            'failed_projects': 0,
            'interrupted_projects': 0,
            'results': []
        }
        
        # 设置日志
        self.logger = setup_logging(0)

    def discover_matlab_projects(self) -> List[Path]:
        """发现所有Matlab项目"""
        matlab_dir = self.primary_filter_base / self.language
        
        if not matlab_dir.exists():
            raise FileNotFoundError(f"Matlab directory does not exist: {matlab_dir}")
        
        projects = []
        try:
            for item in matlab_dir.iterdir():
                if item.is_dir() and not item.name.startswith('.'):
                    projects.append(item)
            
            # 按名称排序以确保一致性
            projects.sort(key=lambda x: x.name.lower())
            
            self.logger.info(f"Found {len(projects)} Matlab projects")
            return projects
            
        except Exception as e:
            self.logger.error(f"Error discovering Matlab projects: {e}")
            return []

    def split_projects_into_chunks(self, projects: List[Path]) -> List[List[Tuple]]:
        """将项目分成多个进程块"""
        total_projects = len(projects)
        chunk_size = (total_projects + self.num_processes - 1) // self.num_processes
        
        chunks = []
        for i in range(self.num_processes):
            start_idx = i * chunk_size
            end_idx = min(start_idx + chunk_size, total_projects)
            
            if start_idx >= total_projects:
                break
                
            chunk_projects = projects[start_idx:end_idx]
            
            # 为每个项目创建参数元组
            chunk_args = []
            for j, project_path in enumerate(chunk_projects):
                project_index = start_idx + j + 1  # 全局索引
                args_tuple = (
                    project_path,
                    self.language,
                    self.args.model_name,
                    self.args.max_iterations,
                    self.args.timeout_per_command,
                    self.args.verbose,
                    i,  # process_id
                    project_index,  # project_index
                    total_projects  # total_projects
                )
                chunk_args.append(args_tuple)
            
            chunks.append(chunk_args)
            self.logger.info(f"Process {i}: {len(chunk_args)} projects (indices {start_idx+1}-{end_idx})")
        
        return chunks

    def run_process_chunk(self, chunk_args: List[Tuple]) -> List[Dict]:
        """运行一个进程块中的所有项目"""
        if not chunk_args:
            return []
        
        process_id = chunk_args[0][6]  # 从参数元组中获取process_id
        logger = setup_logging(process_id)
        
        logger.info(f"🚀 Process {process_id} starting with {len(chunk_args)} projects")
        
        results = []
        for args_tuple in chunk_args:
            try:
                result = run_single_project(args_tuple)
                results.append(result)
                
                # 在进程间添加小延迟，避免过度竞争资源
                time.sleep(1)
                
            except Exception as e:
                logger.error(f"Error processing project in process {process_id}: {e}")
                # 创建错误结果
                project_path = args_tuple[0]
                error_result = {
                    'process_id': process_id,
                    'project_index': args_tuple[7],
                    'project_name': project_path.name,
                    'language': self.language,
                    'project_path': str(project_path),
                    'start_time': datetime.now().isoformat(),
                    'end_time': datetime.now().isoformat(),
                    'status': 'failed',
                    'error_message': str(e),
                    'iterations_completed': 0,
                    'execution_time_seconds': 0,
                    'copied_to_runnable': False
                }
                results.append(error_result)
        
        logger.info(f"🏁 Process {process_id} completed {len(results)} projects")
        return results

    def save_batch_summary(self):
        """保存批处理摘要"""
        self.batch_summary['end_time'] = datetime.now().isoformat()
        
        # 计算总执行时间
        if 'start_time' in self.batch_summary:
            start = datetime.fromisoformat(self.batch_summary['start_time'])
            end = datetime.fromisoformat(self.batch_summary['end_time'])
            self.batch_summary['total_execution_time_seconds'] = (end - start).total_seconds()
        
        # 保存JSON摘要
        summary_file = self.batch_output_dir / "batch_summary.json"
        with open(summary_file, 'w', encoding='utf-8') as f:
            json.dump(self.batch_summary, f, indent=2, ensure_ascii=False)
        
        # 保存人类可读的摘要
        summary_text_file = self.batch_output_dir / "batch_summary.txt"
        with open(summary_text_file, 'w', encoding='utf-8') as f:
            f.write("Multi-Process Matlab Batch Test Detection Summary\n")
            f.write("=" * 60 + "\n")
            f.write(f"Start Time: {self.batch_summary['start_time']}\n")
            f.write(f"End Time: {self.batch_summary['end_time']}\n")
            f.write(f"Total Execution Time: {self.batch_summary.get('total_execution_time_seconds', 0):.2f} seconds\n")
            f.write(f"Language: {self.batch_summary['language']}\n")
            f.write(f"Number of Processes: {self.batch_summary['num_processes']}\n")
            f.write("\n")
            f.write(f"📊 RESULTS SUMMARY:\n")
            f.write(f"Total Projects: {self.batch_summary['total_projects']}\n")
            f.write(f"✅ Successful Projects: {self.batch_summary['successful_projects']}\n")
            f.write(f"❌ Failed Projects: {self.batch_summary['failed_projects']}\n")
            f.write(f"⏸️ Interrupted Projects: {self.batch_summary['interrupted_projects']}\n")
            
            if self.batch_summary['total_projects'] > 0:
                success_rate = (self.batch_summary['successful_projects'] / self.batch_summary['total_projects']) * 100
                f.write(f"\n📈 Success Rate: {success_rate:.2f}%\n")
            
            # 按进程统计
            f.write(f"\n📊 RESULTS BY PROCESS:\n")
            f.write("-" * 30 + "\n")
            
            process_stats = {}
            for result in self.batch_summary['results']:
                pid = result['process_id']
                if pid not in process_stats:
                    process_stats[pid] = {'total': 0, 'success': 0, 'failed': 0, 'interrupted': 0}
                
                process_stats[pid]['total'] += 1
                if result['status'] == 'success':
                    process_stats[pid]['success'] += 1
                elif result['status'] == 'interrupted':
                    process_stats[pid]['interrupted'] += 1
                else:
                    process_stats[pid]['failed'] += 1
            
            for pid in sorted(process_stats.keys()):
                stats = process_stats[pid]
                f.write(f"Process {pid}:\n")
                f.write(f"  Total: {stats['total']}\n")
                f.write(f"  ✅ Success: {stats['success']}\n")
                f.write(f"  ❌ Failed: {stats['failed']}\n")
                f.write(f"  ⏸️ Interrupted: {stats['interrupted']}\n")
                if stats['total'] > 0:
                    success_rate = (stats['success'] / stats['total']) * 100
                    f.write(f"  Success Rate: {success_rate:.1f}%\n")
                f.write("\n")
            
            f.write(f"📋 DETAILED RESULTS:\n")
            f.write("-" * 30 + "\n")
            
            # 按项目索引排序
            sorted_results = sorted(self.batch_summary['results'], key=lambda x: x['project_index'])
            
            for result in sorted_results:
                status_emoji = {
                    'success': '✅',
                    'failed': '❌',
                    'interrupted': '⏸️'
                }.get(result['status'], '❓')
                
                f.write(f"[{result['project_index']}] {result['project_name']} (Process {result['process_id']})\n")
                f.write(f"  Status: {status_emoji} {result['status'].upper()}\n")
                f.write(f"  Iterations: {result['iterations_completed']}\n")
                f.write(f"  Execution Time: {result['execution_time_seconds']:.2f}s\n")
                if result['copied_to_runnable']:
                    f.write(f"  📁 Copied to runnable_filter: Yes\n")
                if result['error_message']:
                    f.write(f"  Error: {result['error_message']}\n")
                f.write("\n")
        
        self.logger.info(f"📋 Batch summary saved to: {summary_file}")

    def run(self):
        """运行多进程批处理管道"""
        self.logger.info(f"🚀 Starting Multi-Process Matlab Batch Pipeline")
        self.logger.info(f"Number of processes: {self.num_processes}")
        self.logger.info(f"Primary filter: {self.primary_filter_base}")
        self.logger.info(f"Runnable filter: {self.runnable_filter_base}")
        self.logger.info(f"Batch output directory: {self.batch_output_dir}")
        self.logger.info("-" * 60)
        
        try:
            # 发现所有Matlab项目
            projects = self.discover_matlab_projects()
            if not projects:
                self.logger.error("No Matlab projects found!")
                return False
            
            self.batch_summary['total_projects'] = len(projects)
            self.logger.info(f"Found {len(projects)} Matlab projects to process")
            
            # 将项目分成多个进程块
            process_chunks = self.split_projects_into_chunks(projects)
            self.logger.info(f"Split into {len(process_chunks)} process chunks")
            
            # 使用多进程处理
            all_results = []
            
            if self.num_processes == 1:
                # 单进程模式
                self.logger.info("Running in single-process mode")
                for chunk_args in process_chunks:
                    chunk_results = self.run_process_chunk(chunk_args)
                    all_results.extend(chunk_results)
            else:
                # 多进程模式
                self.logger.info(f"Running in multi-process mode with {self.num_processes} processes")
                
                with Pool(processes=self.num_processes) as pool:
                    # 提交所有进程块
                    futures = []
                    for chunk_args in process_chunks:
                        future = pool.apply_async(self.run_process_chunk, (chunk_args,))
                        futures.append(future)
                    
                    # 收集结果
                    for i, future in enumerate(futures):
                        try:
                            chunk_results = future.get(timeout=3600)  # 1小时超时
                            all_results.extend(chunk_results)
                            self.logger.info(f"✅ Process chunk {i} completed with {len(chunk_results)} results")
                        except Exception as e:
                            self.logger.error(f"❌ Process chunk {i} failed: {e}")
            
            # 汇总结果
            self.batch_summary['results'] = all_results
            
            for result in all_results:
                if result['status'] == 'success':
                    self.batch_summary['successful_projects'] += 1
                elif result['status'] == 'interrupted':
                    self.batch_summary['interrupted_projects'] += 1
                else:
                    self.batch_summary['failed_projects'] += 1
            
            self.logger.info(f"✅ All processes completed!")
            
        except KeyboardInterrupt:
            self.logger.info("🛑 Batch processing interrupted by user")
            
        except Exception as e:
            self.logger.error(f"❌ Batch processing failed: {e}")
            self.logger.error(f"Full traceback: {traceback.format_exc()}")
        
        finally:
            # 保存摘要
            self.save_batch_summary()
            
            # 打印最终摘要
            self.logger.info("🏁 Multi-Process Matlab Batch Complete!")
            self.logger.info("=" * 60)
            self.logger.info(f"📊 FINAL RESULTS:")
            self.logger.info(f"Total Projects: {self.batch_summary['total_projects']}")
            self.logger.info(f"✅ Successful: {self.batch_summary['successful_projects']}")
            self.logger.info(f"❌ Failed: {self.batch_summary['failed_projects']}")
            self.logger.info(f"⏸️ Interrupted: {self.batch_summary['interrupted_projects']}")
            
            if self.batch_summary['total_projects'] > 0:
                success_rate = (self.batch_summary['successful_projects'] / self.batch_summary['total_projects']) * 100
                self.logger.info(f"🎯 Success Rate: {success_rate:.2f}%")
            
            self.logger.info(f"📁 Runnable projects saved to: {self.runnable_filter_base}")
            
            return self.batch_summary['successful_projects'] > 0


def main():
    parser = argparse.ArgumentParser(description="Multi-Process Batch Test Detection for Matlab Projects")
    parser.add_argument('--model_name', type=str, default="claude-3-7-sonnet-20250219",
                       help="Model name to use (default: claude-3-7-sonnet-20250219)")
    parser.add_argument('--num_processes', type=int, default=10,
                       help="Number of parallel processes (default: 10)")
    parser.add_argument('--max_iterations', type=int, default=10,
                       help="Maximum iterations per project (default: 10)")
    parser.add_argument('--timeout_per_command', type=int, default=20,
                       help="Timeout per command in seconds (default: 20)")
    parser.add_argument('--verbose', action='store_true',
                       help="Enable verbose logging")
    args = parser.parse_args()
    
    # 设置多进程启动方法
    if hasattr(multiprocessing, 'set_start_method'):
        try:
            multiprocessing.set_start_method('spawn', force=True)
        except RuntimeError:
            pass  # 已经设置过了
    
    print(f"🚀 Starting Multi-Process Matlab Batch Pipeline")
    print(f"Number of processes: {args.num_processes}")
    print(f"Expected ~1885 Matlab projects")
    print(f"Model: {args.model_name}")
    print(f"Max iterations per project: {args.max_iterations}")
    print(f"Command timeout: {args.timeout_per_command}s")
    print("-" * 60)
    
    try:
        # 创建并运行批处理管道
        batch_runner = MultiProcessBatchRunner(args)
        has_successful_projects = batch_runner.run()
        
        # 适当的退出码
        if has_successful_projects:
            print("✅ Batch completed with successful projects")
            sys.exit(0)
        else:
            print("⚠️ Batch completed but no projects were successful")
            sys.exit(1)
            
    except Exception as e:
        print(f"💥 Batch pipeline failed: {e}")
        print(f"Full traceback: {traceback.format_exc()}")
        sys.exit(2)


if __name__ == "__main__":
    main()