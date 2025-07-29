# file path: coverage_agent_batch/run_batch.py

"""
Batch runner for coverage analysis pipeline across multiple languages and projects.
Analyzes projects from runnable_filter directory and generates coverage reports.
"""

import argparse
import logging
import os
import sys
import time
import traceback
from pathlib import Path
from datetime import datetime
from typing import List, Dict, Optional
import json
import tiktoken
from coverage_agent_batch.run import CoverageDetectionPipeline


# Configure logging
logging.basicConfig(
    format='%(asctime)s - %(levelname)s - %(name)s - %(message)s',
    datefmt='%m/%d/%Y %H:%M:%S',
    level=logging.INFO,
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler('batch_coverage_analysis_debug.log')
    ]
)
logger = logging.getLogger(__name__)


class BatchCoverageAnalysisPipeline:
    def __init__(self, args, encoding):
        self.args = args
        self.encoding = encoding
        self.languages = args.languages
        
        # Use runnable_filter as the source directory
        self.runnable_filter_base = Path("runnable_filter").resolve()
        self.coverage_results_base = Path("coverage_results").resolve()
        self.max_projects_per_language = args.max_projects_per_language
        self.batch_results = []
        
        # Validate runnable_filter directory exists
        if not self.runnable_filter_base.exists():
            raise FileNotFoundError(f"Runnable filter directory does not exist: {self.runnable_filter_base}")
        
        # Create coverage_results directory if it doesn't exist
        self.coverage_results_base.mkdir(parents=True, exist_ok=True)
        
        # Setup batch output directory
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        self.batch_output_dir = Path("batch_coverage_analysis_logs") / f"batch_run_{timestamp}"
        self.batch_output_dir.mkdir(parents=True, exist_ok=True)
        
        # Initialize batch summary
        self.batch_summary = {
            'start_time': datetime.now().isoformat(),
            'languages': self.languages,
            'max_projects_per_language': self.max_projects_per_language,
            'total_projects': 0,
            'successful_projects': 0,
            'failed_projects': 0,
            'interrupted_projects': 0,
            'average_coverage': 0.0,
            'coverage_distribution': {},
            'results': []
        }

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
                    # Check if run_tests.sh exists
                    run_tests_path = item / "run_tests.sh"
                    if run_tests_path.exists():
                        projects.append(item)
                    else:
                        logger.warning(f"Skipping {item.name} - no run_tests.sh found")
            
            # Sort projects by name for consistent ordering
            projects.sort(key=lambda x: x.name.lower())
            
            # Limit to max_projects_per_language
            if len(projects) > self.max_projects_per_language:
                original_count = len(projects)
                projects = projects[:self.max_projects_per_language]
                logger.info(f"Limited {language} projects to first {self.max_projects_per_language} out of {original_count} total")
            
            logger.info(f"Found {len(projects)} projects with run_tests.sh for language: {language}")
            return projects
            
        except Exception as e:
            logger.error(f"Error discovering projects for {language}: {e}")
            return []

    def run_single_project(self, project_path: Path, language: str) -> Dict:
        """Run coverage analysis pipeline for a single project"""
        project_name = project_path.name
        start_time = datetime.now()
        
        logger.info(f"📊 Starting coverage analysis for: {project_name} ({language})")
        
        result = {
            'project_name': project_name,
            'language': language,
            'project_path': str(project_path),
            'start_time': start_time.isoformat(),
            'status': 'unknown',  # 'success', 'failed', 'interrupted'
            'coverage_percentage': None,
            'error_message': None,
            'iterations_completed': 0,
            'execution_time_seconds': 0,
            'coverage_files_generated': False
        }
        
        try:
            # Create arguments for CoverageDetectionPipeline
            pipeline_args = argparse.Namespace(
                model_name=self.args.model_name,
                repo_path=str(project_path),
                repo_language=language,
                max_iterations=self.args.max_iterations,
                timeout_per_command=self.args.timeout_per_command,
                verbose=self.args.verbose
            )
            
            # Initialize and run pipeline
            pipeline = CoverageDetectionPipeline(pipeline_args, self.encoding)
            final_status, coverage_percentage = pipeline.run()
            
            # Record results based on pipeline status
            result['iterations_completed'] = pipeline.current_iteration
            result['status'] = final_status or 'failed'
            result['coverage_percentage'] = coverage_percentage
            
            if final_status == 'success' and coverage_percentage is not None:
                logger.info(f"✅ Successfully analyzed project: {project_name} - Coverage: {coverage_percentage}%")
                result['coverage_files_generated'] = True
            elif final_status == 'failed':
                logger.info(f"❌ Failed project: {project_name} (coverage analysis failed)")
            elif final_status == 'interrupted':
                logger.info(f"⏸️  Interrupted project: {project_name}")
            else:
                logger.warning(f"❓ Unknown status for project: {project_name}: {final_status}")

        except KeyboardInterrupt:
            logger.info("❌ Pipeline interrupted by user")
            result['error_message'] = "Interrupted by user"
            result['status'] = 'interrupted'
            raise  # Re-raise to stop batch processing
            
        except Exception as e:
            error_msg = f"Failed to process project {project_name}: {str(e)}"
            logger.error(f"❌ {error_msg}")
            logger.error(f"Full traceback: {traceback.format_exc()}")
            result['error_message'] = str(e)
            result['status'] = 'failed'
        
        finally:
            end_time = datetime.now()
            result['end_time'] = end_time.isoformat()
            result['execution_time_seconds'] = (end_time - start_time).total_seconds()
        
        return result

    def calculate_coverage_statistics(self):
        """Calculate coverage statistics from results"""
        successful_results = [r for r in self.batch_summary['results'] 
                            if r['status'] == 'success' and r['coverage_percentage'] is not None]
        
        if not successful_results:
            return
        
        # Calculate average coverage
        total_coverage = sum(r['coverage_percentage'] for r in successful_results)
        self.batch_summary['average_coverage'] = total_coverage / len(successful_results)
        
        # Calculate coverage distribution
        coverage_ranges = {
            '0-20%': 0, '20-40%': 0, '40-60%': 0, 
            '60-80%': 0, '80-100%': 0
        }
        
        for result in successful_results:
            coverage = result['coverage_percentage']
            if coverage < 20:
                coverage_ranges['0-20%'] += 1
            elif coverage < 40:
                coverage_ranges['20-40%'] += 1
            elif coverage < 60:
                coverage_ranges['40-60%'] += 1
            elif coverage < 80:
                coverage_ranges['60-80%'] += 1
            else:
                coverage_ranges['80-100%'] += 1
        
        self.batch_summary['coverage_distribution'] = coverage_ranges

    def save_coverage_summary_file(self):
        """Save a comprehensive coverage summary file"""
        summary_file = self.coverage_results_base / "coverage_summary_all_projects.json"
        
        # Prepare summary data
        projects_coverage = []
        successful_results = [r for r in self.batch_summary['results'] 
                            if r['status'] == 'success' and r['coverage_percentage'] is not None]
        
        for result in successful_results:
            projects_coverage.append({
                'project_name': result['project_name'],
                'language': result['language'],
                'coverage_percentage': result['coverage_percentage'],
                'execution_time_seconds': result['execution_time_seconds'],
                'project_path': result['project_path']
            })
        
        # Sort by coverage percentage (descending)
        projects_coverage.sort(key=lambda x: x['coverage_percentage'], reverse=True)
        
        summary_data = {
            'timestamp': datetime.now().isoformat(),
            'total_projects_analyzed': self.batch_summary['total_projects'],
            'successful_projects': self.batch_summary['successful_projects'],
            'average_coverage': self.batch_summary['average_coverage'],
            'coverage_distribution': self.batch_summary['coverage_distribution'],
            'languages_analyzed': self.batch_summary['languages'],
            'projects_coverage': projects_coverage
        }
        
        with open(summary_file, 'w', encoding='utf-8') as f:
            json.dump(summary_data, f, indent=2, ensure_ascii=False)
        
        # Also save a simple CSV for easy analysis
        csv_file = self.coverage_results_base / "coverage_summary.csv"
        with open(csv_file, 'w', encoding='utf-8') as f:
            f.write("project_name,language,coverage_percentage,execution_time_seconds\n")
            for project in projects_coverage:
                f.write(f"{project['project_name']},{project['language']},{project['coverage_percentage']},{project['execution_time_seconds']}\n")
        
        logger.info(f"📊 Coverage summary saved to: {summary_file}")
        logger.info(f"📊 Coverage CSV saved to: {csv_file}")

    def save_batch_summary(self):
        """Save batch execution summary"""
        self.batch_summary['end_time'] = datetime.now().isoformat()
        
        # Calculate total execution time
        if 'start_time' in self.batch_summary:
            start = datetime.fromisoformat(self.batch_summary['start_time'])
            end = datetime.fromisoformat(self.batch_summary['end_time'])
            self.batch_summary['total_execution_time_seconds'] = (end - start).total_seconds()
        
        # Calculate coverage statistics
        self.calculate_coverage_statistics()
        
        # Save summary as JSON
        summary_file = self.batch_output_dir / "batch_summary.json"
        with open(summary_file, 'w', encoding='utf-8') as f:
            json.dump(self.batch_summary, f, indent=2, ensure_ascii=False)
        
        # Save human-readable summary
        summary_text_file = self.batch_output_dir / "batch_summary.txt"
        with open(summary_text_file, 'w', encoding='utf-8') as f:
            f.write("Batch Coverage Analysis Pipeline Summary\n")
            f.write("=" * 50 + "\n")
            f.write(f"Start Time: {self.batch_summary['start_time']}\n")
            f.write(f"End Time: {self.batch_summary['end_time']}\n")
            f.write(f"Total Execution Time: {self.batch_summary.get('total_execution_time_seconds', 0):.2f} seconds\n")
            f.write(f"Languages: {', '.join(self.batch_summary['languages'])}\n")
            f.write(f"Max Projects per Language: {self.batch_summary['max_projects_per_language']}\n")
            f.write("\n")
            f.write(f"📊 RESULTS SUMMARY:\n")
            f.write(f"Total Projects: {self.batch_summary['total_projects']}\n")
            f.write(f"✅ Successful Projects: {self.batch_summary['successful_projects']}\n")
            f.write(f"❌ Failed Projects: {self.batch_summary['failed_projects']}\n")
            f.write(f"⏸️  Interrupted Projects: {self.batch_summary['interrupted_projects']}\n")
            
            if self.batch_summary['successful_projects'] > 0:
                f.write(f"📊 Average Coverage: {self.batch_summary['average_coverage']:.2f}%\n")
            
            if self.batch_summary['total_projects'] > 0:
                success_rate = (self.batch_summary['successful_projects'] / self.batch_summary['total_projects']) * 100
                f.write(f"\n📈 Success Rate: {success_rate:.2f}%\n")
            
            # Coverage distribution
            if self.batch_summary['coverage_distribution']:
                f.write(f"\n📊 COVERAGE DISTRIBUTION:\n")
                for range_name, count in self.batch_summary['coverage_distribution'].items():
                    f.write(f"  {range_name}: {count} projects\n")
            
            # Group results by language
            f.write(f"\n📁 RESULTS BY LANGUAGE:\n")
            f.write("-" * 30 + "\n")
            
            language_stats = {}
            for result in self.batch_summary['results']:
                lang = result['language']
                if lang not in language_stats:
                    language_stats[lang] = {'total': 0, 'success': 0, 'failed': 0, 'interrupted': 0, 'total_coverage': 0, 'success_with_coverage': 0}
                
                language_stats[lang]['total'] += 1
                if result['status'] == 'success':
                    language_stats[lang]['success'] += 1
                    if result['coverage_percentage'] is not None:
                        language_stats[lang]['total_coverage'] += result['coverage_percentage']
                        language_stats[lang]['success_with_coverage'] += 1
                elif result['status'] == 'interrupted':
                    language_stats[lang]['interrupted'] += 1
                else:
                    language_stats[lang]['failed'] += 1
            
            for lang, stats in language_stats.items():
                f.write(f"{lang}:\n")
                f.write(f"  Total: {stats['total']}\n")
                f.write(f"  ✅ Success: {stats['success']}\n") 
                f.write(f"  ❌ Failed: {stats['failed']}\n")
                f.write(f"  ⏸️  Interrupted: {stats['interrupted']}\n")
                if stats['success_with_coverage'] > 0:
                    avg_coverage = stats['total_coverage'] / stats['success_with_coverage']
                    f.write(f"  📊 Average Coverage: {avg_coverage:.2f}%\n")
                f.write("\n")
            
            f.write(f"📋 DETAILED RESULTS:\n")
            f.write("-" * 30 + "\n")
            
            for result in self.batch_summary['results']:
                status_emoji = {
                    'success': '✅',
                    'failed': '❌',
                    'interrupted': '⏸️ '
                }.get(result['status'], '❓')
                
                f.write(f"Project: {result['project_name']} ({result['language']})\n")
                f.write(f"  Status: {status_emoji} {result['status'].upper()}\n")
                f.write(f"  Iterations: {result['iterations_completed']}\n")
                f.write(f"  Execution Time: {result['execution_time_seconds']:.2f}s\n")
                if result['coverage_percentage'] is not None:
                    f.write(f"  📊 Coverage: {result['coverage_percentage']:.2f}%\n")
                if result['error_message']:
                    f.write(f"  Error: {result['error_message']}\n")
                f.write("\n")
        
        # Save coverage summary file
        self.save_coverage_summary_file()
        
        logger.info(f"📋 Batch summary saved to: {summary_file}")

    def run(self):
        """Run batch coverage analysis pipeline"""
        logger.info(f"📊 Starting Batch Coverage Analysis Pipeline")
        logger.info(f"Languages: {', '.join(self.languages)}")
        logger.info(f"Max projects per language: {self.max_projects_per_language}")
        logger.info(f"Runnable filter: {self.runnable_filter_base}")
        logger.info(f"Coverage results: {self.coverage_results_base}")
        logger.info(f"Batch output directory: {self.batch_output_dir}")
        logger.info("-" * 60)
        
        try:
            for language in self.languages:
                logger.info(f"📁 Processing language: {language}")
                
                # Discover projects for this language
                projects = self.discover_projects(language)
                if not projects:
                    logger.warning(f"No projects found for language: {language}")
                    continue
                
                # Process each project
                for i, project_path in enumerate(projects, 1):
                    logger.info(f"📦 Processing project {i}/{len(projects)} for {language}: {project_path.name}")
                    
                    result = self.run_single_project(project_path, language)
                    self.batch_results.append(result)
                    self.batch_summary['results'].append(result)
                    
                    # Update counters
                    self.batch_summary['total_projects'] += 1
                    if result['status'] == 'success':
                        self.batch_summary['successful_projects'] += 1
                    elif result['status'] == 'interrupted':
                        self.batch_summary['interrupted_projects'] += 1
                    else:
                        self.batch_summary['failed_projects'] += 1
                    
                    # Log intermediate progress
                    coverage_info = f" - Coverage: {result['coverage_percentage']:.2f}%" if result['coverage_percentage'] is not None else ""
                    logger.info(f"Project {project_path.name}: {result['status']} ({result['execution_time_seconds']:.1f}s){coverage_info}")
                    
                    # Add small delay between projects to avoid overwhelming the system
                    if i < len(projects):  # Don't sleep after the last project
                        time.sleep(2)
                
                logger.info(f"✅ Completed all projects for language: {language}")
                logger.info("-" * 40)
        
        except KeyboardInterrupt:
            logger.info("🛑 Batch processing interrupted by user")
        
        except Exception as e:
            logger.error(f"❌ Batch processing failed: {e}")
            logger.error(f"Full traceback: {traceback.format_exc()}")
        
        finally:
            # Always save summary, even if interrupted
            self.save_batch_summary()
            
            # Print final summary
            logger.info("🏁 Batch Coverage Analysis Complete!")
            logger.info("=" * 50)
            logger.info(f"📊 FINAL RESULTS:")
            logger.info(f"Total Projects Analyzed: {self.batch_summary['total_projects']}")
            logger.info(f"✅ Successfully Processed: {self.batch_summary['successful_projects']}")
            logger.info(f"❌ Failed: {self.batch_summary['failed_projects']}")
            logger.info(f"⏸️  Interrupted: {self.batch_summary['interrupted_projects']}")
            
            if self.batch_summary['successful_projects'] > 0:
                logger.info(f"📊 Average Coverage: {self.batch_summary['average_coverage']:.2f}%")
            
            if self.batch_summary['total_projects'] > 0:
                success_rate = (self.batch_summary['successful_projects'] / self.batch_summary['total_projects']) * 100
                logger.info(f"🎯 Success Rate: {success_rate:.2f}%")
            
            logger.info(f"📁 Coverage results saved to: {self.coverage_results_base}")
            
            # Return final status for exit code
            return self.batch_summary['successful_projects'] > 0


def main():
    parser = argparse.ArgumentParser(description="Batch Coverage Analysis Pipeline for Multiple Languages")
    parser.add_argument('--model_name', type=str, default="claude-3-7-sonnet-20250219", 
                       help="Model name to use (default: claude-3-7-sonnet-20250219)")
    parser.add_argument('--max_projects_per_language', type=int, default=10, 
                       help="Maximum number of projects to process per language (default: 10)")
    parser.add_argument('--max_iterations', type=int, default=15, 
                       help="Maximum number of iterations per project (default: 15)")
    parser.add_argument('--timeout_per_command', type=int, default=30, 
                       help="Timeout per command in seconds (default: 30)")
    parser.add_argument('--verbose', action='store_true', 
                       help="Enable verbose logging")
    parser.add_argument('--languages', nargs='+', 
                       default=['C', 'C#', 'C++', 'Java', 'JavaScript', 'Python', 'Matlab'],
                       help="Languages to process (default: all supported languages)")
    args = parser.parse_args()
    
    if args.verbose:
        logging.getLogger().setLevel(logging.DEBUG)
    
    logger.info(f"🚀 Starting Batch Coverage Analysis Pipeline")
    logger.info(f"Languages: {', '.join(args.languages)}")
    logger.info(f"Max Projects per Language: {args.max_projects_per_language}")
    logger.info(f"Model: {args.model_name}")
    logger.info(f"Max Iterations per Project: {args.max_iterations}")
    logger.info(f"Command Timeout: {args.timeout_per_command}s")
    logger.info(f"Debug Log: batch_coverage_analysis_debug.log")
    logger.info("-" * 60)
    
    try:
        logger.info(f"Loading tiktoken encoding...")
        encoding = tiktoken.encoding_for_model("gpt-4")
        logger.info(f"Encoding loaded successfully")
        
        # Create and run batch pipeline
        batch_pipeline = BatchCoverageAnalysisPipeline(args, encoding)
        has_successful_projects = batch_pipeline.run()
        
        # Exit with appropriate code
        if has_successful_projects:
            logger.info("✅ Batch completed with some successful coverage analyses")
            sys.exit(0)
        else:
            logger.info("⚠️  Batch completed but no projects were successfully analyzed")
            sys.exit(1)
            
    except Exception as e:
        logger.error(f"💥 Batch pipeline failed with exception: {e}")
        logger.error(f"Full traceback: {traceback.format_exc()}")
        sys.exit(2)


if __name__ == "__main__":
    main()