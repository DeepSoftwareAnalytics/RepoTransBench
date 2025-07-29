# file path: testcase_target_agent_batch/run_batch.py

"""
Batch runner for test case translation pipeline across multiple languages and projects.
Translates test cases from source language repositories to target language repositories.
Supports multi-processing and resume functionality.
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
import multiprocessing as mp
from concurrent.futures import ProcessPoolExecutor, as_completed
import pickle
from testcase_target_agent_batch.run import TestCaseTranslationPipeline


# Configure logging
logging.basicConfig(
    format='%(asctime)s - %(levelname)s - %(name)s - %(message)s',
    datefmt='%m/%d/%Y %H:%M:%S',
    level=logging.INFO,
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler('test_translation_batch_debug.log')
    ]
)
logger = logging.getLogger(__name__)


# Worker function for multiprocessing
def process_single_translation_worker(args_tuple: Tuple) -> Dict:
    """Worker function to process a single translation in a separate process"""
    project_path_str, source_language, target_language, model_name, max_iterations, timeout_per_command, verbose = args_tuple
    
    # Setup logging for worker process
    process_logger = logging.getLogger(f'worker_{os.getpid()}')
    
    project_path = Path(project_path_str)
    project_name = project_path.name
    start_time = datetime.now()
    
    process_logger.info(f"🔄 [PID {os.getpid()}] Starting test translation for: {project_name} ({source_language} → {target_language})")
    
    result = {
        'project_name': project_name,
        'source_language': source_language,
        'target_language': target_language,
        'project_path': str(project_path),
        'start_time': start_time.isoformat(),
        'status': 'unknown',  # 'success', 'failed', 'interrupted', 'timeout'
        'error_message': None,
        'iterations_completed': 0,
        'execution_time_seconds': 0,
        'translation_completed': False,
        'source_original_tests_count': 0,
        'source_public_tests_count': 0,
        'target_original_tests_count': 0,
        'target_public_tests_count': 0,
        'project_structure_created': False,
        'worker_pid': os.getpid(),
        'files_copied_summary': {
            'target_test_files': 0,
            'additional_target_files': 0,
            'common_files': 0,
            'total_files': 0
        }
    }
    
    try:
        # Load encoding in worker process
        encoding = tiktoken.encoding_for_model("gpt-4")

        # Create arguments for TestCaseTranslationPipeline
        pipeline_args = argparse.Namespace(
            model_name=model_name,
            repo_path=str(project_path),
            source_language=source_language,
            target_language=target_language,
            max_iterations=max_iterations,
            timeout_per_command=timeout_per_command,
            verbose=verbose
        )
        
        # Initialize and run pipeline
        pipeline = TestCaseTranslationPipeline(pipeline_args, encoding)
        final_status, finished_data = pipeline.run()
        
        # Record results based on pipeline status
        result['iterations_completed'] = pipeline.current_iteration
        result['status'] = final_status or 'failed'
        
        if finished_data:
            result['source_original_tests_count'] = len(finished_data.get('source_original_tests', []))
            result['source_public_tests_count'] = len(finished_data.get('source_public_tests', []))
            result['target_original_tests_count'] = len(finished_data.get('target_original_tests', []))
            result['target_public_tests_count'] = len(finished_data.get('target_public_tests', []))
            result['project_structure_created'] = bool(finished_data.get('project_structure'))
        
        if final_status == 'success':
            process_logger.info(f"✅ [PID {os.getpid()}] Successfully translated tests for project: {project_name}")
            result['translation_completed'] = True
            
            # Try to get file copy statistics from the result directory
            try:
                translation_results_base = Path("translated_test_results")
                result_path = translation_results_base / source_language / target_language / project_name
                summary_file = result_path / "translation_summary.json"
                
                if summary_file.exists():
                    with open(summary_file, 'r', encoding='utf-8') as f:
                        summary_data = json.load(f)
                        result['files_copied_summary'] = summary_data.get('files_copied_summary', result['files_copied_summary'])
            except Exception as e:
                process_logger.warning(f"Could not read file copy statistics: {e}")
            
            if finished_data:
                process_logger.info(f"📊 Translation stats - Source: {result['source_original_tests_count']} original + {result['source_public_tests_count']} public, "
                                  f"Target: {result['target_original_tests_count']} original + {result['target_public_tests_count']} public")
                process_logger.info(f"📁 Files copied: {result['files_copied_summary']['total_files']} total files")
                
        elif final_status == 'failed':
            process_logger.info(f"❌ [PID {os.getpid()}] Failed test translation for project: {project_name}")
        elif final_status == 'timeout':
            process_logger.info(f"⏱️  [PID {os.getpid()}] Test translation timed out for project: {project_name}")
        elif final_status == 'interrupted':
            process_logger.info(f"⏸️  [PID {os.getpid()}] Test translation interrupted for project: {project_name}")
        else:
            process_logger.warning(f"❓ [PID {os.getpid()}] Unknown status for project: {project_name}: {final_status}")

    except KeyboardInterrupt:
        process_logger.info(f"❌ [PID {os.getpid()}] Translation interrupted by user")
        result['error_message'] = "Interrupted by user"
        result['status'] = 'interrupted'
        
    except Exception as e:
        error_msg = f"Failed to process translation for {project_name}: {str(e)}"
        process_logger.error(f"❌ [PID {os.getpid()}] {error_msg}")
        process_logger.error(f"Full traceback: {traceback.format_exc()}")
        result['error_message'] = str(e)
        result['status'] = 'failed'
    
    finally:
        end_time = datetime.now()
        result['end_time'] = end_time.isoformat()
        result['execution_time_seconds'] = (end_time - start_time).total_seconds()
        
        process_logger.info(f"🏁 [PID {os.getpid()}] Completed translation {project_name} in {result['execution_time_seconds']:.1f}s")
    
    return result


class BatchTestTranslationPipeline:
    def __init__(self, args, encoding):
        self.args = args
        self.encoding = encoding
        self.source_language = args.source_language
        self.target_language = args.target_language
        self.num_workers = args.num_workers
        
        # Use verified_repos as the source directory
        self.source_repos_base = Path("source_projects").resolve()
        self.translation_results_base = Path("translated_test_results").resolve()
        self.max_projects_per_language = args.max_projects_per_language
        self.batch_results = []
        
        # Validate source directory exists
        if not self.source_repos_base.exists():
            raise FileNotFoundError(f"Source repos directory does not exist: {self.source_repos_base}")
        
        # Create translation_results directory if it doesn't exist
        self.translation_results_base.mkdir(parents=True, exist_ok=True)
        
        # Setup batch output directory with new structure
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        self.batch_output_dir = Path("batch_translation_logs") / f"batch_run_{self.source_language}_to_{self.target_language}_{timestamp}"
        self.batch_output_dir.mkdir(parents=True, exist_ok=True)
        
        # Resume functionality
        self.resume_file = Path(f"batch_translation_progress_{self.source_language}_to_{self.target_language}.pkl")
        self.completed_projects = set()
        self.failed_projects = set()
        
        # Initialize batch summary
        self.batch_summary = {
            'start_time': datetime.now().isoformat(),
            'source_language': self.source_language,
            'target_language': self.target_language,
            'max_projects_per_language': self.max_projects_per_language,
            'num_workers': self.num_workers,
            'total_projects': 0,
            'successful_projects': 0,
            'failed_projects': 0,
            'interrupted_projects': 0,
            'timeout_projects': 0,
            'skipped_projects': 0,
            'total_source_tests_translated': 0,
            'total_target_tests_created': 0,
            'total_files_copied': 0,
            'average_translation_ratio': 0.0,
            'translation_distribution': {},
            'translation_metrics': {},
            'directory_structure_info': {
                'base_path': str(self.translation_results_base),
                'structure_format': 'source_language/target_language/project_name',
                'example_path': f"{self.source_language}/{self.target_language}/example_project"
            },
            'results': []
        }
        
        # Load previous progress if resuming
        if args.resume and self.resume_file.exists():
            self.load_progress()

    def load_progress(self):
        """Load previous progress for resume functionality"""
        try:
            with open(self.resume_file, 'rb') as f:
                progress_data = pickle.load(f)
            
            self.completed_projects = progress_data.get('completed_projects', set())
            self.failed_projects = progress_data.get('failed_projects', set())
            self.batch_summary = progress_data.get('batch_summary', self.batch_summary)
            
            logger.info(f"📥 Loaded progress: {len(self.completed_projects)} completed, {len(self.failed_projects)} failed")
            
        except Exception as e:
            logger.warning(f"⚠️  Failed to load progress file: {e}, starting fresh")
            self.completed_projects = set()
            self.failed_projects = set()

    def save_progress(self):
        """Save current progress for resume functionality"""
        try:
            progress_data = {
                'completed_projects': self.completed_projects,
                'failed_projects': self.failed_projects,
                'batch_summary': self.batch_summary,
                'timestamp': datetime.now().isoformat()
            }
            
            with open(self.resume_file, 'wb') as f:
                pickle.dump(progress_data, f)
                
        except Exception as e:
            logger.error(f"❌ Failed to save progress: {e}")

    def discover_projects(self) -> List[Path]:
        """Discover projects in the source language directory"""
        source_language_dir = self.source_repos_base / self.source_language
        
        if not source_language_dir.exists():
            logger.warning(f"Source language directory does not exist: {source_language_dir}")
            return []
        
        # Get all subdirectories (projects) in the source language directory
        projects = []
        try:
            for item in source_language_dir.iterdir():
                if item.is_dir() and not item.name.startswith('.'):
                    project_key = f"{self.source_language}:{item.name}"
                    
                    # Skip if already completed successfully or if we don't want to retry failed ones
                    if project_key in self.completed_projects:
                        logger.debug(f"Skipping already completed project: {item.name}")
                        continue
                    
                    if not self.args.retry_failed and project_key in self.failed_projects:
                        logger.debug(f"Skipping previously failed project: {item.name}")
                        continue
                    
                    projects.append(item)
            
            # Sort projects by name for consistent ordering
            projects.sort(key=lambda x: x.name.lower())
            
            # Limit to max_projects_per_language if specified (0 means no limit)
            if self.max_projects_per_language > 0 and len(projects) > self.max_projects_per_language:
                original_count = len(projects)
                projects = projects[:self.max_projects_per_language]
                logger.info(f"Limited {self.source_language} projects to first {self.max_projects_per_language} out of {original_count} total")
            
            logger.info(f"Found {len(projects)} projects to translate from {self.source_language} to {self.target_language}")
            return projects
            
        except Exception as e:
            logger.error(f"Error discovering projects for {self.source_language}: {e}")
            return []

    def calculate_translation_statistics(self):
        """Calculate translation statistics from results"""
        successful_results = [r for r in self.batch_summary['results'] 
                            if r['status'] == 'success' and r['translation_completed']]
        
        if not successful_results:
            return
        
        # Calculate totals
        total_source_tests = sum(r['source_original_tests_count'] + r['source_public_tests_count'] 
                               for r in successful_results)
        total_target_tests = sum(r['target_original_tests_count'] + r['target_public_tests_count'] 
                               for r in successful_results)
        total_files_copied = sum(r.get('files_copied_summary', {}).get('total_files', 0) 
                               for r in successful_results)
        
        self.batch_summary['total_source_tests_translated'] = total_source_tests
        self.batch_summary['total_target_tests_created'] = total_target_tests
        self.batch_summary['total_files_copied'] = total_files_copied
        
        # Calculate average translation ratio
        if total_source_tests > 0:
            self.batch_summary['average_translation_ratio'] = total_target_tests / total_source_tests
        
        # Calculate translation distribution based on test count
        translation_ranges = {
            '0-10 tests': 0, '11-25 tests': 0, '26-50 tests': 0, 
            '51-100 tests': 0, '100+ tests': 0
        }
        
        complete_translations = 0
        for result in successful_results:
            total_tests = result['target_original_tests_count'] + result['target_public_tests_count']
            if total_tests <= 10:
                translation_ranges['0-10 tests'] += 1
            elif total_tests <= 25:
                translation_ranges['11-25 tests'] += 1
            elif total_tests <= 50:
                translation_ranges['26-50 tests'] += 1
            elif total_tests <= 100:
                translation_ranges['51-100 tests'] += 1
            else:
                translation_ranges['100+ tests'] += 1
            
            if result['translation_completed'] and result['project_structure_created']:
                complete_translations += 1
        
        self.batch_summary['translation_distribution'] = translation_ranges
        
        # Calculate translation metrics
        self.batch_summary['translation_metrics'] = {
            'complete_translations': complete_translations,
            'complete_translation_percentage': (complete_translations / len(successful_results)) * 100 if successful_results else 0,
            'projects_with_structure': sum(1 for r in successful_results if r['project_structure_created']),
            'average_source_tests_per_project': total_source_tests / len(successful_results) if successful_results else 0,
            'average_target_tests_per_project': total_target_tests / len(successful_results) if successful_results else 0,
            'average_files_copied_per_project': total_files_copied / len(successful_results) if successful_results else 0
        }

    def save_translation_summary_file(self):
        """Save a comprehensive translation summary file with new directory structure"""
        # Save to the new directory structure location
        summary_dir = self.translation_results_base / self.source_language / self.target_language
        summary_dir.mkdir(parents=True, exist_ok=True)
        
        summary_file = summary_dir / f"batch_translation_summary.json"
        csv_file = summary_dir / f"batch_translation_summary.csv"
        
        # Prepare summary data
        projects_data = []
        successful_results = [r for r in self.batch_summary['results'] 
                            if r['status'] == 'success']
        
        for result in successful_results:
            project_data = {
                'project_name': result['project_name'],
                'source_language': result['source_language'],
                'target_language': result['target_language'],
                'translation_completed': result['translation_completed'],
                'source_original_tests_count': result['source_original_tests_count'],
                'source_public_tests_count': result['source_public_tests_count'],
                'target_original_tests_count': result['target_original_tests_count'],
                'target_public_tests_count': result['target_public_tests_count'],
                'project_structure_created': result['project_structure_created'],
                'execution_time_seconds': result['execution_time_seconds'],
                'iterations_completed': result['iterations_completed'],
                'project_path': result['project_path'],
                'worker_pid': result.get('worker_pid', 'N/A'),
                'result_location': f"{self.source_language}/{self.target_language}/{result['project_name']}"
            }
            
            # Add file copy statistics if available
            files_copied = result.get('files_copied_summary', {})
            if files_copied:
                project_data.update({
                    'target_test_files_copied': files_copied.get('target_test_files', 0),
                    'additional_target_files_copied': files_copied.get('additional_target_files', 0),
                    'common_files_copied': files_copied.get('common_files', 0),
                    'total_files_copied': files_copied.get('total_files', 0)
                })
            
            projects_data.append(project_data)
        
        # Sort by total target tests (descending)
        projects_data.sort(key=lambda x: x['target_original_tests_count'] + x['target_public_tests_count'], reverse=True)
        
        summary_data = {
            'timestamp': datetime.now().isoformat(),
            'source_language': self.source_language,
            'target_language': self.target_language,
            'directory_structure': {
                'base_path': str(self.translation_results_base),
                'structure_format': 'source_language/target_language/project_name',
                'this_batch_path': f"{self.source_language}/{self.target_language}"
            },
            'batch_statistics': {
                'total_projects_analyzed': self.batch_summary['total_projects'],
                'successful_projects': self.batch_summary['successful_projects'],
                'failed_projects': self.batch_summary['failed_projects'],
                'skipped_projects': self.batch_summary['skipped_projects'],
                'total_source_tests_translated': self.batch_summary['total_source_tests_translated'],
                'total_target_tests_created': self.batch_summary['total_target_tests_created'],
                'total_files_copied': self.batch_summary['total_files_copied'],
                'average_translation_ratio': self.batch_summary['average_translation_ratio']
            },
            'translation_distribution': self.batch_summary['translation_distribution'],
            'translation_metrics': self.batch_summary['translation_metrics'],
            'num_workers': self.batch_summary['num_workers'],
            'projects_data': projects_data
        }
        
        with open(summary_file, 'w', encoding='utf-8') as f:
            json.dump(summary_data, f, indent=2, ensure_ascii=False)
        
        # Also save a simple CSV for easy analysis
        with open(csv_file, 'w', encoding='utf-8') as f:
            f.write("project_name,source_language,target_language,translation_completed,source_original_tests,source_public_tests,target_original_tests,target_public_tests,project_structure_created,execution_time_seconds,iterations_completed,total_files_copied,result_location,worker_pid\n")
            for project in projects_data:
                f.write(f"{project['project_name']},{project['source_language']},{project['target_language']},{project['translation_completed']},{project['source_original_tests_count']},{project['source_public_tests_count']},{project['target_original_tests_count']},{project['target_public_tests_count']},{project['project_structure_created']},{project['execution_time_seconds']},{project['iterations_completed']},{project.get('total_files_copied', 0)},{project['result_location']},{project['worker_pid']}\n")
        
        logger.info(f"📊 Translation summary saved to: {summary_file}")
        logger.info(f"📊 Translation CSV saved to: {csv_file}")

    def save_batch_summary(self):
        """Save batch execution summary"""
        self.batch_summary['end_time'] = datetime.now().isoformat()
        
        # Calculate total execution time
        if 'start_time' in self.batch_summary:
            start = datetime.fromisoformat(self.batch_summary['start_time'])
            end = datetime.fromisoformat(self.batch_summary['end_time'])
            self.batch_summary['total_execution_time_seconds'] = (end - start).total_seconds()
        
        # Calculate translation statistics
        self.calculate_translation_statistics()
        
        # Save summary as JSON
        summary_file = self.batch_output_dir / "batch_summary.json"
        with open(summary_file, 'w', encoding='utf-8') as f:
            json.dump(self.batch_summary, f, indent=2, ensure_ascii=False)
        
        # Save human-readable summary
        summary_text_file = self.batch_output_dir / "batch_summary.txt"
        with open(summary_text_file, 'w', encoding='utf-8') as f:
            f.write("Batch Test Case Translation Pipeline Summary\n")
            f.write("=" * 50 + "\n")
            f.write(f"Start Time: {self.batch_summary['start_time']}\n")
            f.write(f"End Time: {self.batch_summary['end_time']}\n")
            f.write(f"Total Execution Time: {self.batch_summary.get('total_execution_time_seconds', 0):.2f} seconds\n")
            f.write(f"Source Language: {self.batch_summary['source_language']}\n")
            f.write(f"Target Language: {self.batch_summary['target_language']}\n")
            f.write(f"Max Projects per Language: {self.batch_summary['max_projects_per_language']}\n")
            f.write(f"Number of Workers: {self.batch_summary['num_workers']}\n")
            f.write(f"Results Directory Structure: {self.batch_summary['directory_structure_info']['structure_format']}\n")
            f.write(f"Results Base Path: {self.batch_summary['directory_structure_info']['base_path']}\n")
            f.write("\n")
            f.write(f"📊 RESULTS SUMMARY:\n")
            f.write(f"Total Projects: {self.batch_summary['total_projects']}\n")
            f.write(f"✅ Successful Projects: {self.batch_summary['successful_projects']}\n")
            f.write(f"❌ Failed Projects: {self.batch_summary['failed_projects']}\n")
            f.write(f"⏱️  Timeout Projects: {self.batch_summary['timeout_projects']}\n")
            f.write(f"⏸️  Interrupted Projects: {self.batch_summary['interrupted_projects']}\n")
            f.write(f"⏭️  Skipped Projects: {self.batch_summary['skipped_projects']}\n")
            
            if self.batch_summary['successful_projects'] > 0:
                f.write(f"📊 Total Source Tests Translated: {self.batch_summary['total_source_tests_translated']}\n")
                f.write(f"📊 Total Target Tests Created: {self.batch_summary['total_target_tests_created']}\n")
                f.write(f"📊 Total Files Copied: {self.batch_summary['total_files_copied']}\n")
                f.write(f"📊 Average Translation Ratio: {self.batch_summary['average_translation_ratio']:.2f}\n")
            
            if self.batch_summary['total_projects'] > 0:
                success_rate = (self.batch_summary['successful_projects'] / self.batch_summary['total_projects']) * 100
                f.write(f"\n📈 Success Rate: {success_rate:.2f}%\n")
            
            # Translation metrics
            if self.batch_summary['translation_metrics']:
                metrics = self.batch_summary['translation_metrics']
                f.write(f"\n🔄 TRANSLATION METRICS:\n")
                f.write(f"  Complete Translations: {metrics.get('complete_translations', 0)}\n")
                f.write(f"  Complete Translation Rate: {metrics.get('complete_translation_percentage', 0):.2f}%\n")
                f.write(f"  Projects with Structure Created: {metrics.get('projects_with_structure', 0)}\n")
                f.write(f"  Average Source Tests per Project: {metrics.get('average_source_tests_per_project', 0):.1f}\n")
                f.write(f"  Average Target Tests per Project: {metrics.get('average_target_tests_per_project', 0):.1f}\n")
                f.write(f"  Average Files Copied per Project: {metrics.get('average_files_copied_per_project', 0):.1f}\n")
            
            # Translation distribution
            if self.batch_summary['translation_distribution']:
                f.write(f"\n📊 TRANSLATION DISTRIBUTION (Target Tests Created):\n")
                for range_name, count in self.batch_summary['translation_distribution'].items():
                    f.write(f"  {range_name}: {count} projects\n")
            
            # Show detailed results
            f.write(f"\n📁 DETAILED RESULTS:\n")
            f.write("-" * 30 + "\n")
            
            successful_results = [r for r in self.batch_summary['results'] if r['status'] == 'success']
            for result in successful_results[:10]:  # Show first 10
                f.write(f"{result['project_name']}:\n")
                f.write(f"  Status: {result['status']}\n")
                f.write(f"  Location: {self.source_language}/{self.target_language}/{result['project_name']}\n")
                f.write(f"  Source Tests: {result['source_original_tests_count']} original + {result['source_public_tests_count']} public\n")
                f.write(f"  Target Tests: {result['target_original_tests_count']} original + {result['target_public_tests_count']} public\n")
                f.write(f"  Files Copied: {result.get('files_copied_summary', {}).get('total_files', 'N/A')}\n")
                f.write(f"  Execution Time: {result['execution_time_seconds']:.1f}s\n")
                f.write(f"  Iterations: {result['iterations_completed']}\n")
                f.write("\n")
            
            if len(successful_results) > 10:
                f.write(f"  ... and {len(successful_results) - 10} more successful projects\n")
            
            # Directory structure explanation
            f.write(f"\n📁 DIRECTORY STRUCTURE:\n")
            f.write(f"Translation results are organized as:\n")
            f.write(f"  {self.translation_results_base}/\n")
            f.write(f"  └── {self.source_language}/\n")
            f.write(f"      └── {self.target_language}/\n")
            f.write(f"          ├── project1/\n")
            f.write(f"          ├── project2/\n")
            f.write(f"          └── ...\n")
        
        # Save translation summary file
        self.save_translation_summary_file()
        
        logger.info(f"📋 Batch summary saved to: {summary_file}")

    def run(self):
        """Run batch test case translation pipeline with multiprocessing"""
        logger.info(f"🔄 Starting Batch Test Case Translation Pipeline")
        logger.info(f"Source Language: {self.source_language}")
        logger.info(f"Target Language: {self.target_language}")
        logger.info(f"Max projects per language: {self.max_projects_per_language}")
        logger.info(f"Number of workers: {self.num_workers}")
        logger.info(f"Resume mode: {self.args.resume}")
        logger.info(f"Retry failed: {self.args.retry_failed}")
        logger.info(f"Source repos: {self.source_repos_base}")
        logger.info(f"Translation results: {self.translation_results_base}")
        logger.info(f"Directory structure: {self.source_language}/{self.target_language}/project_name")
        logger.info(f"Batch output directory: {self.batch_output_dir}")
        logger.info("-" * 60)
        
        try:
            # Get all projects to process
            all_projects = self.discover_projects()
            print(all_projects[0].name)
            all_projects = [project for project in all_projects if project.name == "socialwifi_RouterOS-api"]
            print(len(all_projects))
            # exit()
            current_dir = f'translated_test_results/{self.args.source_language}/{self.args.target_language}'
            # if os.path.exists(current_dir):
            #     exclude_projects = os.listdir(current_dir)
            #     all_projects = [project for project in all_projects if project.name not in exclude_projects]

            if not all_projects:
                logger.warning("No projects found to translate!")
                return False
            
            # Prepare arguments for workers
            worker_args = []
            for project_path in all_projects:
                args_tuple = (
                    str(project_path),
                    self.source_language,
                    self.target_language,
                    self.args.model_name,
                    self.args.max_iterations,
                    self.args.timeout_per_command,
                    self.args.verbose
                )
                worker_args.append(args_tuple)
            
            logger.info(f"🚀 Starting translation of {len(worker_args)} projects with {self.num_workers} workers")
            
            # Process projects using multiprocessing
            completed_count = 0
            with ProcessPoolExecutor(max_workers=self.num_workers) as executor:
                # Submit all jobs
                future_to_args = {
                    executor.submit(process_single_translation_worker, args): args 
                    for args in worker_args
                }
                
                # Process completed futures
                for future in as_completed(future_to_args):
                    args = future_to_args[future]
                    project_path_str, source_language, target_language, _, _, _, _ = args
                    project_name = Path(project_path_str).name
                    project_key = f"{source_language}:{project_name}"
                    
                    try:
                        result = future.result()
                        completed_count += 1
                        
                        # Add result to batch summary
                        self.batch_summary['results'].append(result)
                        self.batch_summary['total_projects'] += 1
                        
                        # Update counters and tracking sets
                        if result['status'] == 'success':
                            self.batch_summary['successful_projects'] += 1
                            self.completed_projects.add(project_key)
                        elif result['status'] == 'interrupted':
                            self.batch_summary['interrupted_projects'] += 1
                        elif result['status'] == 'timeout':
                            self.batch_summary['timeout_projects'] += 1
                            self.failed_projects.add(project_key)
                        else:
                            self.batch_summary['failed_projects'] += 1
                            self.failed_projects.add(project_key)
                        
                        # Log progress with new directory structure info
                        translation_info = ""
                        if result['translation_completed']:
                            translation_info = (f" - Tests: {result['source_original_tests_count']+result['source_public_tests_count']} → "
                                              f"{result['target_original_tests_count']+result['target_public_tests_count']}")
                            if result['project_structure_created']:
                                translation_info += " ✅"
                            
                            files_copied = result.get('files_copied_summary', {}).get('total_files', 0)
                            if files_copied > 0:
                                translation_info += f" ({files_copied} files)"
                        
                        progress_pct = (completed_count / len(worker_args)) * 100
                        result_location = f"{source_language}/{target_language}/{project_name}"
                        logger.info(f"[{completed_count}/{len(worker_args)} {progress_pct:.1f}%] "
                                   f"Project {project_name}: {result['status']} "
                                   f"({result['execution_time_seconds']:.1f}s){translation_info}")
                        if result['status'] == 'success':
                            logger.info(f"  📁 Result location: {result_location}")
                        
                        # Save progress periodically
                        if completed_count % 10 == 0:
                            self.save_progress()
                            logger.info(f"💾 Progress saved at {completed_count}/{len(worker_args)} projects")
                        
                    except Exception as e:
                        logger.error(f"❌ Error processing {project_name}: {e}")
                        # Add failed result manually
                        error_result = {
                            'project_name': project_name,
                            'source_language': source_language,
                            'target_language': target_language,
                            'project_path': project_path_str,
                            'start_time': datetime.now().isoformat(),
                            'end_time': datetime.now().isoformat(),
                            'status': 'failed',
                            'error_message': str(e),
                            'execution_time_seconds': 0,
                            'iterations_completed': 0,
                            'translation_completed': False,
                            'source_original_tests_count': 0,
                            'source_public_tests_count': 0,
                            'target_original_tests_count': 0,
                            'target_public_tests_count': 0,
                            'project_structure_created': False,
                            'worker_pid': 'unknown',
                            'files_copied_summary': {
                                'target_test_files': 0,
                                'additional_target_files': 0,
                                'common_files': 0,
                                'total_files': 0
                            }
                        }
                        self.batch_summary['results'].append(error_result)
                        self.batch_summary['total_projects'] += 1
                        self.batch_summary['failed_projects'] += 1
                        self.failed_projects.add(project_key)
                        completed_count += 1
        
        except KeyboardInterrupt:
            logger.info("🛑 Batch translation interrupted by user")
        
        except Exception as e:
            logger.error(f"❌ Batch translation failed: {e}")
            logger.error(f"Full traceback: {traceback.format_exc()}")
        
        finally:
            # Always save final progress and summary
            self.save_progress()
            self.save_batch_summary()
            
            # Print final summary with new directory structure
            logger.info("🏁 Batch Test Case Translation Complete!")
            logger.info("=" * 50)
            logger.info(f"📊 FINAL RESULTS:")
            logger.info(f"Total Projects Processed: {self.batch_summary['total_projects']}")
            logger.info(f"✅ Successfully Translated: {self.batch_summary['successful_projects']}")
            logger.info(f"❌ Failed: {self.batch_summary['failed_projects']}")
            logger.info(f"⏱️  Timeout: {self.batch_summary['timeout_projects']}")
            logger.info(f"⏸️  Interrupted: {self.batch_summary['interrupted_projects']}")
            logger.info(f"⏭️  Skipped: {self.batch_summary['skipped_projects']}")
            
            if self.batch_summary['successful_projects'] > 0:
                logger.info(f"📊 Total Source Tests Translated: {self.batch_summary['total_source_tests_translated']}")
                logger.info(f"📊 Total Target Tests Created: {self.batch_summary['total_target_tests_created']}")
                logger.info(f"📊 Total Files Copied: {self.batch_summary['total_files_copied']}")
                logger.info(f"📊 Average Translation Ratio: {self.batch_summary['average_translation_ratio']:.2f}")
                
                if self.batch_summary['translation_metrics']:
                    metrics = self.batch_summary['translation_metrics']
                    logger.info(f"🎯 Complete Translations: {metrics.get('complete_translations', 0)}")
                    logger.info(f"🔄 Projects with Structure: {metrics.get('projects_with_structure', 0)}")
            
            if self.batch_summary['total_projects'] > 0:
                success_rate = (self.batch_summary['successful_projects'] / self.batch_summary['total_projects']) * 100
                logger.info(f"📈 Success Rate: {success_rate:.2f}%")
            
            logger.info(f"📁 Translation results saved to: {self.translation_results_base}")
            logger.info(f"📁 Directory structure: {self.source_language}/{self.target_language}/project_name")
            logger.info(f"💾 Progress file: {self.resume_file}")
            
            # Return final status for exit code
            return self.batch_summary['successful_projects'] > 0


def main():
    parser = argparse.ArgumentParser(description="Batch Test Case Translation Pipeline")
    parser.add_argument('--model_name', type=str, default="gpt-4.1", 
                       help="Model name to use (default: gpt-4.1)")
    parser.add_argument('--source_language', type=str, required=True,
                       help="Source language to translate from (e.g., Python)")
    parser.add_argument('--target_language', type=str, required=True,
                       help="Target language to translate to (e.g., Java)")
    parser.add_argument('--max_projects_per_language', type=int, default=0, 
                       help="Maximum number of projects to process (0 = no limit, default: 0)")
    parser.add_argument('--max_iterations', type=int, default=40, 
                       help="Maximum number of iterations per project (default: 10)")
    parser.add_argument('--timeout_per_command', type=int, default=30, 
                       help="Timeout per command in seconds (default: 30)")
    parser.add_argument('--num_workers', type=int, default=20, 
                       help="Number of worker processes (default: 20)")
    parser.add_argument('--verbose', action='store_true', 
                       help="Enable verbose logging")
    parser.add_argument('--resume', action='store_true', 
                       help="Resume from previous run using progress file")
    parser.add_argument('--retry_failed', action='store_true', 
                       help="Retry previously failed projects (only effective with --resume)")
    
    args = parser.parse_args()
    
    if args.verbose:
        logging.getLogger().setLevel(logging.DEBUG)
    
    # Validate languages
    supported_languages = ['Python', 'C++', 'JavaScript', 'C', 'Java', 'Matlab', 'C#', 'Rust', 'Go']
    if args.source_language not in supported_languages:
        logger.error(f"Unsupported source language: {args.source_language}. Supported: {', '.join(supported_languages)}")
        sys.exit(1)
    
    if args.target_language not in supported_languages:
        logger.error(f"Unsupported target language: {args.target_language}. Supported: {', '.join(supported_languages)}")
        sys.exit(1)
    
    if args.source_language == args.target_language:
        logger.error("Source and target languages cannot be the same")
        sys.exit(1)
    
    # Validate number of workers
    max_workers = min(args.num_workers, mp.cpu_count())
    if args.num_workers != max_workers:
        logger.warning(f"Reduced number of workers from {args.num_workers} to {max_workers} (CPU limit)")
        args.num_workers = max_workers
    
    logger.info(f"🚀 Starting Batch Test Case Translation Pipeline")
    logger.info(f"Source Language: {args.source_language}")
    logger.info(f"Target Language: {args.target_language}")
    logger.info(f"Max Projects: {'No limit' if args.max_projects_per_language == 0 else args.max_projects_per_language}")
    logger.info(f"Number of Workers: {args.num_workers}")
    logger.info(f"Model: {args.model_name}")
    logger.info(f"Max Iterations per Project: {args.max_iterations}")
    logger.info(f"Command Timeout: {args.timeout_per_command}s")
    logger.info(f"Resume Mode: {args.resume}")
    logger.info(f"Retry Failed: {args.retry_failed}")
    logger.info(f"Directory Structure: source_language/target_language/project_name")
    logger.info(f"Debug Log: test_translation_batch_debug.log")
    logger.info("-" * 60)
    
    try:
        logger.info(f"Loading tiktoken encoding...")
        encoding = tiktoken.encoding_for_model("gpt-4")
        logger.info(f"Encoding loaded successfully")
        
        # Create and run batch pipeline
        batch_pipeline = BatchTestTranslationPipeline(args, encoding)
        has_successful_translations = batch_pipeline.run()
        
        # Exit with appropriate code
        if has_successful_translations:
            logger.info("✅ Batch completed with some successful test case translations")
            sys.exit(0)
        else:
            logger.info("⚠️  Batch completed but no projects were successfully translated")
            sys.exit(1)
            
    except Exception as e:
        logger.error(f"💥 Batch translation pipeline failed with exception: {e}")
        logger.error(f"Full traceback: {traceback.format_exc()}")
        sys.exit(2)


if __name__ == "__main__":
    main()