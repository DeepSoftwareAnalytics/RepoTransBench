# file path: testcase_public_agent_batch/run.py

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

from testcase_public_agent_batch.generator import Generator
from testcase_public_agent_batch.prompts.system_prompts import get_public_test_prompt_for_language
from testcase_public_agent_batch.prompts.user_prompts import (
    generate_initial_public_test_prompt, 
    generate_public_test_followup_prompt,
    extract_finished_data
)
from testcase_public_agent_batch.utils import get_project_structure, truncate_content
import tiktoken

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


class PublicTestCaseGenerationPipeline:
    def __init__(self, args, encoding):
        self.args = args
        self.encoding = encoding
        self.generator = None
        
        # Original paths
        self.original_repo_path = Path(args.repo_path).resolve()
        self.runnable_filter_base = Path("verified_repos_v2").resolve()
        self.public_test_results_base = Path("public_test_results").resolve()
        
        self.repo_language = args.repo_language

        # Validate original repository path
        if not self.original_repo_path.exists():
            raise FileNotFoundError(f"Original repository path does not exist: {self.original_repo_path}")
        
        # Check if existing tests are available
        existing_tests = self.find_existing_test_files()
        if not existing_tests:
            logger.warning(f"No existing test files found in {self.original_repo_path}")
        else:
            logger.info(f"Found {len(existing_tests)} existing test files")
        
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
        self.test_results = {
            'existing_tests_found': len(existing_tests) > 0,
            'public_tests_generated': False,
            'existing_tests_passed': False,
            'public_tests_passed': False
        }
        self.finished_data = None
        
        # Use public test generation system prompt
        self.system_prompt = get_public_test_prompt_for_language(self.repo_language)
        
        # Setup output directory for saving conversation logs
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        repo_name = self.original_repo_path.name
        self.output_dir = Path("public_test_generation_logs") / f"{repo_name}_{timestamp}"
        
        self.setup_output_directory()
        self.initialize_generator()

    def find_existing_test_files(self) -> List[Path]:
        """Find existing test files in the repository"""
        test_patterns = [
            'test_*.py', '*_test.py', '*Test.java', '*.test.js', '*.spec.js',
            '*_test.cpp', '*_test.c', '*Test.cs', 'test_*.m', '*_test.m'
        ]
        
        existing_tests = []
        for pattern in test_patterns:
            for file in self.original_repo_path.glob(f'**/{pattern}'):
                # Skip excluded directories
                if any(excl in str(file) for excl in ['.git', 'node_modules', 'venv', '__pycache__']):
                    continue
                existing_tests.append(file)
        
        return existing_tests

    def cleanup_existing_temp_directories(self):
        """Clean up any existing temp directories from previous runs"""
        pass

    def setup_temp_environment(self):
        """Create temporary directory and copy project files"""
        # Create temp base directory if it doesn't exist
        temp_base_dir = Path("/workspace/temp_public_testcase")
        temp_base_dir.mkdir(parents=True, exist_ok=True)
        
        # Create temporary directory with timestamp for uniqueness
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        self.temp_dir = temp_base_dir / f"public_test_generation_{timestamp}"
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
        """Initialize the generator with public test generation prompt"""
        self.generator = Generator(self.args, logger, self.system_prompt)

    def save_public_test_results(self):
        """Save public test results to public_test_results directory"""
        try:
            # Determine relative path from runnable_filter
            relative_path = self.original_repo_path.relative_to(self.runnable_filter_base)
            result_destination = self.public_test_results_base / relative_path
            result_destination.parent.mkdir(parents=True, exist_ok=True)
            
            # Copy the project with public test data
            if result_destination.exists():
                shutil.rmtree(result_destination)
            shutil.copytree(self.repo_path, result_destination)
            
            # Collect original test files and scripts paths
            original_test_files = self.find_existing_test_files()
            original_test_paths = [str(f.relative_to(self.original_repo_path)) for f in original_test_files]
            
            # Check for original test scripts
            original_test_scripts = []
            for script_name in ['run_tests.sh', 'test.sh', 'run_test.sh']:
                script_path = self.original_repo_path / script_name
                if script_path.exists():
                    original_test_scripts.append(script_name)
            
            # Collect public test files paths (from created files)
            public_test_files = []
            public_test_scripts = []
            
            for created_file_path in self.created_files:
                file_path = Path(created_file_path)
                relative_file_path = file_path.relative_to(self.repo_path)
                
                # Check if it's a test file
                if any(indicator in str(relative_file_path).lower() for indicator in ['test', 'spec']):
                    if any(public_indicator in str(relative_file_path).lower() 
                           for public_indicator in ['public', '_public', 'public_']):
                        public_test_files.append(str(relative_file_path))
                
                # Check if it's a test script
                if str(relative_file_path).endswith('.sh'):
                    if any(public_indicator in str(relative_file_path).lower() 
                           for public_indicator in ['public', '_public', 'public_']):
                        public_test_scripts.append(str(relative_file_path))
            
            # Calculate token usage stats for this project
            token_stats = self.calculate_token_usage_stats()
            
            # Create comprehensive public test summary file
            public_test_summary = {
                'project_name': self.original_repo_path.name,
                'language': self.repo_language,
                'test_results': self.test_results,
                'timestamp': datetime.now().isoformat(),
                'original_path': str(self.original_repo_path),
                'public_test_result_path': str(result_destination),
                'iterations_completed': self.current_iteration,
                
                # Token usage statistics
                'token_usage_stats': token_stats,
                
                # Original test information
                'original_tests': {
                    'test_files': original_test_paths,
                    'test_scripts': original_test_scripts,
                    'total_original_test_files': len(original_test_paths),
                    'found_existing_tests': len(original_test_paths) > 0
                },
                
                # Public test information
                'public_tests': {
                    'test_files': public_test_files,
                    'test_scripts': public_test_scripts,
                    'total_public_test_files': len(public_test_files),
                    'generated_public_tests': len(public_test_files) > 0
                },
                
                # All created files during the process
                'created_files': [str(Path(f).relative_to(self.repo_path)) for f in self.created_files]
            }
            
            # Add finished_data information if available
            if self.finished_data:
                public_test_summary['finished_data'] = {
                    'tests_path': self.finished_data.get('tests_path', []),
                    'run_tests_path': self.finished_data.get('run_tests_path', 'run_tests.sh'),
                    'public_tests_path': self.finished_data.get('public_tests_path', []),
                    'run_public_tests_path': self.finished_data.get('run_public_tests_path', 'run_public_tests.sh')
                }
            
            summary_file = result_destination / "public_test_summary.json"
            with open(summary_file, 'w', encoding='utf-8') as f:
                json.dump(public_test_summary, f, indent=2, ensure_ascii=False)
            
            logger.info(f"✅ Public test results saved to: {result_destination}")
            logger.info(f"📊 Test results: {self.test_results}")
            logger.info(f"📁 Original tests: {len(original_test_paths)} files, {len(original_test_scripts)} scripts")
            logger.info(f"📁 Public tests: {len(public_test_files)} files, {len(public_test_scripts)} scripts")
            logger.info(f"🔢 Token usage: {token_stats}")
            return result_destination
            
        except Exception as e:
            logger.error(f"❌ Failed to save public test results: {e}")
            return None

    def calculate_token_usage_stats(self) -> Dict:
        """Calculate token usage statistics for monitoring"""
        if not self.encoding:
            return {'error': 'No encoding available for token calculation'}
        
        try:
            # Get current project structure
            project_structure = get_project_structure(self.repo_path)
            structure_tokens = len(self.encoding.encode(project_structure))
            
            # Count tokens in test files
            test_files_tokens = 0
            test_files_count = 0
            
            test_patterns = ['**/test_*.py', '**/*_test.py', '**/test_*.cpp', '**/test_*.c', '**/*Test.java']
            for pattern in test_patterns:
                for file in self.repo_path.glob(pattern):
                    if any(excl in str(file) for excl in ['.git', 'node_modules', 'venv']):
                        continue
                    try:
                        content = file.read_text(encoding='utf-8', errors='ignore')
                        test_files_tokens += len(self.encoding.encode(content))
                        test_files_count += 1
                    except:
                        continue
            
            # Count tokens in source files
            source_files_tokens = 0
            source_files_count = 0
            
            source_patterns = ['**/*.py', '**/*.cpp', '**/*.c', '**/*.java', '**/*.js']
            for pattern in source_patterns:
                for file in self.repo_path.glob(pattern):
                    if any(excl in str(file) for excl in ['.git', 'node_modules', 'venv', 'test_', '_test']):
                        continue
                    try:
                        content = file.read_text(encoding='utf-8', errors='ignore')
                        source_files_tokens += len(self.encoding.encode(content))
                        source_files_count += 1
                    except:
                        continue
            
            return {
                'project_structure_tokens': structure_tokens,
                'test_files_tokens': test_files_tokens,
                'test_files_count': test_files_count,
                'source_files_tokens': source_files_tokens,
                'source_files_count': source_files_count,
                'total_content_tokens': structure_tokens + test_files_tokens + source_files_tokens,
                'average_tokens_per_test_file': test_files_tokens // max(test_files_count, 1),
                'average_tokens_per_source_file': source_files_tokens // max(source_files_count, 1)
            }
        except Exception as e:
            return {'error': f'Failed to calculate token stats: {str(e)}'}

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
            
            # Token check for individual files
            if self.encoding:
                content_tokens = len(self.encoding.encode(content))
                if content_tokens > 8000:  # Warn about large files
                    logger.warning(f"Large file detected: {filename} ({content_tokens:,} tokens)")
            
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
        total_tokens = 0
        
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
                
                # Calculate token usage for this file
                if self.encoding:
                    file_tokens = len(self.encoding.encode(content))
                    total_tokens += file_tokens
                    results.append(f"✅ Created file: {filename} ({file_tokens:,} tokens)")
                else:
                    results.append(f"✅ Created file: {filename}")
                    
                logger.info(f"Created file: {filename}")
                
                # Track if public tests were generated
                if any(indicator in filename.lower() for indicator in ['public', '_public']):
                    self.test_results['public_tests_generated'] = True
                    
            except Exception as e:
                results.append(f"❌ Failed to create {filename}: {str(e)}")
                logger.error(f"Failed to create file {filename}: {e}")
                all_success = False
        
        if total_tokens > 0:
            results.append(f"📊 Total tokens created: {total_tokens:,}")
        
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
        finished_pattern = r'```finished\s*\n.*?\n```'
        if re.search(finished_pattern, text, re.DOTALL):
            return 'finished'
        
        failed_pattern = r'```status\s*\n\s*failed\s*\n```'
        if re.search(failed_pattern, text, re.IGNORECASE):
            return 'failed'
        
        success_pattern = r'```status\s*\n\s*success\s*\n```'
        if re.search(success_pattern, text, re.IGNORECASE):
            return 'success'
        
        return None

    def execute_command(self, command: str) -> Tuple[bool, str, str]:
        """Execute a single command with timeout"""
        if command == "exit":
            return False, "", ""
        try:
            logger.info(f"Executing command: {command}")
            
            # Use configured timeout for commands
            actual_timeout = self.timeout_per_command
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
                
                # Check if this was a test command and update test results
                if success and any(test_indicator in command.lower() for test_indicator in ['test', 'pytest', 'jest', 'junit']):
                    if 'public' in command.lower():
                        self.test_results['public_tests_passed'] = True
                    else:
                        self.test_results['existing_tests_passed'] = True
                
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
                    # Truncate very long outputs to save tokens
                    if len(output) > 2000:
                        output = output[:2000] + f"\n... (truncated, {len(stdout)} total chars)"
                    results.append(f"Output:\n{output}")
            else:
                results.append("❌ FAILED")
                
                if stderr.strip():
                    error_output = stderr.strip()
                    if len(error_output) > 1000:
                        error_output = error_output[:1000] + f"\n... (truncated, {len(stderr)} total chars)"
                    results.append(f"Error:\n{error_output}")
                if stdout.strip():
                    output = stdout.strip()
                    if len(output) > 1000:
                        output = output[:1000] + f"\n... (truncated, {len(stdout)} total chars)"
                    results.append(f"Output:\n{output}")
            
            results.append("")  # Empty line for separation
            time.sleep(0.1)
        
        return "\n".join(results), execution_details

    def extract_and_validate_response(self, response: str) -> dict:
        """Extract and validate model response"""
        commands = self.extract_bash_commands(response)
        files = self.extract_file_blocks(response)
        status = self.check_status(response)
        finished_data = extract_finished_data(response)
        
        # Calculate response token usage
        response_tokens = 0
        if self.encoding:
            response_tokens = len(self.encoding.encode(response))
        
        return {
            'commands': commands,
            'files': files,
            'status': status,
            'finished_data': finished_data,
            'original_response': response,
            'response_tokens': response_tokens
        }

    def save_iteration_files(self, iteration: int, user_input: str, model_output: str, 
                           commands: List[str], files: List[Dict[str, str]], 
                           execution_details: List[dict], status: Optional[str]):
        """Save detailed files for each iteration with token usage tracking"""
        logger.info(f"Saving iteration {iteration} files...")
        
        iteration_dir = self.output_dir / f"iteration_{iteration:02d}"
        iteration_dir.mkdir(parents=True, exist_ok=True)
        
        # Calculate token usage for this iteration
        input_tokens = len(self.encoding.encode(user_input)) if self.encoding else 0
        output_tokens = len(self.encoding.encode(model_output)) if self.encoding else 0
        
        # Save user input
        with open(iteration_dir / "user_input.txt", 'w', encoding='utf-8') as f:
            f.write(f"Iteration {iteration} - User Input ({input_tokens:,} tokens)\n")
            f.write("=" * 50 + "\n")
            f.write(f"Timestamp: {datetime.now().isoformat()}\n")
            f.write(f"Original Repository: {self.original_repo_path}\n")
            f.write(f"Working Directory: {self.repo_path}\n")
            f.write(f"Input Tokens: {input_tokens:,}\n\n")
            f.write(user_input)
        
        # Save model output
        with open(iteration_dir / "model_output.txt", 'w', encoding='utf-8') as f:
            f.write(f"Iteration {iteration} - Model Output ({output_tokens:,} tokens)\n")
            f.write("=" * 50 + "\n")
            f.write(f"Timestamp: {datetime.now().isoformat()}\n")
            f.write(f"Status: {status}\n")
            f.write(f"Test Results: {self.test_results}\n")
            f.write(f"Output Tokens: {output_tokens:,}\n")
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
                    file_tokens = len(self.encoding.encode(file_info['content'])) if self.encoding else 0
                    f.write(f"File: {file_info['filename']} ({file_tokens:,} tokens)\n")
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
                'test_results': self.test_results,
                'total_commands': len(commands),
                'total_files_created': len(files),
                'token_usage': {
                    'input_tokens': input_tokens,
                    'output_tokens': output_tokens,
                    'total_tokens': input_tokens + output_tokens
                },
                'execution_details': execution_details,
                'created_files': [{'filename': f['filename'], 'size': len(f['content'])} for f in files]
            }
            json.dump(execution_data, f, indent=2, ensure_ascii=False)
        
        logger.info(f"✅ Iteration {iteration} files saved successfully")
        logger.info(f"📊 Token usage - Input: {input_tokens:,}, Output: {output_tokens:,}, Total: {input_tokens + output_tokens:,}")

    def run(self):
        """Main public test case generation pipeline execution with token optimization"""
        retry_cnt = 0
        
        # Generate initial prompt with project structure
        logger.info("🔍 Generating initial prompt with token limits...")
        user_prompt = generate_initial_public_test_prompt(self.original_repo_path, self.repo_language, self.encoding)
        
        # Log initial token usage
        if self.encoding:
            initial_tokens = len(self.encoding.encode(user_prompt))
            logger.info(f"📊 Initial prompt tokens: {initial_tokens:,}")
        
        final_status = None
        
        try:
            while self.current_iteration < self.max_iterations:
                self.current_iteration += 1
                logger.info(f"=== Starting Public Test Generation Iteration {self.current_iteration}/{self.max_iterations} ===")
                
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
                    
                    logger.info("📤 Requesting response from model...")
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
                    
                    if response_data['response_tokens'] > 0:
                        logger.info(f"📊 Model response tokens: {response_data['response_tokens']:,}")
                    
                    # Handle file creation
                    files = response_data['files']
                    file_results = ""
                    if files:
                        logger.info(f"📁 Found {len(files)} file(s) to create")
                        success, file_results = self.create_files(files)
                    
                    # Handle command execution
                    commands = response_data['commands'] 
                    execution_details = []
                    execution_results = ""
                    if commands:
                        logger.info(f"⚡ Found {len(commands)} commands to execute")
                        execution_results, execution_details = self.execute_commands(commands)
                    
                    # Check for status decision and finished data
                    status = response_data['status']
                    finished_data = response_data['finished_data']
                    
                    if finished_data:
                        self.finished_data = finished_data
                        logger.info(f"🏁 Task completion data received: {json.dumps(finished_data)}")
                    
                    # Save iteration data
                    self.save_iteration_files(
                        self.current_iteration,
                        user_prompt,
                        response,
                        commands,
                        files,
                        execution_details,
                        status
                    )

                    # Handle status decisions
                    if status == 'failed':
                        logger.info("❌ Model decided public test generation failed")
                        final_status = 'failed'
                        break
                    elif status == 'success' or status == 'finished':
                        logger.info("🎉 Model reported public test generation completed successfully")
                        final_status = 'success'
                        # Save public test results
                        saved_path = self.save_public_test_results()
                        if saved_path:
                            logger.info(f"✅ Public test results saved to: {saved_path}")
                        break
                    
                    # Check if both existing and public tests are passing
                    if (self.test_results['existing_tests_passed'] and 
                        self.test_results['public_tests_passed']):
                        logger.info("🎯 Both existing and public tests are passing!")
                        final_status = 'success'
                        # Save public test results
                        saved_path = self.save_public_test_results()
                        if saved_path:
                            logger.info(f"✅ Public test results saved to: {saved_path}")
                        break
                        
                    # Prepare next iteration prompt if continuing
                    logger.info("🔄 Preparing next iteration prompt with token limits...")
                    user_prompt = generate_public_test_followup_prompt(
                        self.repo_path, 
                        self.repo_language, 
                        execution_results, 
                        file_results,
                        self.encoding
                    )
                    
                    # Log follow-up prompt token usage
                    if self.encoding:
                        followup_tokens = len(self.encoding.encode(user_prompt))
                        logger.info(f"📊 Follow-up prompt tokens: {followup_tokens:,}")
                    
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
                final_status = 'timeout'
        
        except KeyboardInterrupt:
            logger.info("Public test generation pipeline interrupted by user")
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
        logger.info(f"🏁 Public test generation pipeline completed with status: {final_status}")
        logger.info(f"📊 Final test results: {self.test_results}")
        
        return final_status, self.test_results


def main():
    parser = argparse.ArgumentParser(description="Public Test Case Generation Pipeline with Token Optimization")
    parser.add_argument('--model_name', type=str, default="gpt-4.1", help="Model name to use")
    parser.add_argument('--repo_path', type=str, default='runnable_filter/C/0x1abin_MultiTimer', help="Path to the repository to analyze")
    parser.add_argument('--repo_language', type=str, default='C', help="Language of the repository")                   
    parser.add_argument('--max_iterations', type=int, default=10, help="Maximum number of iterations (default: 10)")
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
    
    logger.info(f"🧪 Starting Public Test Case Generation Pipeline with Token Optimization")
    logger.info(f"Original Repository: {args.repo_path}")
    logger.info(f"Repository Language: {args.repo_language}")
    logger.info(f"Temporary Directory: /workspace/temp_public_testcase")
    logger.info(f"Model: {args.model_name}")
    logger.info(f"Max Iterations: {args.max_iterations}")
    logger.info(f"Command Timeout: {args.timeout_per_command}s")
    logger.info(f"Debug Log: public_test_case_generation_debug.log")
    logger.info("-" * 60)
    
    try:
        encoding = tiktoken.encoding_for_model("gpt-4")
        pipeline = PublicTestCaseGenerationPipeline(args, encoding)
        final_status, test_results = pipeline.run()
        
        # Exit with appropriate code
        if final_status == 'success':
            logger.info("✅ Public test generation pipeline completed successfully")
            logger.info(f"📊 Final test results: {test_results}")
            
            # Check if both existing and public tests passed
            if (test_results.get('existing_tests_passed', False) and 
                test_results.get('public_tests_passed', False)):
                logger.info("🎯 Both existing and public tests passed!")
                sys.exit(0)
            else:
                logger.info("⚠️ Not all tests passed successfully")
                sys.exit(1)
        elif final_status == 'failed':
            logger.info("❌ Public test generation pipeline failed")
            sys.exit(2)
        elif final_status == 'timeout':
            logger.info("⏱️ Public test generation pipeline timed out")
            sys.exit(3)
        else:
            logger.info("❓ Public test generation pipeline completed with errors or interruption")
            sys.exit(4)
            
    except Exception as e:
        logger.error(f"💥 Public test generation pipeline failed with exception: {e}")
        logger.error(f"Full traceback: {traceback.format_exc()}")
        sys.exit(5)


if __name__ == "__main__":
    main()