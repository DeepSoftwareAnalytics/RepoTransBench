# file path: testcase_public_agent_batch/run_batch.py

"""
Batch runner for public test case generation pipeline across multiple languages and projects.
Analyzes projects from runnable_filter directory and generates public test cases based on existing tests.
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
from testcase_public_agent_batch.run import PublicTestCaseGenerationPipeline


# Configure logging
logging.basicConfig(
    format='%(asctime)s - %(levelname)s - %(name)s - %(message)s',
    datefmt='%m/%d/%Y %H:%M:%S',
    level=logging.INFO,
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler('public_test_case_generation_debug.log')
    ]
)
logger = logging.getLogger(__name__)


# Worker function for multiprocessing
def process_single_project_worker(args_tuple: Tuple) -> Dict:
    """Worker function to process a single project in a separate process"""
    project_path_str, language, model_name, max_iterations, timeout_per_command, verbose = args_tuple
    
    # Setup logging for worker process
    process_logger = logging.getLogger(f'worker_{os.getpid()}')
    
    project_path = Path(project_path_str)
    project_name = project_path.name
    start_time = datetime.now()
    
    process_logger.info(f"🧪 [PID {os.getpid()}] Starting public test case generation for: {project_name} ({language})")
    
    result = {
        'project_name': project_name,
        'language': language,
        'project_path': str(project_path),
        'start_time': start_time.isoformat(),
        'status': 'unknown',  # 'success', 'failed', 'interrupted', 'timeout'
        'error_message': None,
        'iterations_completed': 0,
        'execution_time_seconds': 0,
        'existing_tests_found': False,
        'public_tests_generated': False,
        'run_tests_created': False,
        'run_public_tests_created': False,
        'existing_tests_passed': False,
        'public_tests_passed': False,
        'test_summary_path': None,
        'worker_pid': os.getpid()
    }
    
    try:
        # Load encoding in worker process
        encoding = tiktoken.encoding_for_model("gpt-4")
        
        # Create arguments for PublicTestCaseGenerationPipeline
        pipeline_args = argparse.Namespace(
            model_name=model_name,
            repo_path=str(project_path),
            repo_language=language,
            max_iterations=max_iterations,
            timeout_per_command=timeout_per_command,
            verbose=verbose
        )
        
        # Initialize and run pipeline
        pipeline = PublicTestCaseGenerationPipeline(pipeline_args, encoding)
        final_status, test_results = pipeline.run()
        
        # Record results based on pipeline status
        result['iterations_completed'] = pipeline.current_iteration
        result['status'] = final_status or 'failed'
        
        # Update test-related results
        if test_results:
            result['existing_tests_found'] = test_results.get('existing_tests_found', False)
            result['public_tests_generated'] = test_results.get('public_tests_generated', False)
            result['existing_tests_passed'] = test_results.get('existing_tests_passed', False)
            result['public_tests_passed'] = test_results.get('public_tests_passed', False)
        
        if final_status == 'success':
            process_logger.info(f"✅ [PID {os.getpid()}] Successfully generated public tests for project: {project_name}")
            
            # Check if test runner scripts were created
            run_tests_path = project_path / "run_tests.sh"
            run_public_tests_path = project_path / "run_public_tests.sh"
            if run_tests_path.exists():
                result['run_tests_created'] = True
            if run_public_tests_path.exists():
                result['run_public_tests_created'] = True
            
            # Record detailed test summary path if available
            if hasattr(pipeline, 'finished_data') and pipeline.finished_data:
                result['test_summary_path'] = {
                    'tests_path': pipeline.finished_data.get('tests_path', []),
                    'public_tests_path': pipeline.finished_data.get('public_tests_path', []),
                    'run_tests_path': pipeline.finished_data.get('run_tests_path', 'run_tests.sh'),
                    'run_public_tests_path': pipeline.finished_data.get('run_public_tests_path', 'run_public_tests.sh')
                }
            
            # Try to read the detailed summary file if it was created
            try:
                summary_file = project_path / "public_test_summary.json"
                if summary_file.exists():
                    with open(summary_file, 'r', encoding='utf-8') as f:
                        detailed_summary = json.load(f)
                        result['detailed_test_info'] = {
                            'original_tests': detailed_summary.get('original_tests', {}),
                            'public_tests': detailed_summary.get('public_tests', {}),
                            'created_files': detailed_summary.get('created_files', [])
                        }
            except Exception as e:
                process_logger.warning(f"Could not read detailed summary file: {e}")
                
        elif final_status == 'failed':
            process_logger.info(f"❌ [PID {os.getpid()}] Failed public test generation for project: {project_name}")
        elif final_status == 'timeout':
            process_logger.info(f"⏱️  [PID {os.getpid()}] Public test generation timed out for project: {project_name}")
        elif final_status == 'interrupted':
            process_logger.info(f"⏸️  [PID {os.getpid()}] Public test generation interrupted for project: {project_name}")
        else:
            process_logger.warning(f"❓ [PID {os.getpid()}] Unknown status for project: {project_name}: {final_status}")

    except KeyboardInterrupt:
        process_logger.info(f"❌ [PID {os.getpid()}] Pipeline interrupted by user")
        result['error_message'] = "Interrupted by user"
        result['status'] = 'interrupted'
        
    except Exception as e:
        error_msg = f"Failed to process project {project_name}: {str(e)}"
        process_logger.error(f"❌ [PID {os.getpid()}] {error_msg}")
        process_logger.error(f"Full traceback: {traceback.format_exc()}")
        result['error_message'] = str(e)
        result['status'] = 'failed'
    
    finally:
        end_time = datetime.now()
        result['end_time'] = end_time.isoformat()
        result['execution_time_seconds'] = (end_time - start_time).total_seconds()
        
        process_logger.info(f"🏁 [PID {os.getpid()}] Completed project {project_name} in {result['execution_time_seconds']:.1f}s")
    
    return result


class BatchPublicTestGenerationPipeline:
    def __init__(self, args, encoding):
        self.args = args
        self.encoding = encoding
        self.languages = args.languages
        self.num_workers = args.num_workers
        
        # Use runnable_filter as the source directory
        self.runnable_filter_base = Path("verified_repos_v2").resolve()
        self.public_test_results_base = Path("public_test_results").resolve()
        self.max_projects_per_language = args.max_projects_per_language
        self.batch_results = []
        
        # Validate runnable_filter directory exists
        if not self.runnable_filter_base.exists():
            raise FileNotFoundError(f"Runnable filter directory does not exist: {self.runnable_filter_base}")
        
        # Create public_test_results directory if it doesn't exist
        self.public_test_results_base.mkdir(parents=True, exist_ok=True)
        
        # Setup batch output directory
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        self.batch_output_dir = Path("batch_public_test_logs") / f"batch_run_{timestamp}"
        self.batch_output_dir.mkdir(parents=True, exist_ok=True)
        
        # Resume functionality
        self.resume_file = Path("batch_public_test_progress.pkl")
        self.completed_projects = set()
        self.failed_projects = set()
        
        # Initialize batch summary
        self.batch_summary = {
            'start_time': datetime.now().isoformat(),
            'languages': self.languages,
            'max_projects_per_language': self.max_projects_per_language,
            'num_workers': self.num_workers,
            'total_projects': 0,
            'successful_projects': 0,
            'failed_projects': 0,
            'interrupted_projects': 0,
            'timeout_projects': 0,
            'skipped_projects': 0,
            'public_test_metrics': {},
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

    def discover_projects(self, language: str) -> List[Path]:
        """Discover projects in a language directory"""
        language_dir = self.runnable_filter_base / language
        
        if not language_dir.exists():
            logger.warning(f"Language directory does not exist: {language_dir}")
            return []
        
        # Get all subdirectories (projects) in the language directory
        projects = []
        try:
            for item in language_dir.iterdir():
                if item.is_dir() and not item.name.startswith('.'):
                    project_key = f"{language}:{item.name}"
                    
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
                logger.info(f"Limited {language} projects to first {self.max_projects_per_language} out of {original_count} total")
            
            logger.info(f"Found {len(projects)} projects to process for public test generation in language: {language}")
            return projects
            
        except Exception as e:
            logger.error(f"Error discovering projects for {language}: {e}")
            return []

    def get_all_projects(self) -> List[Tuple[Path, str]]:
        """Get all projects across all languages"""
        all_projects = []
        for language in self.languages:
            projects = self.discover_projects(language)
            for project in projects:
                all_projects.append((project, language))
        
        logger.info(f"Total projects to process: {len(all_projects)}")
        return all_projects

    def calculate_public_test_statistics(self):
        """Calculate public test generation statistics from results"""
        successful_results = [r for r in self.batch_summary['results'] 
                            if r['status'] == 'success']
        
        if not successful_results:
            return
        
        # Calculate public test generation metrics
        projects_with_existing_tests = sum(1 for r in successful_results if r['existing_tests_found'])
        projects_with_public_tests = sum(1 for r in successful_results if r['public_tests_generated'])
        projects_existing_tests_passed = sum(1 for r in successful_results if r['existing_tests_passed'])
        projects_public_tests_passed = sum(1 for r in successful_results if r['public_tests_passed'])
        projects_with_run_tests = sum(1 for r in successful_results if r['run_tests_created'])
        projects_with_run_public_tests = sum(1 for r in successful_results if r['run_public_tests_created'])
        
        self.batch_summary['public_test_metrics'] = {
            'projects_with_existing_tests': projects_with_existing_tests,
            'projects_with_public_tests_generated': projects_with_public_tests,
            'projects_existing_tests_passed': projects_existing_tests_passed,
            'projects_public_tests_passed': projects_public_tests_passed,
            'projects_with_run_tests': projects_with_run_tests,
            'projects_with_run_public_tests': projects_with_run_public_tests,
            'public_test_success_rate': (projects_with_public_tests / len(successful_results)) * 100 if successful_results else 0,
            'both_tests_passed_rate': (projects_public_tests_passed / len(successful_results)) * 100 if successful_results else 0
        }

    def save_public_test_summary_file(self):
        """Save a comprehensive public test generation summary file"""
        summary_file = self.public_test_results_base / "public_test_summary_all_projects.json"
        
        # Prepare summary data
        projects_data = []
        successful_results = [r for r in self.batch_summary['results'] 
                            if r['status'] == 'success']
        
        for result in successful_results:
            project_data = {
                'project_name': result['project_name'],
                'language': result['language'],
                'existing_tests_found': result['existing_tests_found'],
                'public_tests_generated': result['public_tests_generated'],
                'existing_tests_passed': result['existing_tests_passed'],
                'public_tests_passed': result['public_tests_passed'],
                'run_tests_created': result['run_tests_created'],
                'run_public_tests_created': result['run_public_tests_created'],
                'execution_time_seconds': result['execution_time_seconds'],
                'iterations_completed': result['iterations_completed'],
                'project_path': result['project_path'],
                'worker_pid': result.get('worker_pid', 'N/A')
            }
            
            # Add detailed test information if available
            if 'detailed_test_info' in result:
                project_data['detailed_test_info'] = result['detailed_test_info']
            
            # Add test summary paths if available
            if 'test_summary_path' in result and result['test_summary_path']:
                project_data['test_summary_path'] = result['test_summary_path']
            
            projects_data.append(project_data)
        
        # Sort by project name
        projects_data.sort(key=lambda x: x['project_name'])
        
        summary_data = {
            'timestamp': datetime.now().isoformat(),
            'total_projects_analyzed': self.batch_summary['total_projects'],
            'successful_projects': self.batch_summary['successful_projects'],
            'failed_projects': self.batch_summary['failed_projects'],
            'skipped_projects': self.batch_summary['skipped_projects'],
            'public_test_metrics': self.batch_summary['public_test_metrics'],
            'languages_analyzed': self.batch_summary['languages'],
            'num_workers': self.batch_summary['num_workers'],
            'projects_data': projects_data
        }
        
        with open(summary_file, 'w', encoding='utf-8') as f:
            json.dump(summary_data, f, indent=2, ensure_ascii=False)
        
        # Also save a simple CSV for easy analysis
        csv_file = self.public_test_results_base / "public_test_summary.csv"
        with open(csv_file, 'w', encoding='utf-8') as f:
            f.write("project_name,language,existing_tests_found,public_tests_generated,existing_tests_passed,public_tests_passed,run_tests_created,run_public_tests_created,execution_time_seconds,iterations_completed,worker_pid,original_test_files_count,public_test_files_count\n")
            for project in projects_data:
                # Extract counts from detailed test info if available
                original_count = 0
                public_count = 0
                if 'detailed_test_info' in project:
                    original_count = project['detailed_test_info'].get('original_tests', {}).get('total_original_test_files', 0)
                    public_count = project['detailed_test_info'].get('public_tests', {}).get('total_public_test_files', 0)
                
                f.write(f"{project['project_name']},{project['language']},{project['existing_tests_found']},{project['public_tests_generated']},{project['existing_tests_passed']},{project['public_tests_passed']},{project['run_tests_created']},{project['run_public_tests_created']},{project['execution_time_seconds']},{project['iterations_completed']},{project['worker_pid']},{original_count},{public_count}\n")
        
        logger.info(f"📊 Public test summary saved to: {summary_file}")
        logger.info(f"📊 Public test CSV saved to: {csv_file}")

    def save_batch_summary(self):
        """Save batch execution summary"""
        self.batch_summary['end_time'] = datetime.now().isoformat()
        
        # Calculate total execution time
        if 'start_time' in self.batch_summary:
            start = datetime.fromisoformat(self.batch_summary['start_time'])
            end = datetime.fromisoformat(self.batch_summary['end_time'])
            self.batch_summary['total_execution_time_seconds'] = (end - start).total_seconds()
        
        # Calculate public test generation statistics
        self.calculate_public_test_statistics()
        
        # Save summary as JSON
        summary_file = self.batch_output_dir / "batch_summary.json"
        with open(summary_file, 'w', encoding='utf-8') as f:
            json.dump(self.batch_summary, f, indent=2, ensure_ascii=False)
        
        # Save human-readable summary
        summary_text_file = self.batch_output_dir / "batch_summary.txt"
        with open(summary_text_file, 'w', encoding='utf-8') as f:
            f.write("Batch Public Test Case Generation Pipeline Summary\n")
            f.write("=" * 50 + "\n")
            f.write(f"Start Time: {self.batch_summary['start_time']}\n")
            f.write(f"End Time: {self.batch_summary['end_time']}\n")
            f.write(f"Total Execution Time: {self.batch_summary.get('total_execution_time_seconds', 0):.2f} seconds\n")
            f.write(f"Languages: {', '.join(self.batch_summary['languages'])}\n")
            f.write(f"Max Projects per Language: {self.batch_summary['max_projects_per_language']}\n")
            f.write(f"Number of Workers: {self.batch_summary['num_workers']}\n")
            f.write("\n")
            f.write(f"📊 RESULTS SUMMARY:\n")
            f.write(f"Total Projects: {self.batch_summary['total_projects']}\n")
            f.write(f"✅ Successful Projects: {self.batch_summary['successful_projects']}\n")
            f.write(f"❌ Failed Projects: {self.batch_summary['failed_projects']}\n")
            f.write(f"⏱️  Timeout Projects: {self.batch_summary['timeout_projects']}\n")
            f.write(f"⏸️  Interrupted Projects: {self.batch_summary['interrupted_projects']}\n")
            f.write(f"⏭️  Skipped Projects: {self.batch_summary['skipped_projects']}\n")
            
            if self.batch_summary['total_projects'] > 0:
                success_rate = (self.batch_summary['successful_projects'] / self.batch_summary['total_projects']) * 100
                f.write(f"\n📈 Success Rate: {success_rate:.2f}%\n")
            
            # Public test generation metrics
            if self.batch_summary['public_test_metrics']:
                metrics = self.batch_summary['public_test_metrics']
                f.write(f"\n🧪 PUBLIC TEST GENERATION METRICS:\n")
                f.write(f"  Projects with Existing Tests Found: {metrics.get('projects_with_existing_tests', 0)}\n")
                f.write(f"  Projects with Public Tests Generated: {metrics.get('projects_with_public_tests_generated', 0)}\n")
                f.write(f"  Projects with Existing Tests Passed: {metrics.get('projects_existing_tests_passed', 0)}\n")
                f.write(f"  Projects with Public Tests Passed: {metrics.get('projects_public_tests_passed', 0)}\n")
                f.write(f"  Projects with run_tests.sh Created: {metrics.get('projects_with_run_tests', 0)}\n")
                f.write(f"  Projects with run_public_tests.sh Created: {metrics.get('projects_with_run_public_tests', 0)}\n")
                f.write(f"  Public Test Generation Success Rate: {metrics.get('public_test_success_rate', 0):.2f}%\n")
                f.write(f"  Both Tests Passed Rate: {metrics.get('both_tests_passed_rate', 0):.2f}%\n")
            
            # Group results by language
            f.write(f"\n📁 RESULTS BY LANGUAGE:\n")
            f.write("-" * 30 + "\n")
            
            language_stats = {}
            for result in self.batch_summary['results']:
                lang = result['language']
                if lang not in language_stats:
                    language_stats[lang] = {
                        'total': 0, 'success': 0, 'failed': 0, 'interrupted': 0, 'timeout': 0,
                        'existing_tests_found': 0, 'public_tests_generated': 0,
                        'existing_tests_passed': 0, 'public_tests_passed': 0
                    }
                
                language_stats[lang]['total'] += 1
                if result['status'] == 'success':
                    language_stats[lang]['success'] += 1
                    if result['existing_tests_found']:
                        language_stats[lang]['existing_tests_found'] += 1
                    if result['public_tests_generated']:
                        language_stats[lang]['public_tests_generated'] += 1
                    if result['existing_tests_passed']:
                        language_stats[lang]['existing_tests_passed'] += 1
                    if result['public_tests_passed']:
                        language_stats[lang]['public_tests_passed'] += 1
                elif result['status'] == 'interrupted':
                    language_stats[lang]['interrupted'] += 1
                elif result['status'] == 'timeout':
                    language_stats[lang]['timeout'] += 1
                else:
                    language_stats[lang]['failed'] += 1
            
            for lang, stats in language_stats.items():
                f.write(f"{lang}:\n")
                f.write(f"  Total: {stats['total']}\n")
                f.write(f"  ✅ Success: {stats['success']}\n") 
                f.write(f"  ❌ Failed: {stats['failed']}\n")
                f.write(f"  ⏱️  Timeout: {stats['timeout']}\n")
                f.write(f"  ⏸️  Interrupted: {stats['interrupted']}\n")
                if stats['success'] > 0:
                    f.write(f"  🔍 Existing Tests Found: {stats['existing_tests_found']}\n")
                    f.write(f"  🧪 Public Tests Generated: {stats['public_tests_generated']}\n")
                    f.write(f"  ✅ Existing Tests Passed: {stats['existing_tests_passed']}\n")
                    f.write(f"  ✅ Public Tests Passed: {stats['public_tests_passed']}\n")
                f.write("\n")
        
        # Save public test summary file
        self.save_public_test_summary_file()
        
        logger.info(f"📋 Batch summary saved to: {summary_file}")

    def run(self):
        """Run batch public test case generation pipeline with multiprocessing"""
        logger.info(f"🧪 Starting Batch Public Test Case Generation Pipeline")
        logger.info(f"Languages: {', '.join(self.languages)}")
        logger.info(f"Max projects per language: {self.max_projects_per_language}")
        logger.info(f"Number of workers: {self.num_workers}")
        logger.info(f"Resume mode: {self.args.resume}")
        logger.info(f"Retry failed: {self.args.retry_failed}")
        logger.info(f"Runnable filter: {self.runnable_filter_base}")
        logger.info(f"Public test results: {self.public_test_results_base}")
        logger.info(f"Batch output directory: {self.batch_output_dir}")
        logger.info("-" * 60)
        
        try:
            # Get all projects to process
            all_projects = self.get_all_projects()
            all_projects = all_projects[self.args.checkpoint:]
            current_dir = f'public_test_results/{self.languages[0]}'
            # print(all_projects[0][0].name)
            # print(os.listdir(current_dir)[0])
            # exit()
            # print(len(all_projects))
            if os.path.exists(current_dir):
                exclude_projects = os.listdir(current_dir)
                all_projects = [project for project in all_projects if project[0].name not in exclude_projects]
            # print(len(all_projects))
            # exit()

            if not all_projects:
                logger.warning("No projects found to process!")
                return False
            
            # Prepare arguments for workers
            worker_args = []
            for project_path, language in all_projects:
                args_tuple = (
                    str(project_path),
                    language,
                    self.args.model_name,
                    self.args.max_iterations,
                    self.args.timeout_per_command,
                    self.args.verbose
                )
                worker_args.append(args_tuple)
            
            logger.info(f"🚀 Starting processing of {len(worker_args)} projects with {self.num_workers} workers")
            
            # Process projects using multiprocessing
            completed_count = 0
            with ProcessPoolExecutor(max_workers=self.num_workers) as executor:
                # Submit all jobs
                future_to_args = {
                    executor.submit(process_single_project_worker, args): args 
                    for args in worker_args
                }
                
                # Process completed futures
                for future in as_completed(future_to_args):
                    args = future_to_args[future]
                    project_path_str, language, _, _, _, _ = args
                    project_name = Path(project_path_str).name
                    project_key = f"{language}:{project_name}"
                    
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
                        
                        # Log progress
                        test_info = ""
                        if result['status'] == 'success':
                            status_indicators = []
                            if result['existing_tests_found']:
                                status_indicators.append("📝 Existing")
                            if result['public_tests_generated']:
                                status_indicators.append("🧪 Public")
                            if result['existing_tests_passed']:
                                status_indicators.append("✅ ExistPass")
                            if result['public_tests_passed']:
                                status_indicators.append("✅ PublicPass")
                            test_info = f" - {' '.join(status_indicators)}" if status_indicators else ""
                        
                        progress_pct = (completed_count / len(worker_args)) * 100
                        logger.info(f"[{completed_count}/{len(worker_args)} {progress_pct:.1f}%] "
                                   f"Project {project_name} ({language}): {result['status']} "
                                   f"({result['execution_time_seconds']:.1f}s){test_info}")
                        
                        # Save progress periodically
                        if completed_count % 50 == 0:
                            self.save_progress()
                            logger.info(f"💾 Progress saved at {completed_count}/{len(worker_args)} projects")
                        
                    except Exception as e:
                        logger.error(f"❌ Error processing {project_name}: {e}")
                        # Add failed result manually
                        error_result = {
                            'project_name': project_name,
                            'language': language,
                            'project_path': project_path_str,
                            'start_time': datetime.now().isoformat(),
                            'end_time': datetime.now().isoformat(),
                            'status': 'failed',
                            'error_message': str(e),
                            'execution_time_seconds': 0,
                            'iterations_completed': 0,
                            'existing_tests_found': False,
                            'public_tests_generated': False,
                            'run_tests_created': False,
                            'run_public_tests_created': False,
                            'existing_tests_passed': False,
                            'public_tests_passed': False,
                            'worker_pid': 'unknown'
                        }
                        self.batch_summary['results'].append(error_result)
                        self.batch_summary['total_projects'] += 1
                        self.batch_summary['failed_projects'] += 1
                        self.failed_projects.add(project_key)
                        completed_count += 1
        
        except KeyboardInterrupt:
            logger.info("🛑 Batch processing interrupted by user")
        
        except Exception as e:
            logger.error(f"❌ Batch processing failed: {e}")
            logger.error(f"Full traceback: {traceback.format_exc()}")
        
        finally:
            # Always save final progress and summary
            self.save_progress()
            self.save_batch_summary()
            
            # Print final summary
            logger.info("🏁 Batch Public Test Case Generation Complete!")
            logger.info("=" * 50)
            logger.info(f"📊 FINAL RESULTS:")
            logger.info(f"Total Projects Analyzed: {self.batch_summary['total_projects']}")
            logger.info(f"✅ Successfully Processed: {self.batch_summary['successful_projects']}")
            logger.info(f"❌ Failed: {self.batch_summary['failed_projects']}")
            logger.info(f"⏱️  Timeout: {self.batch_summary['timeout_projects']}")
            logger.info(f"⏸️  Interrupted: {self.batch_summary['interrupted_projects']}")
            logger.info(f"⏭️  Skipped: {self.batch_summary['skipped_projects']}")
            
            if self.batch_summary['successful_projects'] > 0:
                if self.batch_summary['public_test_metrics']:
                    metrics = self.batch_summary['public_test_metrics']
                    logger.info(f"🧪 Public Tests Generated: {metrics.get('projects_with_public_tests_generated', 0)}")
                    logger.info(f"✅ Both Tests Passed: {metrics.get('projects_public_tests_passed', 0)}")
            
            if self.batch_summary['total_projects'] > 0:
                success_rate = (self.batch_summary['successful_projects'] / self.batch_summary['total_projects']) * 100
                logger.info(f"📈 Success Rate: {success_rate:.2f}%")
            
            logger.info(f"📁 Public test results saved to: {self.public_test_results_base}")
            logger.info(f"💾 Progress file: {self.resume_file}")
            
            # Return final status for exit code
            return self.batch_summary['successful_projects'] > 0


def main():
    parser = argparse.ArgumentParser(description="Batch Public Test Case Generation Pipeline for Multiple Languages")
    parser.add_argument('--model_name', type=str, default="gpt-4.1", 
                       help="Model name to use (default: gpt-4.1)")
    parser.add_argument('--max_projects_per_language', type=int, default=0, 
                       help="Maximum number of projects to process per language (0 = no limit, default: 0)")
    parser.add_argument('--max_iterations', type=int, default=20, 
                       help="Maximum number of iterations per project (default: 10)")
    parser.add_argument('--timeout_per_command', type=int, default=30, 
                       help="Timeout per command in seconds (default: 30)")
    parser.add_argument('--num_workers', type=int, default=10, 
                       help="Number of worker processes (default: 10)")
    parser.add_argument('--verbose', action='store_true', 
                       help="Enable verbose logging")
    parser.add_argument('--resume', action='store_true', 
                       help="Resume from previous run using progress file")
    parser.add_argument('--retry_failed', action='store_true', 
                       help="Retry previously failed projects (only effective with --resume)")
    parser.add_argument('--languages', nargs='+', 
                       default=['C', 'C#', 'C++', 'Java', 'JavaScript', 'Python', 'Matlab'],
                       help="Languages to process (default: all supported languages)")
    parser.add_argument('--checkpoint', type=int, default=0, help="")
    
    args = parser.parse_args()
    
    if args.verbose:
        logging.getLogger().setLevel(logging.DEBUG)
    
    # Validate number of workers
    max_workers = min(args.num_workers, mp.cpu_count())
    if args.num_workers != max_workers:
        logger.warning(f"Reduced number of workers from {args.num_workers} to {max_workers} (CPU limit)")
        args.num_workers = max_workers
    
    logger.info(f"🚀 Starting Batch Public Test Case Generation Pipeline")
    logger.info(f"Languages: {', '.join(args.languages)}")
    logger.info(f"Max Projects per Language: {'No limit' if args.max_projects_per_language == 0 else args.max_projects_per_language}")
    logger.info(f"Number of Workers: {args.num_workers}")
    logger.info(f"Model: {args.model_name}")
    logger.info(f"Max Iterations per Project: {args.max_iterations}")
    logger.info(f"Command Timeout: {args.timeout_per_command}s")
    logger.info(f"Resume Mode: {args.resume}")
    logger.info(f"Retry Failed: {args.retry_failed}")
    logger.info(f"Debug Log: public_test_case_generation_debug.log")
    logger.info("-" * 60)
    
    try:
        logger.info(f"Loading tiktoken encoding...")
        encoding = tiktoken.encoding_for_model("gpt-4")
        logger.info(f"Encoding loaded successfully")
        
        # Create and run batch pipeline
        batch_pipeline = BatchPublicTestGenerationPipeline(args, encoding)
        has_successful_projects = batch_pipeline.run()
        
        # Exit with appropriate code
        if has_successful_projects:
            logger.info("✅ Batch completed with some successful public test generations")
            sys.exit(0)
        else:
            logger.info("⚠️  Batch completed but no projects were successfully processed")
            sys.exit(1)
            
    except Exception as e:
        logger.error(f"💥 Batch pipeline failed with exception: {e}")
        logger.error(f"Full traceback: {traceback.format_exc()}")
        sys.exit(2)


if __name__ == "__main__":
    main()