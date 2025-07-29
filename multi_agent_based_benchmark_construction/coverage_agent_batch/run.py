# file path: coverage_agent_batch/run.py

import argparse
import json
import logging
import os
import re
import shutil
import subprocess
import sys
import tempfile
import time
import traceback
from datetime import datetime
from pathlib import Path
from typing import List, Tuple, Optional, Dict
import signal

from coverage_agent_batch.generator import Generator
from coverage_agent_batch.prompts.system_prompts import get_coverage_detection_prompt_for_language
from coverage_agent_batch.prompts.user_prompts import (
    generate_initial_coverage_prompt, 
    generate_coverage_followup_prompt,
    extract_coverage_percentage
)
from runnable_agent_batch.utils import get_project_structure, truncate_content
import tiktoken

# Configure logging
logging.basicConfig(
    format='%(asctime)s - %(levelname)s - %(name)s - %(message)s',
    datefmt='%m/%d/%Y %H:%M:%S',
    level=logging.INFO,
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler('coverage_detection_debug.log')
    ]
)
logger = logging.getLogger(__name__)


class CoverageDetectionPipeline:
    def __init__(self, args, encoding):
        self.args = args
        self.encoding = encoding
        self.generator = None
        
        # Original paths
        self.original_repo_path = Path(args.repo_path).resolve()
        self.runnable_filter_base = Path("runnable_filter").resolve()
        self.coverage_results_base = Path("coverage_results").resolve()
        
        self.repo_language = args.repo_language

        # Validate original repository path
        if not self.original_repo_path.exists():
            raise FileNotFoundError(f"Original repository path does not exist: {self.original_repo_path}")
        
        # Validate run_tests.sh exists
        run_tests_path = self.original_repo_path / "run_tests.sh"
        if not run_tests_path.exists():
            raise FileNotFoundError(f"run_tests.sh not found in {self.original_repo_path}")
        
        # Clean up any existing temp directories before starting
        self.cleanup_existing_temp_directories()
        
        # Create temp directory and copy project
        self.temp_dir = None
        self.repo_path = None
        self.setup_temp_environment()
        
        self.max_iterations = args.max_iterations
        self.timeout_per_command = args.timeout_per_command
        self.current_iteration = 0
        self.execution_history = []
        self.created_files = []
        self.coverage_percentage = None
        
        # Use coverage detection system prompt
        self.system_prompt = get_coverage_detection_prompt_for_language(self.repo_language)
        
        # Setup output directory for saving conversation logs
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        repo_name = self.original_repo_path.name
        self.output_dir = Path("coverage_detection_logs") / f"{repo_name}_{timestamp}"
        
        self.setup_output_directory()
        self.initialize_generator()

    def cleanup_existing_temp_directories(self):
        """Clean up any existing temp directories from previous runs"""
        pass

    def setup_temp_environment(self):
        """Create temporary directory and copy project files"""
        # Create temp base directory if it doesn't exist
        temp_base_dir = Path("/workspace/temp")
        temp_base_dir.mkdir(parents=True, exist_ok=True)
        
        # Create temporary directory with timestamp for uniqueness
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        self.temp_dir = temp_base_dir / f"coverage_detection_{timestamp}"
        self.temp_dir.mkdir(parents=True, exist_ok=True)
                
        # Copy project to temp directory
        repo_name = self.original_repo_path.name
        self.repo_path = self.temp_dir / repo_name
        
        shutil.copytree(self.original_repo_path, self.repo_path)
        logger.info(f"Project copied to temporary directory: {self.repo_path}")
        
    def setup_output_directory(self):
        """Setup output directory for logs"""
        self.output_dir.mkdir(parents=True, exist_ok=True)

    def initialize_generator(self):
        """Initialize the generator with coverage detection prompt"""
        self.generator = Generator(self.args, logger, self.system_prompt)

    def save_coverage_results(self):
        """Save coverage results to coverage_results directory"""
        try:
            # Determine relative path from runnable_filter
            relative_path = self.original_repo_path.relative_to(self.runnable_filter_base)
            coverage_destination = self.coverage_results_base / relative_path
            coverage_destination.parent.mkdir(parents=True, exist_ok=True)
            
            # Copy the project with coverage data
            if coverage_destination.exists():
                shutil.rmtree(coverage_destination)
            shutil.copytree(self.repo_path, coverage_destination)
            
            # Create coverage summary file
            coverage_summary = {
                'project_name': self.original_repo_path.name,
                'language': self.repo_language,
                'coverage_percentage': self.coverage_percentage,
                'timestamp': datetime.now().isoformat(),
                'original_path': str(self.original_repo_path),
                'coverage_path': str(coverage_destination)
            }
            
            summary_file = coverage_destination / "coverage_summary.json"
            with open(summary_file, 'w', encoding='utf-8') as f:
                json.dump(coverage_summary, f, indent=2, ensure_ascii=False)
            
            logger.info(f"✅ Coverage results saved to: {coverage_destination}")
            logger.info(f"📊 Coverage percentage: {self.coverage_percentage}%")
            return coverage_destination
            
        except Exception as e:
            logger.error(f"❌ Failed to save coverage results: {e}")
            return None

    def get_current_project_structure(self) -> str:
        """Get current project structure from the working directory"""
        return get_project_structure(self.repo_path)

    def extract_file_blocks(self, text: str) -> List[Dict[str, str]]:
        """Extract file creation blocks from model response"""
        file_pattern = r'```file:([^\n]+)\n(.*?)```'
        matches = re.findall(file_pattern, text, re.DOTALL)
        
        files = []
        for filename, content in matches:
            filename = filename.strip()
            content = content.strip()
            files.append({
                'filename': filename,
                'content': content
            })
            logger.info(f"Found file block: {filename}")
        
        return files

    def create_files(self, files: List[Dict[str, str]]) -> Tuple[bool, str]:
        """Create files from file blocks"""
        if not files:
            return True, "No files to create"
        
        results = []
        all_success = True
        
        for file_info in files:
            filename = file_info['filename']
            content = file_info['content']
            
            try:
                file_path = self.repo_path / filename
                file_path.parent.mkdir(parents=True, exist_ok=True)
                
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(content)
                
                # Make shell scripts executable
                if filename.endswith('.sh'):
                    os.chmod(file_path, 0o755)
                
                self.created_files.append(str(file_path))
                results.append(f"✅ Created file: {filename}")
                logger.info(f"Created file: {filename}")
            except Exception as e:
                results.append(f"❌ Failed to create {filename}: {str(e)}")
                logger.error(f"Failed to create file {filename}: {e}")
                all_success = False
        
        return all_success, "\n".join(results)

    def extract_bash_commands(self, text: str) -> List[str]:
        """Extract bash commands from model response"""        
        bash_pattern = r'```bash\s*\n(.*?)\n```'
        matches = re.findall(bash_pattern, text, re.DOTALL)
        
        commands = []
        for match in matches:
            lines = [line.strip() for line in match.split('\n')]
            lines = [line for line in lines if line and not line.startswith('#')]
            commands.extend(lines)
        
        return commands

    def check_status(self, text: str) -> Optional[str]:
        """Check for status markers in the response"""
        failed_pattern = r'```status\s*\n\s*failed\s*\n```'
        if re.search(failed_pattern, text, re.IGNORECASE):
            return 'failed'
        
        success_pattern = r'```status\s*\n\s*success\s*\n```'
        if re.search(success_pattern, text, re.IGNORECASE):
            return 'success'
        
        return None

    def execute_command(self, command: str) -> Tuple[bool, str, str]:
        """Execute a single command with timeout"""
        try:
            logger.info(f"Executing command: {command}")
            
            actual_timeout = min(self.timeout_per_command, 30)  # Max 30 seconds for coverage
            command = f"timeout {actual_timeout} bash -c '{command}'"
            
            process = subprocess.Popen(
                command,
                shell=True,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=False,
                cwd=self.repo_path,
                stdin=subprocess.DEVNULL,
                preexec_fn=os.setsid if os.name != 'nt' else None
            )
            
            try:
                stdout_bytes, _ = process.communicate(timeout=actual_timeout + 2)
                return_code = process.returncode
                
                stdout = self._decode_bytes(stdout_bytes)
                
                if return_code == 124:  # timeout exit code
                    logger.warning(f"Command timed out after {actual_timeout} seconds")
                    return False, stdout, f"Command timed out after {actual_timeout} seconds"
                
                success = return_code == 0
                logger.info(f"Command {'succeeded' if success else 'failed'} with return code: {return_code}")
                
                return success, stdout, ""
                
            except subprocess.TimeoutExpired:
                logger.warning(f"Process timeout, killing process group...")
                
                try:
                    if os.name != 'nt':
                        os.killpg(os.getpgid(process.pid), signal.SIGTERM)
                        time.sleep(1)
                        try:
                            os.killpg(os.getpgid(process.pid), signal.SIGKILL)
                        except:
                            pass
                    else:
                        process.terminate()
                        time.sleep(1)
                        process.kill()
                except Exception as kill_error:
                    logger.error(f"Error killing process: {kill_error}")
                
                try:
                    stdout_bytes, _ = process.communicate(timeout=1)
                    stdout = self._decode_bytes(stdout_bytes)
                except:
                    stdout = ""
                
                error_msg = f"Command timed out and was killed after {actual_timeout} seconds"
                logger.warning(error_msg)
                return False, stdout, error_msg
                
        except Exception as e:
            error_msg = f"Failed to execute command: {str(e)}"
            logger.error(error_msg)
            return False, "", error_msg

    def _decode_bytes(self, data: bytes) -> str:
        """Try multiple encodings to decode bytes"""
        if not data:
            return ""
        
        encodings = ['utf-8', 'gbk', 'gb2312', 'latin-1', 'cp1252']
        
        for encoding in encodings:
            try:
                return data.decode(encoding)
            except UnicodeDecodeError:
                continue
        
        return data.decode('utf-8', errors='replace')

    def execute_commands(self, commands: List[str]) -> Tuple[str, List[dict]]:
        """Execute commands with better error handling"""
        if not commands:
            logger.info("No commands to execute")
            return "No commands found in the response.", []
        
        logger.info(f"Executing {len(commands)} command(s)")
        results = []
        execution_details = []
        results.append(f"Executing {len(commands)} command(s) from temp project directory: {self.repo_path}\n")
        
        for i, command in enumerate(commands, 1):
            results.append(f"=== Command {i}: {command} ===")
            
            # Skip dangerous commands
            if any(dangerous in command.lower() for dangerous in ['sudo', 'rm -rf', 'format', 'mkfs']):
                results.append("❌ SKIPPED - Potentially dangerous command")
                continue
            
            start_time = time.time()
            success, stdout, stderr = self.execute_command(command)
            execution_time = time.time() - start_time
            
            execution_detail = {
                'command_index': i,
                'command': command,
                'success': success,
                'stdout': stdout,
                'stderr': stderr,
                'execution_time': execution_time,
                'timestamp': datetime.now().isoformat()
            }
            execution_details.append(execution_detail)
            
            self.execution_history.append({
                'iteration': self.current_iteration,
                'command': command,
                'success': success,
                'stdout': stdout,
                'stderr': stderr
            })
            
            if success:
                results.append("✅ SUCCESS")
                if stdout.strip():
                    output = stdout.strip()
                    results.append(f"Output:\n{output}")
            else:
                results.append("❌ FAILED")
                
                if stderr.strip():
                    results.append(f"Error:\n{stderr}")
                if stdout.strip():
                    output = stdout.strip()
                    results.append(f"Output:\n{output}")
            
            results.append("")  # Empty line for separation
            time.sleep(0.1)
        
        return "\n".join(results), execution_details

    def extract_and_validate_response(self, response: str) -> dict:
        """Extract and validate model response"""
        commands = self.extract_bash_commands(response)
        files = self.extract_file_blocks(response)
        status = self.check_status(response)
        coverage_percentage = extract_coverage_percentage(response)
        
        return {
            'commands': commands,
            'files': files,
            'status': status,
            'coverage_percentage': coverage_percentage,
            'original_response': response
        }

    def save_iteration_files(self, iteration: int, user_input: str, model_output: str, 
                           commands: List[str], files: List[Dict[str, str]], 
                           execution_details: List[dict], status: Optional[str],
                           coverage_percentage: Optional[float]):
        """Save detailed files for each iteration"""
        logger.info(f"Saving iteration {iteration} files...")
        
        iteration_dir = self.output_dir / f"iteration_{iteration:02d}"
        iteration_dir.mkdir(parents=True, exist_ok=True)
        
        # Save user input
        with open(iteration_dir / "user_input.txt", 'w', encoding='utf-8') as f:
            f.write(f"Iteration {iteration} - User Input\n")
            f.write("=" * 50 + "\n")
            f.write(f"Timestamp: {datetime.now().isoformat()}\n")
            f.write(f"Original Repository: {self.original_repo_path}\n")
            f.write(f"Working Directory: {self.repo_path}\n\n")
            f.write(user_input)
        
        # Save model output
        with open(iteration_dir / "model_output.txt", 'w', encoding='utf-8') as f:
            f.write(f"Iteration {iteration} - Model Output\n")
            f.write("=" * 50 + "\n")
            f.write(f"Timestamp: {datetime.now().isoformat()}\n")
            f.write(f"Status: {status}\n")
            if coverage_percentage is not None:
                f.write(f"Coverage Percentage: {coverage_percentage}%\n")
            f.write("\n")
            f.write(model_output)
        
        # Save extracted commands
        with open(iteration_dir / "extracted_commands.txt", 'w', encoding='utf-8') as f:
            f.write(f"Iteration {iteration} - Extracted Commands\n")
            f.write("=" * 50 + "\n")
            f.write(f"Total Commands: {len(commands)}\n")
            f.write(f"Working Directory: {self.repo_path}\n\n")
            for i, cmd in enumerate(commands, 1):
                f.write(f"Command {i}: {cmd}\n")
        
        # Save created files info
        if files:
            with open(iteration_dir / "created_files.txt", 'w', encoding='utf-8') as f:
                f.write(f"Iteration {iteration} - Created Files\n")
                f.write("=" * 50 + "\n")
                f.write(f"Total Files: {len(files)}\n\n")
                for file_info in files:
                    f.write(f"File: {file_info['filename']}\n")
                    f.write("-" * 30 + "\n")
                    f.write(file_info['content'])
                    f.write("\n\n")
        
        # Save execution details as JSON
        with open(iteration_dir / "execution_details.json", 'w', encoding='utf-8') as f:
            execution_data = {
                'iteration': iteration,
                'timestamp': datetime.now().isoformat(),
                'original_repository': str(self.original_repo_path),
                'working_directory': str(self.repo_path),
                'status': status,
                'coverage_percentage': coverage_percentage,
                'total_commands': len(commands),
                'total_files_created': len(files),
                'execution_details': execution_details,
                'created_files': [{'filename': f['filename'], 'size': len(f['content'])} for f in files]
            }
            json.dump(execution_data, f, indent=2, ensure_ascii=False)
        
        logger.info(f"✅ Iteration {iteration} files saved successfully")

    def run(self):
        """Main coverage pipeline execution"""
        retry_cnt = 0
        
        # Generate initial prompt with project structure
        user_prompt = generate_initial_coverage_prompt(self.original_repo_path, self.repo_language, self.encoding)
        
        final_status = None
        
        try:
            while self.current_iteration < self.max_iterations:
                self.current_iteration += 1
                logger.info(f"=== Starting Coverage Iteration {self.current_iteration}/{self.max_iterations} ===")
                
                try:
                    # Manage conversation window
                    self.generator.slide_window_conversation()
                    self.generator.messages.append({
                        "role": "user", 
                        "content": [
                            {
                                "type": "text",
                                "text": user_prompt,
                            },
                        ]
                    })
                    
                    logger.info("Requesting response from model...")
                    response = self.generator.get_response(
                        repo_name=self.original_repo_path.name
                    )
                    
                    if response == -1:
                        retry_cnt += 1
                        if retry_cnt >= 3:
                            logger.error("Breaking loop due to too many retries")
                            final_status = 'failed'
                            break
                        logger.error("Failed to get response from model")
                        continue
                    
                    # Reset retry counter on successful response
                    retry_cnt = 0
                    
                    # Extract and validate response
                    response_data = self.extract_and_validate_response(response)
                    
                    # Handle file creation
                    files = response_data['files']
                    file_results = ""
                    if files:
                        logger.info(f"Found {len(files)} file(s) to create")
                        success, file_results = self.create_files(files)
                    
                    # Handle command execution
                    commands = response_data['commands'] 
                    execution_details = []
                    execution_results = ""
                    if commands:
                        logger.info(f"Found {len(commands)} commands to execute")
                        execution_results, execution_details = self.execute_commands(commands)
                    
                    # Check for status decision and coverage percentage
                    status = response_data['status']
                    coverage_percentage = response_data['coverage_percentage']
                    
                    if coverage_percentage is not None:
                        self.coverage_percentage = coverage_percentage
                        logger.info(f"📊 Extracted coverage percentage: {coverage_percentage}%")
                    
                    # Save iteration data
                    self.save_iteration_files(
                        self.current_iteration,
                        user_prompt,
                        response,
                        commands,
                        files,
                        execution_details,
                        status,
                        coverage_percentage
                    )

                    # Handle status decisions
                    if status == 'failed':
                        logger.info("❌ Model decided coverage analysis failed")
                        final_status = 'failed'
                        break
                    elif status == 'success':
                        logger.info("🎉 Model reported coverage analysis completed successfully")
                        final_status = 'success'
                        # Save coverage results
                        saved_path = self.save_coverage_results()
                        if saved_path:
                            logger.info(f"✅ Coverage results saved to: {saved_path}")
                        break
                        
                    # Prepare next iteration prompt if continuing
                    user_prompt = generate_coverage_followup_prompt(
                        self.repo_path, 
                        self.repo_language, 
                        execution_results, 
                        file_results,
                        self.encoding
                    )
                    
                except Exception as iteration_error:
                    logger.error(f"❌ Error in iteration {self.current_iteration}: {iteration_error}")
                    logger.error(f"Full traceback: {traceback.format_exc()}")
                    retry_cnt += 1
                    if retry_cnt >= 3:
                        logger.error("Breaking loop due to too many iteration errors")
                        final_status = 'failed'
                        break
                    continue
                
                time.sleep(1)
            
            # Handle timeout case
            if self.current_iteration >= self.max_iterations:
                logger.warning(f"Reached maximum iterations ({self.max_iterations})")
                final_status = 'failed'
        
        except KeyboardInterrupt:
            logger.info("Coverage pipeline interrupted by user")
            final_status = 'interrupted'
        
        finally:
            # Clean up temp directory
            if self.temp_dir and self.temp_dir.exists():
                pass
                # Uncomment if you want to clean up temp directories
                # try:
                #     shutil.rmtree(self.temp_dir)
                #     logger.info(f"Cleaned up temporary directory: {self.temp_dir}")
                # except Exception as e:
                #     logger.warning(f"Failed to cleanup temp directory: {e}")
        
        # Log final result
        logger.info(f"🏁 Coverage pipeline completed with status: {final_status}")
        if self.coverage_percentage is not None:
            logger.info(f"📊 Final coverage percentage: {self.coverage_percentage}%")
        
        return final_status, self.coverage_percentage


def main():
    parser = argparse.ArgumentParser(description="Coverage Detection and Analysis Pipeline")
    parser.add_argument('--model_name', type=str, default="claude-3-7-sonnet-20250219", help="Model name to use")
    parser.add_argument('--repo_path', type=str, default='runnable_filter/C/0x1abin_MultiTimer', help="Path to the repository to analyze")
    parser.add_argument('--repo_language', type=str, default='C', help="Language of the repository")                   
    parser.add_argument('--max_iterations', type=int, default=15, help="Maximum number of iterations (default: 15)")
    parser.add_argument('--timeout_per_command', type=int, default=30, help="Timeout per command in seconds (default: 30)")
    parser.add_argument('--verbose', action='store_true', help="Enable verbose logging")
    args = parser.parse_args()
    
    if args.verbose:
        logging.getLogger().setLevel(logging.DEBUG)
    
    # Extract repo language from path if not provided
    if args.repo_path:
        path_parts = args.repo_path.split('/')
        if len(path_parts) >= 2:
            args.repo_language = path_parts[1]
    
    logger.info(f"📊 Starting Coverage Detection and Analysis Pipeline")
    logger.info(f"Original Repository: {args.repo_path}")
    logger.info(f"Repository Language: {args.repo_language}")
    logger.info(f"Temporary Directory: /workspace/temp")
    logger.info(f"Model: {args.model_name}")
    logger.info(f"Max Iterations: {args.max_iterations}")
    logger.info(f"Command Timeout: {args.timeout_per_command}s")
    logger.info(f"Debug Log: coverage_detection_debug.log")
    logger.info("-" * 60)
    
    try:
        encoding = tiktoken.encoding_for_model("gpt-4")
        pipeline = CoverageDetectionPipeline(args, encoding)
        final_status, coverage_percentage = pipeline.run()
        
        # Exit with appropriate code
        if final_status == 'success':
            logger.info("✅ Coverage pipeline completed successfully")
            if coverage_percentage is not None:
                logger.info(f"📊 Coverage: {coverage_percentage}%")
            sys.exit(0)
        elif final_status == 'failed':
            logger.info("❌ Coverage pipeline failed - could not measure coverage")
            sys.exit(1)
        else:
            logger.info("❓ Coverage pipeline completed with errors or interruption")
            sys.exit(2)
            
    except Exception as e:
        logger.error(f"💥 Coverage pipeline failed with exception: {e}")
        logger.error(f"Full traceback: {traceback.format_exc()}")
        sys.exit(3)


if __name__ == "__main__":
    main()