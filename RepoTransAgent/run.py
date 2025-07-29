# file path: RepoTransAgent/run.py

import argparse
import logging
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path
from typing import Optional, Tuple
from datetime import datetime

from RepoTransAgent.generator import Generator
from RepoTransAgent.prompts.system_prompt import get_react_translation_prompt
from RepoTransAgent.actions import AVAILABLE_ACTIONS, Action, CreateFile, ReadFile, ExecuteCommand, SearchContent, Finished
from RepoTransAgent.test_analyzer import TestAnalyzer

# Configure logging
logging.basicConfig(
    format='%(asctime)s - %(levelname)s - %(message)s',
    level=logging.INFO,
    handlers=[logging.StreamHandler(sys.stdout)]
)
logger = logging.getLogger(__name__)


class ReactTranslationAgent:
    def __init__(self, args):
        self.args = args
        self.model_name = args.model_name
        self.project_name = args.project_name
        self.source_language = args.source_language
        self.target_language = args.target_language
        self.max_iterations = args.max_iterations
        self.current_iteration = 0
        
        # Set up paths
        self.working_path = Path(f"/workspace/translated_projects/{self.model_name.replace('/', '_')}/{self.source_language}/{self.target_language}/{self.project_name}").resolve()
        self.target_base_path = Path(f"/workspace/target_projects/{self.source_language}/{self.target_language}/{self.project_name}").resolve()
        
        # Setup directories
        self.setup_logging_directory()
        self.setup_working_directory()
        
        # Initialize components
        self.test_analyzer = TestAnalyzer(self.target_language)
        self.system_prompt = get_react_translation_prompt(self.source_language, self.target_language, self.project_name)
        self.generator = Generator(self.args, logger, self.system_prompt)
        
        # Save system prompt
        self.save_system_prompt()

    def setup_logging_directory(self):
        """Setup directory for saving conversation logs"""
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        self.log_dir = Path(f"logs/{self.model_name.replace('/', '_')}/{self.project_name}_{self.source_language}_to_{self.target_language}_{timestamp}")
        self.log_dir.mkdir(parents=True, exist_ok=True)
        logger.info(f"📁 Log directory: {self.log_dir}")

    def setup_working_directory(self):
        """Copy target project to working directory"""
        try:
            self.working_path.parent.mkdir(parents=True, exist_ok=True)
            if self.working_path.exists():
                shutil.rmtree(self.working_path)
            shutil.copytree(self.target_base_path, self.working_path)
            logger.info(f"✅ Working directory setup: {self.working_path}")
        except Exception as e:
            raise RuntimeError(f"Failed to setup working directory: {e}")

    def save_system_prompt(self):
        """Save system prompt to file"""
        system_prompt_file = self.log_dir / "system_prompt.txt"
        with open(system_prompt_file, 'w', encoding='utf-8') as f:
            f.write("=" * 80 + "\n")
            f.write("SYSTEM PROMPT\n")
            f.write("=" * 80 + "\n\n")
            f.write(self.system_prompt)
        logger.info(f"💾 System prompt saved to: {system_prompt_file}")

    def parse_action(self, output: str) -> Optional[Action]:
        """Parse action from model output"""
        if not output:
            return None
        
        # Extract action string from the response
        action_string = ""
        patterns = [
            r'Action:\s*(.*?)(?:Thought|Observation|$)',
            r'Action:\s*(.*?)$',
        ]
        
        for pattern in patterns:
            match = re.search(pattern, output, flags=re.DOTALL)
            if match:
                action_string = match.group(1).strip()
                break
        
        if not action_string:
            action_string = output.strip()
        
        # Try to parse with each action class
        for action_cls in AVAILABLE_ACTIONS:
            action = action_cls.parse_action_from_text(action_string)
            if action is not None:
                return action
        
        return None

    def execute_action(self, action: Action) -> Tuple[str, bool]:
        """Execute an action and return observation and done status"""
        try:
            if isinstance(action, CreateFile):
                return self._execute_create_file(action)
            elif isinstance(action, ReadFile):
                return self._execute_read_file(action)
            elif isinstance(action, ExecuteCommand):
                return self._execute_command(action)
            elif isinstance(action, SearchContent):
                return self._execute_search_content(action)
            elif isinstance(action, Finished):
                return self._execute_finished(action)
            else:
                return f"❌ Unknown action type: {type(action)}", False
        except Exception as e:
            logger.error(f"Error executing action: {e}")
            return f"❌ Error executing action: {str(e)}", False

    def _execute_create_file(self, action: CreateFile) -> Tuple[str, bool]:
        """Execute CreateFile action"""
        try:
            # Handle paths
            if action.filepath.startswith('/workspace'):
                full_path = Path(action.filepath)
            else:
                full_path = self.working_path / action.filepath
            
            # Create directories if needed
            full_path.parent.mkdir(parents=True, exist_ok=True)
            
            # Write file
            with open(full_path, 'w', encoding='utf-8') as f:
                f.write(action.content)
            
            # Make scripts executable
            if action.filepath.endswith('.sh'):
                os.chmod(full_path, 0o755)
            
            logger.info(f"📝 Created file: {action.filepath}")
            return f"✅ Created file: {action.filepath} ({len(action.content)} characters)", False
        except Exception as e:
            return f"❌ Failed to create file {action.filepath}: {str(e)}", False

    def _execute_read_file(self, action: ReadFile) -> Tuple[str, bool]:
        """Execute ReadFile action"""
        try:
            # Handle paths
            if action.filepath.startswith('/workspace'):
                full_path = Path(action.filepath)
            else:
                full_path = self.working_path / action.filepath
            
            if not full_path.exists():
                return f"❌ File not found: {action.filepath}", False
            
            with open(full_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            logger.info(f"📖 Read file: {action.filepath}")
            return f"📄 File: {action.filepath}\n\n{content}", False
        except Exception as e:
            return f"❌ Failed to read file {action.filepath}: {str(e)}", False

    def _execute_command(self, action: ExecuteCommand) -> Tuple[str, bool]:
        """Execute ExecuteCommand action"""
        try:
            result = subprocess.run(
                action.command,
                shell=True,
                capture_output=True,
                text=True,
                cwd=self.working_path,
                timeout=60
            )
            
            output = f"💻 Command: {action.command}\n"
            output += f"📊 Return code: {result.returncode}\n\n"
            
            if result.stdout:
                output += f"📝 STDOUT:\n{result.stdout}\n\n"
            
            if result.stderr:
                output += f"⚠️ STDERR:\n{result.stderr}"
            
            return output, False
        except subprocess.TimeoutExpired:
            return f"⏱️ Command timed out: {action.command}", False
        except Exception as e:
            return f"❌ Command failed: {str(e)}", False

    def _execute_search_content(self, action: SearchContent) -> Tuple[str, bool]:
        """Execute SearchContent action"""
        try:
            matches = []
            
            for file_path in self.working_path.rglob('*'):
                if file_path.is_file() and self._is_text_file(file_path):
                    try:
                        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                            content = f.read()
                        
                        if action.keyword.lower() in content.lower():
                            rel_path = file_path.relative_to(self.working_path)
                            matches.append(f"📁 {rel_path}")
                            
                            if len(matches) >= 5:  # Limit matches
                                break
                    except:
                        continue
            
            if matches:
                result = f"🔍 Found '{action.keyword}' in: {', '.join(matches)}"
            else:
                result = f"🔍 No matches found for '{action.keyword}'"
            
            return result, False
        except Exception as e:
            return f"❌ Search failed: {str(e)}", False

    def _is_text_file(self, file_path: Path) -> bool:
        """Check if file is likely a text file"""
        text_extensions = {'.py', '.js', '.java', '.cpp', '.h', '.txt', '.md', '.json', '.xml', '.yml', '.yaml', '.sh', '.rs', '.go', '.cs'}
        return file_path.suffix.lower() in text_extensions and file_path.stat().st_size < 100000

    def _execute_finished(self, action: Finished) -> Tuple[str, bool]:
        """Execute Finished action"""
        success = action.status.lower() == 'success'
        icon = "🎉" if success else "❌"
        return f"{icon} Task finished with status: {action.status}", True

    def get_project_tree(self) -> str:
        """Get current project file tree"""
        try:
            result = subprocess.run(
                ["tree", str(self.working_path), "-L", "3"],
                capture_output=True,
                text=True,
                timeout=10
            )
            if result.returncode == 0:
                return f"📁 Current project structure:\n```\n{result.stdout}\n```"
            else:
                # Fallback to find
                result = subprocess.run(
                    ["find", str(self.working_path), "-type", "f"],
                    capture_output=True,
                    text=True,
                    timeout=10
                )
                if result.returncode == 0:
                    files = result.stdout.strip().split('\n')
                    rel_files = [str(Path(f).relative_to(self.working_path)) for f in files if f]
                    return f"📁 Current files:\n" + '\n'.join(rel_files[:20])
                
            return "📁 Could not get project structure"
        except Exception as e:
            return f"📁 Error getting project structure: {e}"

    def run_tests_and_analyze(self) -> Tuple[str, object]:
        """Run tests with detailed analysis"""
        try:
            # Run analysis
            analysis = self.test_analyzer.run_and_analyze(self.working_path)
            
            # Format results for display
            formatted_results = self.test_analyzer.format_results(analysis)
            
            return f"🧪 {self.target_language.upper()} Test Analysis:\n{formatted_results}", analysis
            
        except Exception as e:
            return f"🧪 Test analysis failed: {e}", None

    def save_conversation_turn(self, iteration: int, input_text: str, output_text: str, action: Optional[Action], observation: str, test_analysis=None):
        """Save each conversation turn to file"""
        turn_file = self.log_dir / f"turn_{iteration:02d}.txt"
        with open(turn_file, 'w', encoding='utf-8') as f:
            f.write("=" * 80 + "\n")
            f.write(f"TURN {iteration}\n")
            f.write("=" * 80 + "\n\n")
            
            f.write("-" * 40 + " INPUT " + "-" * 40 + "\n")
            f.write(input_text)
            f.write("\n\n")
            
            f.write("-" * 40 + " OUTPUT " + "-" * 39 + "\n")
            f.write(output_text)
            f.write("\n\n")
            
            f.write("-" * 40 + " ACTION " + "-" * 39 + "\n")
            if action:
                f.write(f"Action Type: {type(action).__name__}\n")
                f.write(f"Action Details: {action}\n")
            else:
                f.write("No valid action parsed\n")
            f.write("\n")
            
            # Add test analysis if available
            if test_analysis:
                f.write("-" * 38 + " TEST ANALYSIS " + "-" * 37 + "\n")
                f.write(f"Compilation: {'SUCCESS' if test_analysis.compilation.success else 'FAILED'}\n")
                f.write(f"Overall Tests: {test_analysis.passed_tests}/{test_analysis.total_tests} ({test_analysis.overall_pass_rate:.1f}%)\n")
                f.write(f"Module Tests: {test_analysis.passed_modules}/{test_analysis.total_modules} ({test_analysis.module_pass_rate:.1f}%)\n")
                
                if test_analysis.modules:
                    f.write("\nModule Details:\n")
                    for module_name, module_result in test_analysis.modules.items():
                        status = "PASS" if module_result.is_module_passed else "FAIL"
                        f.write(f"  {module_name}: {status} ({module_result.passed_tests}/{module_result.total_tests})\n")
                f.write("\n")
            
            f.write("-" * 39 + " OBSERVATION " + "-" * 36 + "\n")
            f.write(observation)
            f.write("\n")
        
        logger.info(f"💾 Turn {iteration} saved to: {turn_file}")

    def predict(self, obs: str) -> Tuple[str, Optional[Action]]:
        """Predict next action based on observation"""
        # Prepare messages
        self.generator.slide_window_conversation()
        self.generator.messages.append({
            "role": "user",
            "content": [{"type": "text", "text": obs}]
        })
        
        # Get response from model
        response = self.generator.get_response(repo_name=self.project_name)
        if response == -1:
            return "", None
        
        # Parse action
        action = self.parse_action(response)
        
        return response, action

    def save_final_summary(self, status: str):
        """Save final summary of the translation process"""
        summary_file = self.log_dir / "final_summary.txt"
        
        # Get final test analysis
        final_test_results, final_analysis = self.run_tests_and_analyze()
        
        with open(summary_file, 'w', encoding='utf-8') as f:
            f.write("=" * 80 + "\n")
            f.write("FINAL SUMMARY\n")
            f.write("=" * 80 + "\n\n")
            f.write(f"Project: {self.project_name}\n")
            f.write(f"Translation: {self.source_language} → {self.target_language}\n")
            f.write(f"Final Status: {status}\n")
            f.write(f"Total Iterations: {self.current_iteration}\n")
            f.write(f"Max Iterations: {self.max_iterations}\n\n")
            
            # Add final test analysis
            if final_analysis:
                f.write("=" * 40 + " FINAL TEST RESULTS " + "=" * 39 + "\n")
                f.write(f"Compilation: {'SUCCESS' if final_analysis.compilation.success else 'FAILED'}\n")
                if not final_analysis.compilation.success:
                    f.write(f"Compilation Errors: {final_analysis.compilation.errors}\n")
                    f.write(f"Compilation Warnings: {final_analysis.compilation.warnings}\n")
                
                f.write(f"Overall Test Pass Rate: {final_analysis.overall_pass_rate:.1f}% ({final_analysis.passed_tests}/{final_analysis.total_tests})\n")
                f.write(f"Module Pass Rate: {final_analysis.module_pass_rate:.1f}% ({final_analysis.passed_modules}/{final_analysis.total_modules})\n\n")
                
                if final_analysis.modules:
                    f.write("Module Breakdown:\n")
                    for module_name, module_result in final_analysis.modules.items():
                        status_icon = "✅" if module_result.is_module_passed else "❌"
                        f.write(f"  {status_icon} {module_name}: {module_result.passed_tests}/{module_result.total_tests} ({module_result.pass_rate:.1f}%)\n")
                f.write("\n")
            
            # Get final project tree
            final_tree = self.get_project_tree()
            f.write("Final Project Structure:\n")
            f.write("-" * 40 + "\n")
            f.write(final_tree)
            f.write("\n\n")
            
            # Get final test results
            f.write("Final Test Output:\n")
            f.write("-" * 40 + "\n")
            f.write(final_test_results)
        
        logger.info(f"💾 Final summary saved to: {summary_file}")
        
        # Log key metrics to console
        if final_analysis:
            logger.info(f"🔧 Compilation: {'SUCCESS' if final_analysis.compilation.success else 'FAILED'}")
            logger.info(f"📊 Overall: {final_analysis.overall_pass_rate:.1f}% ({final_analysis.passed_tests}/{final_analysis.total_tests})")
            logger.info(f"📦 Modules: {final_analysis.module_pass_rate:.1f}% ({final_analysis.passed_modules}/{final_analysis.total_modules})")

    def run(self):
        """Main execution loop"""
        logger.info(f"🚀 Starting translation: {self.source_language} → {self.target_language}")
        logger.info(f"📁 Project: {self.project_name}")
        
        # Initial task instruction
        initial_task = f"""You need to translate this {self.source_language} project to {self.target_language}.

TASK: Analyze the provided source code and tests, then implement complete {self.target_language} code that passes all tests.

The system prompt above contains:
- Source code files from the original {self.source_language} project
- Public test files showing expected interfaces and behavior
- Original test files that will evaluate your implementation
- Project structure and test runner script

Your job is to:
1. Understand what the source code does
2. Look at the test files to understand the expected interface
3. Create {self.target_language} implementations using CreateFile actions
4. Test your implementations to ensure they pass

Start by creating the main implementation files based on what you see in the source code and tests."""

        current_obs = initial_task
        done = False
        
        try:
            while not done and self.current_iteration < self.max_iterations:
                self.current_iteration += 1
                logger.info(f"=== Iteration {self.current_iteration}/{self.max_iterations} ===")
                
                # Predict next action
                response, action = self.predict(current_obs)
                
                if action is None:
                    logger.warning("⚠️ Failed to parse action from response")
                    error_obs = "❌ Invalid action format! Use: CreateFile(...), ExecuteCommand(...), ReadFile(...), SearchContent(...), or Finished(...)"
                    
                    # Save this turn even if action parsing failed
                    self.save_conversation_turn(self.current_iteration, current_obs, response, None, error_obs, None)
                    
                    current_obs = error_obs
                    continue
                
                logger.info(f"💭 Action: {action}")
                
                # Execute action
                obs, done = self.execute_action(action)
                
                # Add project tree and detailed test analysis
                tree_info = self.get_project_tree()
                test_results, test_analysis = self.run_tests_and_analyze()
                
                full_observation = f"{obs}\n\n{tree_info}\n\n{test_results}"
                
                # Save conversation turn with test analysis
                self.save_conversation_turn(self.current_iteration, current_obs, response, action, full_observation, test_analysis)
                
                current_obs = full_observation
                
                logger.info(f"📝 Observation: {obs[:200]}...")
                
                if done:
                    # Save final summary
                    self.save_final_summary(action.status if isinstance(action, Finished) else 'unknown')
                    
                    if isinstance(action, Finished) and action.status.lower() == 'success':
                        logger.info("🎉 Translation completed successfully!")
                        return 'success'
                    else:
                        logger.info("❌ Translation finished with failure.")
                        return 'failed'
                
        except KeyboardInterrupt:
            logger.info("🛑 Translation interrupted by user")
            self.save_final_summary('interrupted')
            return 'interrupted'
        except Exception as e:
            logger.error(f"💥 Fatal error: {e}")
            self.save_final_summary('error')
            return 'error'
        
        logger.info("⏱️ Reached maximum iterations.")
        self.save_final_summary('timeout')
        return 'timeout'


def main():
    parser = argparse.ArgumentParser(description="Simple React Translation Agent")
    parser.add_argument('--model_name', type=str, default="claude-sonnet-4-20250514", help="Model name")
    parser.add_argument('--project_name', type=str, required=True, help="Project name")
    parser.add_argument('--source_language', type=str, required=True, help="Source language")
    parser.add_argument('--target_language', type=str, required=True, help="Target language")
    parser.add_argument('--max_iterations', type=int, default=20, help="Max iterations")
    
    args = parser.parse_args()
    
    logger.info(f"🤖 Model: {args.model_name}")
    logger.info(f"📁 Project: {args.project_name}")
    logger.info(f"🔄 Translation: {args.source_language} → {args.target_language}")
    logger.info("-" * 50)
    
    try:
        agent = ReactTranslationAgent(args)
        status = agent.run()
        
        exit_codes = {
            'success': 0,
            'failed': 1,
            'timeout': 2, 
            'interrupted': 3,
            'error': 4
        }
        
        logger.info(f"🏁 Final status: {status}")
        logger.info(f"📁 All logs saved to: {agent.log_dir}")
        sys.exit(exit_codes.get(status, 4))
        
    except Exception as e:
        logger.error(f"💥 Fatal error: {e}")
        sys.exit(4)


if __name__ == "__main__":
    main()