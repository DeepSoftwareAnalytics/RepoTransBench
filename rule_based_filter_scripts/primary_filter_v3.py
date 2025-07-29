#!/usr/bin/env python3
"""
高性能项目过滤脚本 - 修复卡住和多进程问题
主要修复：
1. 添加超时机制防止卡住
2. 改进错误处理和进程管理
3. 修复复制操作的各种边界情况
4. 确保多进程正确结束
"""

import os
import shutil
import re
import signal
import threading
from pathlib import Path
from typing import Dict, List, Set, Tuple, Optional
import json
import multiprocessing as mp
from concurrent.futures import ProcessPoolExecutor, ThreadPoolExecutor, as_completed, TimeoutError
import time
from collections import defaultdict
import mmap
from functools import lru_cache
import logging

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class TimeoutError(Exception):
    """自定义超时异常"""
    pass

def timeout_handler(signum, frame):
    """超时信号处理器"""
    raise TimeoutError("Operation timed out")

def safe_rmtree(path: Path, max_retries: int = 3) -> bool:
    """安全删除目录树，处理各种异常情况"""
    for attempt in range(max_retries):
        try:
            if path.exists():
                # 处理只读文件
                def handle_remove_readonly(func, path, exc):
                    if os.path.exists(path):
                        os.chmod(path, 0o777)
                        func(path)
                
                shutil.rmtree(path, onerror=handle_remove_readonly)
            return True
        except (PermissionError, OSError) as e:
            if attempt == max_retries - 1:
                logger.warning(f"无法删除目录 {path}: {e}")
                return False
            time.sleep(0.1 * (attempt + 1))  # 递增延迟
    return False

def safe_copytree(src: Path, dst: Path, timeout: int = 60) -> bool:
    """安全复制目录树，带超时和错误处理"""
    def copy_with_timeout():
        try:
            # 自定义忽略函数，跳过问题文件
            def ignore_problematic_files(directory, contents):
                ignored = []
                for item in contents:
                    item_path = Path(directory) / item
                    try:
                        # 跳过符号链接
                        if item_path.is_symlink():
                            ignored.append(item)
                            continue
                        # 跳过过大的文件 (>100MB)
                        if item_path.is_file() and item_path.stat().st_size > 100 * 1024 * 1024:
                            ignored.append(item)
                            continue
                        # 跳过特殊文件类型
                        if item.startswith('.') and item in {'.git', '.svn', '.hg', '__pycache__'}:
                            ignored.append(item)
                            continue
                    except (OSError, PermissionError):
                        ignored.append(item)
                        continue
                return ignored
            
            # 确保目标目录不存在
            if dst.exists():
                safe_rmtree(dst)
            
            # 复制目录
            shutil.copytree(
                src, dst, 
                ignore=ignore_problematic_files,
                dirs_exist_ok=False,
                ignore_dangling_symlinks=True,
                symlinks=False  # 不复制符号链接
            )
            return True
            
        except Exception as e:
            logger.error(f"复制失败 {src} -> {dst}: {e}")
            return False
    
    # 使用线程超时机制
    result = [False]
    
    def worker():
        result[0] = copy_with_timeout()
    
    thread = threading.Thread(target=worker)
    thread.daemon = True
    thread.start()
    thread.join(timeout)
    
    if thread.is_alive():
        logger.warning(f"复制超时 {src} -> {dst}")
        return False
    
    return result[0]

class FastProjectFilter:
    def __init__(self, source_dir: str, target_dir: str, max_workers: int = None):
        self.source_dir = Path(source_dir)
        self.target_dir = Path(target_dir)
        # 减少工作进程数，避免资源竞争
        self.max_workers = max_workers or min(mp.cpu_count(), 4)
        
        # 创建目标目录
        self.target_dir.mkdir(exist_ok=True)
        
        # 优化的过滤规则 - 包含所有需要的语言
        self.filter_rules = {
            'Python': {
                'extensions': {'.py'},
                'exclude_patterns': [
                    # 深度学习/机器学习框架
                    rb'\b(?:import\s+(?:torch|tensorflow|keras|sklearn|numpy|pandas|scipy|opencv|cv2|PIL|Pillow)|from\s+(?:torch|tensorflow|keras|sklearn|numpy|pandas|scipy|opencv|cv2|PIL|Pillow))\b',
                    # GUI框架
                    rb'\b(?:import\s+(?:tkinter|PyQt[56]|PySide[26]|kivy)|from\s+(?:tkinter|PyQt[56]|PySide[26]|kivy))\b',
                    # 平台特定
                    rb'\b(?:import\s+(?:win32api|win32gui|pywin32)|from\s+(?:win32api|win32gui|pywin32))\b',
                    # 游戏引擎
                    rb'\b(?:import\s+(?:pygame|panda3d)|from\s+(?:pygame|panda3d))\b',
                ],
                'test_files': {
                    'test_*.py', '*_test.py', 'conftest.py', 'pytest.ini', 'tox.ini', 'setup.cfg'
                },
                'test_dirs': {'test', 'tests'}
            },
            'C++': {
                'extensions': {'.cpp', '.cc', '.cxx', '.c++', '.h', '.hpp', '.hxx', '.h++'},
                'exclude_patterns': [
                    # GUI框架
                    rb'#include\s*[<"](?:Qt/|gtk/|wxWidgets|FLTK)',
                    # 游戏引擎
                    rb'#include\s*[<"](?:unreal|unity|ogre)',
                    # 图形API
                    rb'#include\s*[<"](?:OpenGL|DirectX|Vulkan)',
                    # 平台特定
                    rb'#include\s*[<"](?:windows\.h|win32)',
                    # 计算机视觉
                    rb'#include\s*[<"](?:opencv)',
                ],
                'test_files': {'test_*.cpp', '*_test.cpp', 'test_*.cc', '*_test.cc', 'CMakeLists.txt', 'Makefile'},
                'test_dirs': {'test', 'tests'}
            },
            'JavaScript': {
                'extensions': {'.js', '.jsx', '.ts', '.tsx', '.mjs'},
                'exclude_patterns': [
                    # 前端框架
                    rb'\b(?:import\s+.*(?:react|vue|angular|svelte)|require\s*\(\s*["\'].*(?:react|vue|angular|svelte)|from\s+["\'].*(?:react|vue|angular|svelte))\b',
                    # 构建工具
                    rb'\b(?:import\s+.*(?:webpack|vite|rollup|parcel)|require\s*\(\s*["\'].*(?:webpack|vite|rollup|parcel))\b',
                    # 浏览器特定
                    rb'\b(?:document\.|window\.|jquery|\$\()',
                    # Node.js特定
                    rb'\b(?:import\s+.*electron|require\s*\(\s*["\'].*electron)\b',
                ],
                'test_files': {
                    '*.test.js', '*.test.ts', '*.spec.js', '*.spec.ts',
                    'jest.config.*', 'mocha.opts', 'karma.conf.*', 'package.json'
                },
                'test_dirs': {'test', 'tests', '__tests__', 'spec'}
            },
            'C': {
                'extensions': {'.c', '.h'},
                'exclude_patterns': [
                    # 平台特定头文件
                    rb'#include\s*[<"](?:windows\.h|win32)',
                    # GUI
                    rb'#include\s*[<"](?:gtk/|qt)',
                    # 系统调用
                    rb'#include\s*[<"](?:sys/|unistd\.h)',
                ],
                'test_files': {'test_*.c', '*_test.c', 'Makefile', 'CMakeLists.txt'},
                'test_dirs': {'test', 'tests'}
            },
            'Java': {
                'extensions': {'.java'},
                'exclude_patterns': [
                    # Android开发
                    rb'\bimport\s+(?:android|androidx\.)',
                    # GUI框架
                    rb'\bimport\s+(?:javax\.swing|java\.awt|javafx)',
                    # 游戏引擎
                    rb'\bimport\s+(?:lwjgl|libgdx)',
                ],
                'test_files': {'*Test.java', '*Tests.java', 'pom.xml', 'build.gradle', 'build.xml'},
                'test_dirs': {'test', 'src/test'}
            },
            'Matlab': {
                'extensions': {'.m'},
                'exclude_patterns': [
                    # Matlab特定工具箱
                    rb'\b(?:Simulink|toolbox)',
                    # 图形相关
                    rb'\b(?:plot|figure|imshow|imagesc|surf|mesh)\s*\(',
                    # 信号处理工具箱
                    rb'\b(?:fft|ifft|filter|freqz)\s*\(',
                ],
                'test_files': {'test*.m', '*_test.m', 'runtests.m'},
                'test_dirs': {'test', 'tests'}
            },
            'C#': {
                'extensions': {'.cs'},
                'exclude_patterns': [
                    # 平台特定
                    rb'\busing\s+(?:System\.Windows|Microsoft\.Win32)',
                    # GUI框架
                    rb'\busing\s+(?:System\.Windows\.Forms|System\.Windows\.Controls)',
                    # 游戏引擎
                    rb'\busing\s+(?:UnityEngine|Microsoft\.Xna)',
                    # ASP.NET
                    rb'\busing\s+(?:System\.Web)',
                ],
                'test_files': {'*Test.cs', '*Tests.cs', '*.csproj', '*.sln'},
                'test_dirs': {'test', 'Test', 'Tests'}
            }
        }
        
        # 预编译所有正则表达式
        self._precompile_patterns()
        
        # 性能统计
        self.stats = {
            'files_checked': 0,
            'pattern_matches': 0,
            'projects_analyzed': 0
        }
    
    def _precompile_patterns(self):
        """预编译所有正则表达式模式"""
        self.compiled_patterns = {}
        
        for language, rules in self.filter_rules.items():
            compiled_rules = {
                'extensions': rules['extensions'],
                'exclude_patterns': [],
                'test_files': rules['test_files'],
                'test_dirs': rules['test_dirs']
            }
            
            # 编译排除模式
            for pattern in rules['exclude_patterns']:
                try:
                    compiled_rules['exclude_patterns'].append(
                        re.compile(pattern, re.MULTILINE | re.IGNORECASE)
                    )
                except re.error as e:
                    logger.warning(f"Failed to compile pattern {pattern}: {e}")
            
            self.compiled_patterns[language] = compiled_rules
    
    @staticmethod
    def fast_file_check(file_path: Path, patterns: List[re.Pattern], max_size: int = 512 * 1024) -> bool:
        """高效的文件内容检查 - 带超时保护"""
        try:
            # 跳过过大的文件
            file_size = file_path.stat().st_size
            if file_size > max_size:
                return False
            
            if file_size == 0:
                return False
            
            # 设置超时保护
            def read_file_content():
                with open(file_path, 'rb') as f:
                    if file_size < 8192:
                        return f.read()
                    else:
                        with mmap.mmap(f.fileno(), 0, access=mmap.ACCESS_READ) as mm:
                            return mm[:]
            
            # 使用线程超时
            content = None
            result = [None]
            
            def worker():
                try:
                    result[0] = read_file_content()
                except Exception as e:
                    result[0] = None
            
            thread = threading.Thread(target=worker)
            thread.daemon = True
            thread.start()
            thread.join(timeout=5)  # 5秒超时
            
            if thread.is_alive() or result[0] is None:
                return False
            
            content = result[0]
            
            # 检查模式匹配
            for pattern in patterns:
                if pattern.search(content):
                    return True
                    
            return False
            
        except (OSError, PermissionError, UnicodeDecodeError):
            return False
    
    @staticmethod
    def quick_test_check(project_path: Path, test_files: Set[str], test_dirs: Set[str]) -> bool:
        """快速检查测试文件 - 带超时保护"""
        try:
            # 首先检查常见的测试目录
            for test_dir in test_dirs:
                test_path = project_path / test_dir
                if test_path.exists() and test_path.is_dir():
                    try:
                        if any(test_path.iterdir()):
                            return True
                    except (PermissionError, OSError):
                        continue
            
            # 检查测试文件模式 - 限制搜索范围
            files_checked = 0
            max_files = 30
            max_depth = 2
            
            def check_directory(dir_path: Path, depth: int) -> bool:
                nonlocal files_checked
                if depth > max_depth or files_checked > max_files:
                    return False
                    
                try:
                    for item in dir_path.iterdir():
                        if files_checked > max_files:
                            return False
                            
                        if item.is_file():
                            files_checked += 1
                            for pattern in test_files:
                                if item.match(pattern):
                                    return True
                        elif item.is_dir() and not item.name.startswith('.'):
                            if check_directory(item, depth + 1):
                                return True
                except (PermissionError, OSError):
                    pass
                return False
            
            return check_directory(project_path, 0)
        except Exception:
            return False
    
    @staticmethod
    def analyze_project_with_timeout(args: tuple) -> tuple:
        """带超时的项目分析"""
        project_path_str, language, compiled_rules = args
        
        # 设置进程超时
        def timeout_handler(signum, frame):
            raise TimeoutError("Analysis timeout")
        
        # 只在Unix系统上设置信号处理器
        if hasattr(signal, 'SIGALRM'):
            signal.signal(signal.SIGALRM, timeout_handler)
            signal.alarm(30)  # 30秒超时
        
        try:
            result = FastProjectFilter.analyze_project_fast((project_path_str, language, compiled_rules))
            return result
        except TimeoutError:
            return project_path_str, True, ["Analysis timeout"], 0, 0
        except Exception as e:
            return project_path_str, True, [f"Analysis error: {str(e)}"], 0, 0
        finally:
            if hasattr(signal, 'SIGALRM'):
                signal.alarm(0)  # 取消超时
    
    @staticmethod
    def analyze_project_fast(args: tuple) -> tuple:
        """快速分析单个项目"""
        project_path_str, language, compiled_rules = args
        project_path = Path(project_path_str)
        
        try:
            # 1. 快速测试检查
            if not FastProjectFilter.quick_test_check(
                project_path, 
                compiled_rules['test_files'], 
                compiled_rules['test_dirs']
            ):
                return project_path_str, True, ["No tests found"], 0, 0
            
            # 2. 快速代码文件检查
            excluded_count = 0
            total_count = 0
            max_files = 15  # 进一步减少检查文件数
            
            patterns = compiled_rules['exclude_patterns']
            extensions = compiled_rules['extensions']
            
            # 只检查项目根目录和一级子目录
            dirs_to_check = [project_path]
            try:
                for item in project_path.iterdir():
                    if item.is_dir() and not item.name.startswith('.') and len(dirs_to_check) < 3:
                        dirs_to_check.append(item)
            except (PermissionError, OSError):
                pass
            
            for dir_path in dirs_to_check:
                if total_count >= max_files:
                    break
                    
                try:
                    for file_path in dir_path.iterdir():
                        if total_count >= max_files:
                            break
                            
                        if file_path.is_file() and file_path.suffix in extensions:
                            total_count += 1
                            if FastProjectFilter.fast_file_check(file_path, patterns):
                                excluded_count += 1
                                if excluded_count > max_files * 0.6:
                                    break
                except (PermissionError, OSError):
                    continue
            
            # 3. 决策逻辑
            should_exclude = False
            reasons = []
            
            if total_count > 0:
                exclusion_ratio = excluded_count / total_count
                if exclusion_ratio > 0.5:
                    should_exclude = True
                    reasons.append(f"Too many files use untranslatable libraries: {excluded_count}/{total_count} ({exclusion_ratio:.1%})")
            
            return project_path_str, should_exclude, reasons, total_count, excluded_count
            
        except Exception as e:
            return project_path_str, True, [f"Analysis error: {str(e)}"], 0, 0
    
    def get_projects_batch(self) -> List[Tuple[str, str]]:
        """批量获取项目列表"""
        projects = []
        
        for language in self.compiled_patterns.keys():
            language_dir = self.source_dir / language
            if not language_dir.exists():
                continue
                
            try:
                for project_dir in language_dir.iterdir():
                    if project_dir.is_dir():
                        projects.append((str(project_dir), language))
            except (PermissionError, OSError):
                logger.warning(f"Permission denied: {language_dir}")
                continue
        
        return projects
    
    def copy_project_safe(self, source_path: str, target_path: str) -> bool:
        """安全复制项目"""
        try:
            source = Path(source_path)
            target = Path(target_path)
            
            # 确保目标目录存在
            target.parent.mkdir(parents=True, exist_ok=True)
            
            # 使用改进的复制函数
            return safe_copytree(source, target, timeout=60)
                
        except Exception as e:
            logger.error(f"复制失败 {source_path}: {e}")
            return False
    
    def filter_projects(self):
        """执行高性能项目过滤"""
        logger.info(f"🚀 启动高性能过滤器，使用 {self.max_workers} 个工作进程")
        
        start_time = time.time()
        
        # 批量获取所有项目
        all_projects = self.get_projects_batch()
        total_projects = len(all_projects)
        logger.info(f"发现 {total_projects} 个项目")
        
        if total_projects == 0:
            logger.warning("❌ 没有找到任何项目")
            return {}
        
        # 准备批处理参数
        process_args = [
            (project_path, language, self.compiled_patterns[language])
            for project_path, language in all_projects
        ]
        
        # 统计信息
        stats = defaultdict(lambda: {'total': 0, 'kept': 0, 'excluded': 0})
        projects_to_copy = []
        processed = 0
        
        logger.info("开始分析项目...")
        
        # 使用更小的批次，更好的错误处理
        batch_size = 50
        
        for i in range(0, len(process_args), batch_size):
            batch = process_args[i:i + batch_size]
            
            # 使用上下文管理器确保进程池正确关闭
            with ProcessPoolExecutor(max_workers=self.max_workers) as executor:
                # 提交所有任务
                future_to_args = {
                    executor.submit(self.analyze_project_with_timeout, args): args 
                    for args in batch
                }
                
                # 处理完成的任务，设置超时
                try:
                    for future in as_completed(future_to_args, timeout=120):  # 2分钟批次超时
                        try:
                            project_path, should_exclude, reasons, total_files, excluded_files = future.result(timeout=60)
                            
                            language = Path(project_path).parent.name
                            project_name = Path(project_path).name
                            
                            stats[language]['total'] += 1
                            processed += 1
                            
                            if should_exclude:
                                stats[language]['excluded'] += 1
                            else:
                                stats[language]['kept'] += 1
                                projects_to_copy.append((project_path, language, project_name))
                            
                            # 定期输出进度
                            if processed % 20 == 0:
                                elapsed = time.time() - start_time
                                rate = processed / elapsed if elapsed > 0 else 0
                                eta = (total_projects - processed) / rate if rate > 0 else 0
                                logger.info(f"分析进度: {processed}/{total_projects} ({processed/total_projects*100:.1f}%) "
                                          f"速度: {rate:.1f}/秒 预计剩余: {eta:.0f}秒")
                        
                        except TimeoutError:
                            logger.warning("任务超时，跳过")
                            processed += 1
                        except Exception as e:
                            logger.error(f"处理任务时出错: {e}")
                            processed += 1
                
                except TimeoutError:
                    logger.warning("批次处理超时，继续下一批")
                    # 取消未完成的任务
                    for future in future_to_args:
                        future.cancel()
        
        analysis_time = time.time() - start_time
        logger.info(f"✅ 分析完成！耗时: {analysis_time:.1f}秒")
        logger.info(f"需要复制 {len(projects_to_copy)} 个项目")
        
        # 串行复制，避免并发问题
        if projects_to_copy:
            logger.info("开始复制项目...")
            copy_start = time.time()
            
            copied_count = 0
            failed_count = 0
            
            for i, (project_path, language, project_name) in enumerate(projects_to_copy):
                try:
                    target_lang_dir = self.target_dir / language
                    target_lang_dir.mkdir(exist_ok=True)
                    target_project_path = str(target_lang_dir / project_name)
                    
                    if self.copy_project_safe(project_path, target_project_path):
                        copied_count += 1
                    else:
                        failed_count += 1
                        
                    if (i + 1) % 10 == 0:
                        logger.info(f"复制进度: {i + 1}/{len(projects_to_copy)} (成功: {copied_count}, 失败: {failed_count})")
                        
                except Exception as e:
                    logger.error(f"复制项目时出错: {e}")
                    failed_count += 1
            
            copy_time = time.time() - copy_start
            logger.info(f"✅ 复制完成！耗时: {copy_time:.1f}秒，成功: {copied_count}，失败: {failed_count}")
        
        # 保存统计
        stats_dict = dict(stats)
        stats_file = self.target_dir / 'filter_stats.json'
        try:
            with open(stats_file, 'w', encoding='utf-8') as f:
                json.dump(stats_dict, f, indent=2, ensure_ascii=False)
        except Exception as e:
            logger.error(f"保存统计文件失败: {e}")
        
        total_time = time.time() - start_time
        logger.info(f"\n🎉 过滤完成！总耗时: {total_time:.1f}秒")
        logger.info(f"平均处理速度: {total_projects/total_time:.1f} 项目/秒")
        
        return stats_dict

def main():
    source_dir = "github_repos"
    target_dir = "primary_filter_v3"
    
    if not os.path.exists(source_dir):
        logger.error(f"❌ 源目录 {source_dir} 不存在")
        return
    
    # 使用保守的工作进程数
    max_workers = min(mp.cpu_count(), 4)
    logger.info(f"🖥️  检测到 {mp.cpu_count()} 个CPU核心，使用 {max_workers} 个进程")
    
    try:
        filter_instance = FastProjectFilter(source_dir, target_dir, max_workers)
        stats = filter_instance.filter_projects()
        
        # 打印统计结果
        print("\n📊 最终统计:")
        print("-" * 60)
        if stats:
            total_projects = sum(s['total'] for s in stats.values())
            kept_projects = sum(s['kept'] for s in stats.values())
            excluded_projects = sum(s['excluded'] for s in stats.values())
            
            print(f"📈 总项目数: {total_projects:,}")
            print(f"✅ 保留项目: {kept_projects:,}")
            print(f"❌ 排除项目: {excluded_projects:,}")
            if total_projects > 0:
                print(f"📋 保留率: {kept_projects/total_projects*100:.1f}%")
            
            print(f"\n🔍 各语言详情:")
            for lang, stat in sorted(stats.items()):
                if stat['total'] > 0:
                    keep_rate = stat['kept'] / stat['total'] * 100
                    print(f"  {lang:>12}: {stat['kept']:>4}/{stat['total']:>4} ({keep_rate:>5.1f}%)")
        else:
            print("❌ 没有找到任何项目")
    
    except KeyboardInterrupt:
        logger.info("\n⚠️ 用户中断操作")
    except Exception as e:
        logger.error(f"\n❌ 运行出错: {e}")
        import traceback
        traceback.print_exc()
    finally:
        logger.info("\n🏁 程序结束")

if __name__ == "__main__":
    main()