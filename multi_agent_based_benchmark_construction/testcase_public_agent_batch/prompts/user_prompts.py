# file path: testcase_public_agent_batch/prompts/user_prompts.py

import os
import subprocess
import re
from pathlib import Path
import tiktoken
from testcase_public_agent_batch.utils import get_project_structure, truncate_content, read_file_content


def calculate_content_tokens(content: str, encoding: tiktoken.Encoding) -> int:
    """Calculate token count for content"""
    if not encoding:
        # Rough estimation: 4 characters per token
        return len(content) // 4
    return len(encoding.encode(content))


def split_content_into_chunks(content: str, max_tokens: int, encoding: tiktoken.Encoding) -> list:
    """Split content into chunks based on token count"""
    if not content:
        return []
    
    tokens = calculate_content_tokens(content, encoding)
    if tokens <= max_tokens:
        return [content]
    
    # Split by lines and reassemble into chunks
    lines = content.split('\n')
    chunks = []
    current_chunk = []
    current_tokens = 0
    
    for line in lines:
        line_tokens = calculate_content_tokens(line + '\n', encoding)
        
        if current_tokens + line_tokens > max_tokens and current_chunk:
            chunks.append('\n'.join(current_chunk))
            current_chunk = [line]
            current_tokens = line_tokens
        else:
            current_chunk.append(line)
            current_tokens += line_tokens
    
    if current_chunk:
        chunks.append('\n'.join(current_chunk))
    
    return chunks


def read_and_limit_test_files(repo_path: Path, test_files: list, language: str, encoding: tiktoken.Encoding, max_total_tokens: int = 12000) -> list:
    """Read test files and limit total token count"""
    test_samples = []
    current_tokens = 0
    max_files = 3
    max_tokens_per_file = 4000
    
    for i, file_path in enumerate(test_files[:max_files]):
        if current_tokens >= max_total_tokens:
            break
            
        full_path = repo_path / file_path
        content = read_file_content(full_path, max_tokens=max_tokens_per_file, encoding=encoding)
        
        if content:
            content_tokens = calculate_content_tokens(content, encoding)
            
            # If adding this would exceed limit, truncate
            if current_tokens + content_tokens > max_total_tokens:
                remaining_tokens = max_total_tokens - current_tokens
                content = truncate_content(content, remaining_tokens, encoding)
                content_tokens = calculate_content_tokens(content, encoding)
            
            test_samples.append(f"## Existing Test File: {file_path}\n```{language.lower()}\n{content}\n```\n")
            current_tokens += content_tokens
            
            if current_tokens >= max_total_tokens:
                break
    
    return test_samples


def read_and_limit_source_files(repo_path: Path, source_files: list, language: str, encoding: tiktoken.Encoding, max_total_tokens: int = 8000) -> list:
    """Read source files and limit total token count"""
    source_samples = []
    current_tokens = 0
    max_files = 2
    max_tokens_per_file = 4000
    
    for i, file_path in enumerate(source_files[:max_files]):
        if current_tokens >= max_total_tokens:
            break
            
        full_path = repo_path / file_path
        content = read_file_content(full_path, max_tokens=max_tokens_per_file, encoding=encoding)
        
        if content:
            content_tokens = calculate_content_tokens(content, encoding)
            
            # If adding this would exceed limit, truncate
            if current_tokens + content_tokens > max_total_tokens:
                remaining_tokens = max_total_tokens - current_tokens
                content = truncate_content(content, remaining_tokens, encoding)
                content_tokens = calculate_content_tokens(content, encoding)
            
            source_samples.append(f"## Source File: {file_path}\n```{language.lower()}\n{content}\n```\n")
            current_tokens += content_tokens
            
            if current_tokens >= max_total_tokens:
                break
    
    return source_samples


def generate_initial_public_test_prompt(repo_path: Path, language: str, encoding: tiktoken.Encoding = None) -> str:
    """Generate the initial user prompt for public test case generation with token limits"""
    repo_path = Path(repo_path)
    
    # Get project structure (limited to 2000 tokens)
    project_structure = get_project_structure(repo_path)
    project_structure = truncate_content(project_structure, 2000, encoding)
    
    # Check if run_tests.sh exists and show its content (limited to 500 tokens)
    run_tests_path = repo_path / "run_tests.sh"
    run_tests_content = ""
    if run_tests_path.exists():
        run_tests_content = read_file_content(run_tests_path, max_tokens=500, encoding=encoding)
    
    # Analyze existing test files (with token limits)
    existing_test_files = []
    test_file_patterns = [
        'test_*.py', '*_test.py', '*Test.java', '*.test.js', '*.spec.js',
        '*_test.cpp', '*_test.c', '*Test.cs', 'test_*.m', '*_test.m'
    ]
    
    for pattern in test_file_patterns:
        for file in repo_path.glob(f'**/{pattern}'):
            # Skip excluded directories
            if any(excl in str(file) for excl in ['.git', 'node_modules', 'venv', '__pycache__']):
                continue
            rel_path = file.relative_to(repo_path)
            existing_test_files.append(str(rel_path))
    
    # Check source code files to understand what's being tested (with token limits)
    source_files = []
    exclude_patterns = ['.git', 'node_modules', 'venv', '__pycache__', '.pytest_cache']
    
    # Add MATLAB extension to source_extensions dictionary
    source_extensions = {
        'Python': ['.py'],
        'JavaScript': ['.js', '.jsx', '.ts', '.tsx'],
        'Java': ['.java'],
        'C++': ['.cpp', '.cc', '.cxx', '.h', '.hpp'],
        'C#': ['.cs'],
        'C': ['.c', '.h'],
        'Matlab': ['.m']  # Add MATLAB extension
    }
    
    # Get extensions for the specified language
    extensions = source_extensions.get(language, [])
    
    # Find all source files with matching extensions
    for ext in extensions:
        for file in repo_path.glob(f'**/*{ext}'):
            # Skip excluded directories
            if any(excl in str(file) for excl in exclude_patterns):
                continue
            # Skip test files
            if any(test_pattern in str(file) for test_pattern in ['test_', '_test', 'Test', '.test.']):
                continue
            # Add relative path from repo root
            rel_path = file.relative_to(repo_path)
            source_files.append(str(rel_path))
    
    # Read existing test files content with token limits (max 12k tokens total)
    test_code_samples = read_and_limit_test_files(repo_path, existing_test_files, language, encoding, max_total_tokens=12000)
    
    # Read source files with token limits (max 8k tokens total)
    source_code_samples = read_and_limit_source_files(repo_path, source_files, language, encoding, max_total_tokens=8000)
    
    # Calculate approximate token usage for monitoring
    total_tokens_estimate = 0
    if encoding:
        total_tokens_estimate += calculate_content_tokens(project_structure, encoding)
        total_tokens_estimate += calculate_content_tokens(run_tests_content, encoding)
        for sample in test_code_samples:
            total_tokens_estimate += calculate_content_tokens(sample, encoding)
        for sample in source_code_samples:
            total_tokens_estimate += calculate_content_tokens(sample, encoding)
    
    # Build the prompt with token-conscious structure
    prompt = f"""## Project Information - Token-Limited Analysis
Project Name: {repo_path.name}
Language: {language}
Estimated Content Tokens: ~{total_tokens_estimate:,}

## Project Structure (Max 2k tokens)
```
{project_structure}
```

## Existing Test Runner Script (Max 500 tokens)
The project has a `run_tests.sh` script:
```bash
{run_tests_content}
```

## Existing Test Files
The following test files already exist and need public test counterparts:
{', '.join(existing_test_files[:10]) if existing_test_files else 'No existing test files found'}
{f"... and {len(existing_test_files) - 10} more files" if len(existing_test_files) > 10 else ""}

## Existing Test Code Patterns (Max 12k tokens)
Analyze these existing tests to understand the testing logic:

{os.linesep.join(test_code_samples)}

## Source Code Context (Max 8k tokens)  
The following source files are being tested:

{os.linesep.join(source_code_samples)}

## Your Task
1. **Analyze existing test cases** to understand their testing logic and patterns
2. **Generate public test cases** with the same functionality but different input/output test data
3. **Create run_public_tests.sh** script to execute all public tests
4. **Ensure all tests pass** - both existing and public tests must work correctly

## Public Test Requirements
- **Same testing logic** as existing tests but with **different test data**
- Examples:
  - Existing: `assert add(2, 3) == 5` → Public: `assert add(4, 7) == 11`
  - Existing: `assert is_even(4) == True` → Public: `assert is_even(8) == True`
  - Existing: `assert factorial(3) == 6` → Public: `assert factorial(4) == 24`
- Use meaningful different values that still test the same functionality
- Maintain test validity and edge case coverage with different data

## Token Management
- This prompt is designed to stay within token limits
- Test content: ~{sum(calculate_content_tokens(sample, encoding) for sample in test_code_samples):,} tokens
- Source content: ~{sum(calculate_content_tokens(sample, encoding) for sample in source_code_samples):,} tokens
- Focus on the most important test patterns and source code sections

## Required Deliverables
1. **Public test files** with different input/output data
2. **run_public_tests.sh** script that executes all public tests
3. **Verification** that both existing and public tests pass
4. **Clear separation** between existing and public test files

## Output Format
You must provide your work in the following formats:

### For File Creation/Modification
```file:path/to/file.ext
File content here
```

### For Commands to Execute
```bash
command1
command2
```

### For Task Completion
When you've completed the task successfully, include:
```finished
{{
    "tests_path": ["path/to/existing_testfile1", "path/to/existing_testfile2"],  
    "run_tests_path": "run_tests.sh",
    "public_tests_path": ["path/to/public_testfile1", "path/to/public_testfile2"],  
    "run_public_tests_path": "run_public_tests.sh"
}}
```

Remember, you have limited rounds of interaction, so be efficient and focus on creating high-quality public tests with different test data!
"""
    
    return prompt.strip()


def generate_public_test_followup_prompt(repo_path: Path, language: str, 
                                        execution_results: str = None, 
                                        file_results: str = None,
                                        encoding: tiktoken.Encoding = None) -> str:
    """Generate follow-up user prompt for public test case generation based on previous results with token limits"""
    repo_path = Path(repo_path)
    
    # Get current project structure (limited to 1500 tokens)
    current_structure = get_project_structure(repo_path)
    current_structure = truncate_content(current_structure, 1500, encoding)
    
    # Start building the prompt
    prompt_parts = [
        f"## Current Project Structure (Max 1.5k tokens)",
        "```",
        current_structure,
        "```",
        ""
    ]
    
    # Add operation results if any (with token limits)
    if execution_results or file_results:
        prompt_parts.append("## Previous Results:")
        
        if file_results:
            file_results = truncate_content(file_results, 1500, encoding)
            prompt_parts.extend([
                "### File Operations Results (Max 1.5k tokens)",
                file_results,
                ""
            ])
            
        if execution_results:
            execution_results = truncate_content(execution_results, 1500, encoding)
            prompt_parts.extend([
                "### Command Execution Results (Max 1.5k tokens)",
                execution_results,
                ""
            ])
    
    # Analyze existing and public test files (with limits)
    existing_test_files = []
    public_test_files = []
    
    # Find existing test files (excluding public tests)
    test_patterns = [
        '**/test_*.py', '**/*_test.py', '**/*Test.java', '**/*.test.js', 
        '**/*.spec.js', '**/test_*.cpp', '**/test_*.c', '**/*Test.cs',
        '**/test_*.m', '**/*_test.m'
    ]
    
    public_patterns = [
        '**/public_test_*.py', '**/*_public_test.py', '**/*PublicTest.java', 
        '**/*.public.test.js', '**/public_*.test.js', '**/public_test_*.cpp', 
        '**/public_test_*.c', '**/*PublicTest.cs', '**/public_test_*.m'
    ]
    
    for pattern in test_patterns:
        for file in repo_path.glob(pattern):
            # Skip common excluded directories
            if any(excl in str(file) for excl in ['.git', 'node_modules', 'venv']):
                continue
            rel_path = str(file.relative_to(repo_path))
            # Skip if it looks like a public test file
            if any(pub_indicator in rel_path.lower() for pub_indicator in ['public', '_public']):
                continue
            existing_test_files.append(rel_path)
    
    for pattern in public_patterns:
        for file in repo_path.glob(pattern):
            if any(excl in str(file) for excl in ['.git', 'node_modules', 'venv']):
                continue
            rel_path = str(file.relative_to(repo_path))
            public_test_files.append(rel_path)
    
    # Also check for public tests in directories
    public_dirs = ['public_tests', 'public', 'tests/public']
    for dir_name in public_dirs:
        pub_dir = repo_path / dir_name
        if pub_dir.exists():
            for file in pub_dir.glob('**/*'):
                if file.is_file() and any(file.name.endswith(ext) for ext in ['.py', '.js', '.java', '.cpp', '.c', '.cs', '.m']):
                    rel_path = str(file.relative_to(repo_path))
                    public_test_files.append(rel_path)
    
    # Add test file information to the prompt (with limits)
    if existing_test_files:
        # Limit to first 10 files to avoid token overflow
        file_list = ', '.join(existing_test_files[:10])
        prompt_parts.extend([
            "## Existing Test Files",
            file_list,
            "" if len(existing_test_files) <= 10 else f"... and {len(existing_test_files) - 10} more files",
            ""
        ])
    
    if public_test_files:
        # Limit to first 10 files
        file_list = ', '.join(public_test_files[:10])
        prompt_parts.extend([
            "## Created Public Test Files",
            file_list,
            "" if len(public_test_files) <= 10 else f"... and {len(public_test_files) - 10} more files",
            ""
        ])
    else:
        prompt_parts.extend([
            "## Public Test Files",
            "No public test files detected yet.",
            ""
        ])
    
    # Check for test runner scripts
    test_scripts = []
    for script_name in ['run_tests.sh', 'run_public_tests.sh']:
        script_path = repo_path / script_name
        if script_path.exists():
            test_scripts.append(f"✅ {script_name} exists")
        else:
            test_scripts.append(f"❌ {script_name} missing")
    
    if test_scripts:
        prompt_parts.extend([
            "## Test Runner Scripts Status",
            "\n".join(test_scripts),
            ""
        ])
    
    # Calculate token usage for monitoring
    current_tokens = 0
    if encoding:
        for part in prompt_parts:
            current_tokens += calculate_content_tokens(part, encoding)
    
    # Complete the prompt with task instructions
    prompt_parts.extend([
        f"## Token Usage Status",
        f"Current prompt tokens: ~{current_tokens:,}",
        f"Target: Keep under 25k total tokens",
        "",
        "## Your Task",
        "Based on the results above, continue with public test case generation:",
        "",
        "1. **Fix any issues** with existing or public tests",
        "2. **Create missing public test files** based on existing test patterns",
        "3. **Use different input/output data** while maintaining the same testing logic",
        "4. **Create/update run_public_tests.sh** to run all public tests",
        "5. **Verify both test suites pass** completely",
        "",
        "## Public Test Guidelines",
        "- Analyze existing test patterns and create equivalent public tests",
        "- Use different but valid test data (different numbers, strings, scenarios)",
        "- Maintain the same test structure and assertions",
        "- Ensure public tests are as comprehensive as existing tests",
        "",
        "## Token-Conscious Approach",
        "- Focus on the most critical test files first",
        "- Create concise but comprehensive public tests",
        "- Prioritize quality over quantity",
        "",
        "## Required Actions",
        "- Create/update public test files with different test data",
        "- Create/update run_public_tests.sh script",
        "- Run both existing and public tests to verify they pass",
        "- Fix any failing tests",
        "",
        "## Output Format",
        "Provide your work in the following formats:",
        "",
        "### For File Creation/Modification",
        "```file:path/to/file.ext",
        "File content here",
        "```",
        "",
        "### For Commands to Execute",
        "```bash",
        "command1",
        "command2",
        "```",
        "",
        "### For Task Completion",
        "When both existing and public tests pass completely, include:",
        "```finished",
        "{",
        '    "tests_path": ["path/to/existing_testfile1", "path/to/existing_testfile2"],',
        '    "run_tests_path": "run_tests.sh",',
        '    "public_tests_path": ["path/to/public_testfile1", "path/to/public_testfile2"],',
        '    "run_public_tests_path": "run_public_tests.sh"',
        "}",
        "```",
        "",
        "Focus on creating high-quality public tests with meaningful different test data!"
    ])
    
    # Join all the parts to create the final prompt
    final_prompt = "\n".join(prompt_parts)
    
    # Final token check and truncation if needed
    if encoding:
        final_tokens = calculate_content_tokens(final_prompt, encoding)
        if final_tokens > 25000:  # Emergency truncation
            final_prompt = truncate_content(final_prompt, 25000, encoding)
    
    return final_prompt


def extract_finished_data(text: str) -> dict:
    """Extract finished block data from model response"""
    finished_pattern = r'```finished\s*\n(.*?)\n```'
    matches = re.findall(finished_pattern, text, re.DOTALL)
    
    if not matches:
        return None
        
    try:
        # Clean up the JSON string
        json_str = matches[-1].strip()
        # Handle potential format issues (convert single quotes to double quotes, etc.)
        json_str = re.sub(r"'", '"', json_str)
        # Parse the JSON
        import json
        finished_data = json.loads(json_str)
        return finished_data
    except (json.JSONDecodeError, ValueError, IndexError):
        return None