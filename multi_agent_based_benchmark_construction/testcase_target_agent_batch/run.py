# file path: testcase_target_agent_batch/run.py

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

from testcase_target_agent_batch.generator import Generator
from testcase_target_agent_batch.prompts.system_prompts import get_test_translation_prompt
from testcase_target_agent_batch.prompts.user_prompts import (
    generate_initial_test_translation_prompt, 
    generate_test_translation_followup_prompt,
    extract_finished_data
)
from testcase_target_agent_batch.utils import get_project_structure, truncate_content
import tiktoken

# Configure logging
logging.basicConfig(
    format='%(asctime)s - %(levelname)s - %(name)s - %(message)s',
    datefmt='%m/%d/%Y %H:%M:%S',
    level=logging.INFO,
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler('test_translation_debug.log')
    ]
)
logger = logging.getLogger(__name__)


class TestCaseTranslationPipeline:
    def __init__(self, args, encoding):
        self.args = args
        self.encoding = encoding
        self.generator = None
        
        # Original paths
        self.original_repo_path = Path(args.repo_path).resolve()
        self.source_repos_base = Path("source_projects").resolve()
        self.translation_results_base = Path("translated_test_results").resolve()
        
        self.source_language = args.source_language
        self.target_language = args.target_language

        # Validate original repository path
        if not self.original_repo_path.exists():
            raise FileNotFoundError(f"Original repository path does not exist: {self.original_repo_path}")
        
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
        self.finished_data = None
        
        # Use test translation system prompt
        self.system_prompt = get_test_translation_prompt(self.source_language, self.target_language)
        
        # Setup output directory for saving conversation logs
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        repo_name = self.original_repo_path.name
        self.output_dir = Path("test_translation_logs") / f"{repo_name}_{self.source_language}_to_{self.target_language}_{timestamp}"
        
        self.setup_output_directory()
        self.initialize_generator()

    def cleanup_existing_temp_directories(self):
        """Clean up any existing temp directories from previous runs"""
        pass

    def setup_temp_environment(self):
        """Create temporary directory and copy project files"""
        # Create temp base directory if it doesn't exist
        temp_base_dir = Path("/workspace/temp_translation")
        temp_base_dir.mkdir(parents=True, exist_ok=True)
        
        # Create temporary directory with timestamp for uniqueness
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        self.temp_dir = temp_base_dir / f"translation_{timestamp}"
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
        """Initialize the generator with test translation prompt"""
        self.generator = Generator(self.args, logger, self.system_prompt)

    def save_translation_results(self):
        """Save translation results to translated_test_results directory with improved structure"""
        try:
            # Create destination path: translated_test_results/source_lang/target_lang/project_name
            repo_name = self.original_repo_path.name
            result_destination = self.translation_results_base / self.source_language / self.target_language / repo_name
            result_destination.parent.mkdir(parents=True, exist_ok=True)
            
            # Create clean target language project structure
            if result_destination.exists():
                shutil.rmtree(result_destination)
            result_destination.mkdir(parents=True, exist_ok=True)
            
            # Collect all target language files from temp directory
            target_files_copied = 0
            
            if self.finished_data:
                # Get all test file lists
                target_original_tests = self.finished_data.get('target_original_tests', [])
                target_public_tests = self.finished_data.get('target_public_tests', [])
                all_target_tests = target_original_tests + target_public_tests
                
                logger.info(f"Copying {len(all_target_tests)} target test files")
                
                # Copy all target test files
                for test_file in all_target_tests:
                    source_file = self.repo_path / test_file
                    dest_file = result_destination / test_file
                    if source_file.exists():
                        dest_file.parent.mkdir(parents=True, exist_ok=True)
                        shutil.copy2(source_file, dest_file)
                        target_files_copied += 1
                        logger.debug(f"Copied target test: {test_file}")
                    else:
                        logger.warning(f"Target test file not found: {test_file}")
                
                # Copy project structure file
                project_structure_file = self.finished_data.get('project_structure', 'project_structure.txt')
                if project_structure_file:
                    source_structure = self.repo_path / project_structure_file
                    dest_structure = result_destination / project_structure_file
                    if source_structure.exists():
                        shutil.copy2(source_structure, dest_structure)
                        logger.debug(f"Copied project structure: {project_structure_file}")
                
                # **NEW: Copy run_tests.sh script with special handling**
                run_tests_script = self.finished_data.get('run_tests_script', 'run_tests.sh')
                if run_tests_script:
                    source_script = self.repo_path / run_tests_script
                    dest_script = result_destination / 'run_tests.sh'  # Always name it run_tests.sh
                    if source_script.exists():
                        shutil.copy2(source_script, dest_script)
                        # Make script executable
                        os.chmod(dest_script, 0o755)
                        logger.info(f"✅ Copied and made executable: {run_tests_script}")
                    else:
                        logger.warning(f"⚠️ Run tests script not found: {run_tests_script}")
            
            # Additionally, copy ALL target language files found in temp directory
            # This ensures we don't miss any generated files
            target_extensions = self.get_target_language_extensions()
            additional_files_copied = 0
            
            for ext in target_extensions:
                for file in self.repo_path.rglob(f'*{ext}'):
                    if file.is_file():
                        rel_path = file.relative_to(self.repo_path)
                        dest_file = result_destination / rel_path
                        
                        # Only copy if not already copied
                        if not dest_file.exists():
                            dest_file.parent.mkdir(parents=True, exist_ok=True)
                            shutil.copy2(file, dest_file)
                            additional_files_copied += 1
                            logger.debug(f"Copied additional target file: {rel_path}")
            
            # **NEW: Ensure run_tests.sh exists and is executable**
            run_tests_path = result_destination / 'run_tests.sh'
            if not run_tests_path.exists():
                # Look for run_tests.sh in temp directory
                for script_file in self.repo_path.rglob('run_tests.sh'):
                    if script_file.is_file():
                        shutil.copy2(script_file, run_tests_path)
                        os.chmod(run_tests_path, 0o755)
                        logger.info(f"✅ Found and copied run_tests.sh from: {script_file.relative_to(self.repo_path)}")
                        break
                else:
                    logger.warning(f"⚠️ run_tests.sh not found in translation results")
            
            # Make sure run_tests.sh is executable if it exists
            if run_tests_path.exists():
                os.chmod(run_tests_path, 0o755)
                logger.info(f"✅ run_tests.sh is executable")
            
            # Copy common project files (README, build files, etc.)
            common_files = [
                'README.md', 'README.txt', 'readme.md', 'Readme.md',
                '.gitignore', 'LICENSE', 'MANIFEST.in', 'requirements.txt',
                'CMakeLists.txt', 'Makefile', 'pom.xml', 'build.gradle',
                'package.json', 'Cargo.toml', 'go.mod', 'go.sum',
                # Add more build/config files
                'setup.py', 'pyproject.toml', 'build.xml', 'gradle.properties',
                'project.clj', 'deps.edn', 'composer.json', 'Gemfile'
            ]
            common_files_copied = 0
            
            for common_file in common_files:
                source_file = self.repo_path / common_file
                if source_file.exists():
                    dest_file = result_destination / common_file
                    if not dest_file.exists():  # Don't overwrite if already exists
                        shutil.copy2(source_file, dest_file)
                        common_files_copied += 1
                        logger.debug(f"Copied common file: {common_file}")
            
            # Copy any build/config directories that might be needed
            build_dirs = ['cmake', 'scripts', 'tools', 'configs', 'resources', 'assets', 'docs']
            build_dirs_copied = 0
            
            for build_dir in build_dirs:
                source_dir = self.repo_path / build_dir
                if source_dir.exists() and source_dir.is_dir():
                    dest_dir = result_destination / build_dir
                    if not dest_dir.exists():
                        shutil.copytree(source_dir, dest_dir)
                        build_dirs_copied += 1
                        logger.debug(f"Copied build directory: {build_dir}")
            
            # Create comprehensive translation summary
            translation_summary = {
                'project_name': repo_name,
                'source_language': self.source_language,
                'target_language': self.target_language,
                'timestamp': datetime.now().isoformat(),
                'original_path': str(self.original_repo_path),
                'translation_result_path': str(result_destination),
                'iterations_completed': self.current_iteration,
                'run_tests_script_available': run_tests_path.exists(),
                'run_tests_script_executable': run_tests_path.exists() and os.access(run_tests_path, os.X_OK),
                'files_copied_summary': {
                    'target_test_files': target_files_copied,
                    'additional_target_files': additional_files_copied,
                    'common_files': common_files_copied,
                    'build_directories': build_dirs_copied,
                    'total_files': target_files_copied + additional_files_copied + common_files_copied
                }
            }
            
            if self.finished_data:
                translation_summary.update({
                    'source_original_tests': self.finished_data.get('source_original_tests', []),
                    'source_public_tests': self.finished_data.get('source_public_tests', []),
                    'target_original_tests': self.finished_data.get('target_original_tests', []),
                    'target_public_tests': self.finished_data.get('target_public_tests', []),
                    'project_structure': self.finished_data.get('project_structure', 'project_structure.txt'),
                    'run_tests_script': self.finished_data.get('run_tests_script', 'run_tests.sh'),
                    'translation_stats': {
                        'source_tests_count': len(self.finished_data.get('source_original_tests', []) + self.finished_data.get('source_public_tests', [])),
                        'target_tests_count': len(self.finished_data.get('target_original_tests', []) + self.finished_data.get('target_public_tests', [])),
                        'source_original_count': len(self.finished_data.get('source_original_tests', [])),
                        'source_public_count': len(self.finished_data.get('source_public_tests', [])),
                        'target_original_count': len(self.finished_data.get('target_original_tests', [])),
                        'target_public_count': len(self.finished_data.get('target_public_tests', []))
                    }
                })
            
            # Save translation summary
            summary_file = result_destination / "translation_summary.json"
            with open(summary_file, 'w', encoding='utf-8') as f:
                json.dump(translation_summary, f, indent=2, ensure_ascii=False)
            
            # **NEW: Create a comprehensive testing guide**
            testing_guide_file = result_destination / "TESTING_GUIDE.md"
            with open(testing_guide_file, 'w', encoding='utf-8') as f:
                f.write(f"# Testing Guide for {repo_name}\n\n")
                f.write(f"**Project**: {repo_name}\n")
                f.write(f"**Language**: {self.target_language}\n")
                f.write(f"**Translated from**: {self.source_language}\n")
                f.write(f"**Generated on**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
                
                f.write("## Quick Start\n\n")
                if run_tests_path.exists():
                    f.write("### Run All Tests\n")
                    f.write("```bash\n./run_tests.sh\n```\n\n")
                    
                    f.write("### Run Specific Test Categories\n")
                    f.write("```bash\n")
                    f.write("# Run original tests only\n./run_tests.sh original\n\n")
                    f.write("# Run public tests only\n./run_tests.sh public\n\n")
                    f.write("# Run specific test file\n./run_tests.sh specific <test_file>\n\n")
                    f.write("# Clean and rebuild\n./run_tests.sh clean\n\n")
                    f.write("# Show help\n./run_tests.sh help\n")
                    f.write("```\n\n")
                else:
                    f.write("⚠️ **run_tests.sh not found**. You may need to run tests manually using your language's testing framework.\n\n")
                
                f.write("## Test Structure\n\n")
                if self.finished_data:
                    f.write(f"- **Original Tests**: {len(self.finished_data.get('target_original_tests', []))} files\n")
                    f.write(f"- **Public Tests**: {len(self.finished_data.get('target_public_tests', []))} files\n")
                    f.write(f"- **Total Test Files**: {len(self.finished_data.get('target_original_tests', []) + self.finished_data.get('target_public_tests', []))}\n\n")
                    
                    if self.finished_data.get('target_original_tests'):
                        f.write("### Original Test Files\n")
                        for test_file in self.finished_data.get('target_original_tests', []):
                            f.write(f"- `{test_file}`\n")
                        f.write("\n")
                    
                    if self.finished_data.get('target_public_tests'):
                        f.write("### Public Test Files\n")
                        for test_file in self.finished_data.get('target_public_tests', []):
                            f.write(f"- `{test_file}`\n")
                        f.write("\n")
                
                f.write("## Manual Testing Commands\n\n")
                f.write(f"If you prefer to run tests manually using {self.target_language} tools:\n\n")
                
                # Add language-specific manual commands
                manual_commands = self.get_manual_test_commands()
                for command_desc, command in manual_commands.items():
                    f.write(f"### {command_desc}\n")
                    f.write(f"```bash\n{command}\n```\n\n")
                
                f.write("## Project Structure\n\n")
                if self.finished_data and self.finished_data.get('project_structure'):
                    structure_file = self.repo_path / self.finished_data.get('project_structure')
                    if structure_file.exists():
                        f.write("```\n")
                        f.write(structure_file.read_text(encoding='utf-8'))
                        f.write("\n```\n\n")
                
                f.write("## Notes\n\n")
                f.write("- This project was automatically translated from the original source code\n")
                f.write("- Test logic should be identical to the original tests\n")
                f.write("- If you encounter issues, check the translation_summary.json for details\n")
            
            # Create a simple directory listing for verification
            dir_listing_file = result_destination / "directory_listing.txt"
            with open(dir_listing_file, 'w', encoding='utf-8') as f:
                f.write(f"Translation Result Directory Listing\n")
                f.write(f"Generated on: {datetime.now().isoformat()}\n")
                f.write(f"Project: {repo_name}\n")
                f.write(f"Translation: {self.source_language} → {self.target_language}\n")
                f.write("=" * 60 + "\n\n")
                
                # List all files recursively
                for root, dirs, files in os.walk(result_destination):
                    level = root.replace(str(result_destination), '').count(os.sep)
                    indent = ' ' * 2 * level
                    f.write(f"{indent}{os.path.basename(root)}/\n")
                    subindent = ' ' * 2 * (level + 1)
                    for file in files:
                        if file not in ["directory_listing.txt", "TESTING_GUIDE.md"]:  # Don't list generated files
                            # Mark executable files
                            file_path = Path(root) / file
                            if os.access(file_path, os.X_OK):
                                f.write(f"{subindent}{file} (executable)\n")
                            else:
                                f.write(f"{subindent}{file}\n")
            
            logger.info(f"✅ Translation results saved to: {result_destination}")
            logger.info(f"📁 Directory structure: {self.source_language}/{self.target_language}/{repo_name}")
            logger.info(f"📊 Files copied: {target_files_copied} test files + {additional_files_copied} additional + {common_files_copied} common")
            
            if run_tests_path.exists():
                logger.info(f"🚀 Test script available: ./run_tests.sh")
                logger.info(f"📖 Testing guide created: TESTING_GUIDE.md")
            else:
                logger.warning(f"⚠️ No run_tests.sh found - manual testing required")
            
            return result_destination
            
        except Exception as e:
            logger.error(f"❌ Failed to save translation results: {e}")
            logger.error(f"Full traceback: {traceback.format_exc()}")
            return None

    def get_manual_test_commands(self):
        """Get manual test commands for different languages"""
        commands = {
            'Python': {
                'Install Dependencies': 'pip install -r requirements.txt && pip install pytest',
                'Run All Tests': 'pytest -v',
                'Run Original Tests': 'pytest tests/original/ -v',
                'Run Public Tests': 'pytest public_tests/ -v'
            },
            'Java': {
                'Compile Project': 'mvn clean compile test-compile',
                'Run All Tests': 'mvn test',
                'Run Original Tests': 'mvn test -Dtest="**/original/**/*Test"',
                'Run Public Tests': 'mvn test -Dtest="**/public_tests/**/*Test"'
            },
            'JavaScript': {
                'Install Dependencies': 'npm install',
                'Run All Tests': 'npm test',
                'Run Original Tests': 'npx jest tests/original',
                'Run Public Tests': 'npx jest public_tests'
            },
            'C++': {
                'Build Project': 'mkdir -p build && cd build && cmake .. && make',
                'Run All Tests': './build/test_runner',
                'Run Original Tests': './build/test_runner --gtest_filter="*Original*"',
                'Run Public Tests': './build/test_runner --gtest_filter="*Public*"'
            },
            'C#': {
                'Restore Packages': 'dotnet restore',
                'Run All Tests': 'dotnet test',
                'Run Original Tests': 'dotnet test tests/original/',
                'Run Public Tests': 'dotnet test public_tests/'
            },
            'Rust': {
                'Run All Tests': 'cargo test',
                'Run Original Tests': 'cargo test --test original',
                'Run Public Tests': 'cargo test --test public'
            },
            'Go': {
                'Get Dependencies': 'go mod tidy',
                'Run All Tests': 'go test ./... -v',
                'Run Original Tests': 'go test ./tests/original/... -v',
                'Run Public Tests': 'go test ./public_tests/... -v'
            },
            'C': {
                'Build Tests': 'make clean && make',
                'Run All Tests': './build/test_all',
                'Run Original Tests': './build/test_original',
                'Run Public Tests': './build/test_public'
            },
            'Matlab': {
                'Run All Tests': 'matlab -batch "runtests; exit"',
                'Run Original Tests': 'matlab -batch "runtests(\'tests/original\'); exit"',
                'Run Public Tests': 'matlab -batch "runtests(\'public_tests\'); exit"'
            }
        }
        
        return commands.get(self.target_language, {
            'Run Tests': f'# Please use appropriate {self.target_language} testing commands'
        })
    
    def get_target_language_extensions(self):
        """Get file extensions for target language"""
        extensions = {
            'Python': ['.py', '.pyi', '.pyx'],
            'Java': ['.java'],
            'C++': ['.cpp', '.hpp', '.cc', '.h', '.cxx', '.hxx'],
            'Rust': ['.rs'],
            'Go': ['.go'],
            'C#': ['.cs'],
            'C': ['.c', '.h'],
            'JavaScript': ['.js', '.jsx', '.ts', '.tsx', '.mjs'],
            'Matlab': ['.m', '.mlx']
        }
        return extensions.get(self.target_language, [])

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
        finished_data = extract_finished_data(response)
        
        return {
            'commands': commands,
            'files': files,
            'status': status,
            'finished_data': finished_data,
            'original_response': response
        }

    def save_iteration_files(self, iteration: int, user_input: str, model_output: str, 
                           commands: List[str], files: List[Dict[str, str]], 
                           execution_details: List[dict], status: Optional[str]):
        """Save detailed files for each iteration"""
        logger.info(f"Saving iteration {iteration} files...")
        
        iteration_dir = self.output_dir / f"iteration_{iteration:02d}"
        iteration_dir.mkdir(parents=True, exist_ok=True)
        
        # Save user input
        with open(iteration_dir / "user_input.txt", 'w', encoding='utf-8') as f:
            f.write(f"Iteration {iteration} - User Input\n")
            f.write("=" * 50 + "\n")
            f.write(f"Timestamp: {datetime.now().isoformat()}\n")
            f.write(f"Source Language: {self.source_language}\n")
            f.write(f"Target Language: {self.target_language}\n")
            f.write(f"Original Repository: {self.original_repo_path}\n")
            f.write(f"Working Directory: {self.repo_path}\n\n")
            f.write(user_input)
        
        # Save model output
        with open(iteration_dir / "model_output.txt", 'w', encoding='utf-8') as f:
            f.write(f"Iteration {iteration} - Model Output\n")
            f.write("=" * 50 + "\n")
            f.write(f"Timestamp: {datetime.now().isoformat()}\n")
            f.write(f"Status: {status}\n")
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
                'source_language': self.source_language,
                'target_language': self.target_language,
                'original_repository': str(self.original_repo_path),
                'working_directory': str(self.repo_path),
                'status': status,
                'total_commands': len(commands),
                'total_files_created': len(files),
                'execution_details': execution_details,
                'created_files': [{'filename': f['filename'], 'size': len(f['content'])} for f in files]
            }
            json.dump(execution_data, f, indent=2, ensure_ascii=False)
        
        logger.info(f"✅ Iteration {iteration} files saved successfully")

    def run(self):
        """Main test case translation pipeline execution"""
        retry_cnt = 0
        
        # Generate initial prompt with project structure and all test code
        user_prompt = generate_initial_test_translation_prompt(
            self.original_repo_path, 
            self.source_language, 
            self.target_language, 
            self.encoding
        )
        
        final_status = None
        
        try:
            while self.current_iteration < self.max_iterations:
                self.current_iteration += 1
                logger.info(f"=== Starting Test Translation Iteration {self.current_iteration}/{self.max_iterations} ===")
                
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
                    
                    # Check for status decision and completion data
                    status = response_data['status']
                    finished_data = response_data['finished_data']
                    
                    if finished_data:
                        self.finished_data = finished_data
                        logger.info(f"🏁 Translation completion data received: {json.dumps(finished_data)}")
                        
                        # Validate that required fields are present
                        required_fields = ['source_original_tests', 'source_public_tests', 
                                         'target_original_tests', 'target_public_tests', 'project_structure']
                        if all(field in finished_data for field in required_fields):
                            logger.info("✅ All required translation data fields present")
                        else:
                            missing_fields = [field for field in required_fields if field not in finished_data]
                            logger.warning(f"⚠️ Missing translation data fields: {missing_fields}")
                    
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
                        logger.info("❌ Model decided test translation failed")
                        final_status = 'failed'
                        break
                    elif status == 'success' or status == 'finished':
                        logger.info("🎉 Model reported test translation completed successfully")
                        final_status = 'success'
                        # Save translation results
                        saved_path = self.save_translation_results()
                        if saved_path:
                            logger.info(f"✅ Translation results saved to: {saved_path}")
                        break
                        
                    # Prepare next iteration prompt if continuing
                    # **MODIFIED: Pass original_repo_path to the followup prompt**
                    user_prompt = generate_test_translation_followup_prompt(
                        self.repo_path, 
                        self.source_language, 
                        self.target_language, 
                        execution_results, 
                        file_results,
                        self.encoding,
                        original_repo_path=self.original_repo_path  # **NEW: Pass original repo path**
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
                final_status = 'timeout'
        
        except KeyboardInterrupt:
            logger.info("Test translation pipeline interrupted by user")
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
        logger.info(f"🏁 Test translation pipeline completed with status: {final_status}")
        
        return final_status, self.finished_data


def main():
    parser = argparse.ArgumentParser(description="Test Case Translation Pipeline")
    parser.add_argument('--model_name', type=str, default="gpt-4.1", help="Model name to use")
    parser.add_argument('--repo_path', type=str, default='verified_repos/verified_repos_plus/Python/2captcha-python', help="Path to the repository to analyze")
    parser.add_argument('--source_language', type=str, default='Python', help="Source language of the repository")
    parser.add_argument('--target_language', type=str, default='Java', help="Target language for translation")                 
    parser.add_argument('--max_iterations', type=int, default=10, help="Maximum number of iterations (default: 10)")
    parser.add_argument('--timeout_per_command', type=int, default=30, help="Timeout per command in seconds (default: 30)")
    parser.add_argument('--verbose', action='store_true', help="Enable verbose logging")
    args = parser.parse_args()
    
    if args.verbose:
        logging.getLogger().setLevel(logging.DEBUG)
    
    # Extract source language from path if not provided
    if args.repo_path and not args.source_language:
        path_parts = args.repo_path.split('/')
        if len(path_parts) >= 3:
            args.source_language = path_parts[2]  # verified_repos/verified_repos_plus/Language/Project
    
    logger.info(f"🔄 Starting Test Case Translation Pipeline")
    logger.info(f"Original Repository: {args.repo_path}")
    logger.info(f"Source Language: {args.source_language}")
    logger.info(f"Target Language: {args.target_language}")
    logger.info(f"Temporary Directory: /workspace/temp_translation")
    logger.info(f"Model: {args.model_name}")
    logger.info(f"Max Iterations: {args.max_iterations}")
    logger.info(f"Command Timeout: {args.timeout_per_command}s")
    logger.info(f"Debug Log: test_translation_debug.log")
    logger.info("-" * 60)
    
    try:
        encoding = tiktoken.encoding_for_model("gpt-4")
        pipeline = TestCaseTranslationPipeline(args, encoding)
        final_status, finished_data = pipeline.run()
        
        # Exit with appropriate code
        if final_status == 'success':
            logger.info("✅ Test translation pipeline completed successfully")
            
            # Check if all required translation data is present
            if finished_data and all(field in finished_data for field in 
                                   ['source_original_tests', 'source_public_tests', 
                                    'target_original_tests', 'target_public_tests', 'project_structure']):
                logger.info("🎯 All required translation data successfully generated!")
                logger.info(f"📁 Source Original Tests: {len(finished_data.get('source_original_tests', []))}")
                logger.info(f"📁 Source Public Tests: {len(finished_data.get('source_public_tests', []))}")
                logger.info(f"📁 Target Original Tests: {len(finished_data.get('target_original_tests', []))}")
                logger.info(f"📁 Target Public Tests: {len(finished_data.get('target_public_tests', []))}")
                sys.exit(0)
            else:
                logger.info("⚠️ Translation completed but some required data may be missing")
                sys.exit(1)
        elif final_status == 'failed':
            logger.info("❌ Test translation pipeline failed")
            sys.exit(2)
        elif final_status == 'timeout':
            logger.info("⏱️ Test translation pipeline timed out")
            sys.exit(3)
        else:
            logger.info("❓ Test translation pipeline completed with errors or interruption")
            sys.exit(4)
            
    except Exception as e:
        logger.error(f"💥 Test translation pipeline failed with exception: {e}")
        logger.error(f"Full traceback: {traceback.format_exc()}")
        sys.exit(5)


if __name__ == "__main__":
    main()