# file path: runnable_agent_batch/prompts/user_prompts.py

import os
import subprocess
from pathlib import Path
import tiktoken
from runnable_agent_batch.utils import get_project_structure, truncate_content, read_file_content


def generate_initial_user_prompt(repo_path: Path, language: str, encoding: tiktoken.Encoding = None) -> str:
    """Generate the initial user prompt with project structure"""
    repo_path = Path(repo_path)
    
    # Get project structure
    project_structure = get_project_structure(repo_path)
    
    # Build the prompt with triple quotes
    prompt = f'''## Project Information
Project Name: {repo_path.name}
Language: {language}

## Project Structure
```
{project_structure}
```

## Your Task
Create a `run_tests.sh` script and write commands to run all test cases in it.
Of course, you should output `./run_tests.sh` to run the script.'''
    
    return prompt


def generate_followup_user_prompt(repo_path: Path, language: str, 
                                  execution_results: str = None, 
                                  file_results: str = None,
                                  encoding: tiktoken.Encoding = None) -> str:
    """Generate follow-up user prompt based on previous results"""
    repo_path = Path(repo_path)
    
    # Get current project structure
    current_structure = get_project_structure(repo_path)
    current_structure = truncate_content(current_structure, 1000, encoding)
    
    prompt_parts = [
        f"## Project Structure",
        "```",
        current_structure,
        "```",
        ""
    ]
    
    # Add operation results if any
    if execution_results or file_results:
        prompt_parts.append("## Previous Results:")
        
        if file_results:
            file_results = truncate_content(file_results, 2000, encoding)
            prompt_parts.extend([
                "### File Operations Results",
                file_results,
                ""
            ])
            
        if execution_results:
            execution_results = truncate_content(execution_results, 2000, encoding)
            prompt_parts.extend([
                "### Command Excution Results",
                execution_results,
                ""
            ])
    
    # Complete the prompt with triple quotes
    followup_content = "\n".join(prompt_parts)
    
    prompt = f'''{followup_content}

## Your Task
Based on the results above, make your following decisions:

- Read files or search context if needed
- Install dependencies if needed
- Create/update `run_tests.sh` script

## Termination Conditions
You must output one of these exact status blocks to terminate the pipeline:
If the tests cannot be compiled/built after multiple serious attempts, output:
```status
failed
```

If a `run_tests.sh` script is created, and make all tests pass, output:
```status
success
```'''
    
    return prompt