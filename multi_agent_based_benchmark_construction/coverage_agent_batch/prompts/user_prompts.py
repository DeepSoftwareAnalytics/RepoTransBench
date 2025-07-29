# file path: coverage_agent_batch/prompts/user_prompts.py

import os
import subprocess
from pathlib import Path
import tiktoken
from runnable_agent_batch.utils import get_project_structure, truncate_content, read_file_content


def generate_initial_coverage_prompt(repo_path: Path, language: str, encoding: tiktoken.Encoding = None) -> str:
    """Generate the initial user prompt for coverage analysis"""
    repo_path = Path(repo_path)
    
    # Get project structure
    project_structure = get_project_structure(repo_path)
    
    # Check if run_tests.sh exists and show its content
    run_tests_path = repo_path / "run_tests.sh"
    run_tests_content = ""
    if run_tests_path.exists():
        run_tests_content = read_file_content(run_tests_path, max_tokens=500, encoding=encoding)
    
    # Build the prompt
    prompt_parts = [
        f"## Project Information",
        f"Project Name: {repo_path.name}",
        f"Language: {language}",
        f"",
        f"## Project Structure",
        f"```",
        project_structure,
        f"```",
        f""
    ]
    
    if run_tests_content:
        prompt_parts.extend([
            f"## Existing Test Script",
            f"The project already has a working `run_tests.sh` script:",
            f"```bash",
            run_tests_content,
            f"```",
            f""
        ])
    
    prompt_parts.extend([
        f"## Your Task",
        f"1. Analyze the existing test structure and `run_tests.sh` script",
        f"2. Install appropriate coverage analysis tools for {language}",
        f"3. Create a `run_coverage.sh` script that:",
        f"   - Runs the existing tests with coverage measurement",
        f"   - Generates coverage reports",
        f"   - Outputs the line coverage percentage",
        f"4. Execute the coverage script and report the results",
        f"",
        f"## Important Requirements",
        f"- Do NOT modify the existing `run_tests.sh` script",
        f"- Focus on line coverage as the primary metric",
        f"- Output coverage percentage in the exact format: ```coverage\\nXX.X\\n```",
        f"- Ensure the coverage script is reproducible and reliable"
    ])
    
    return "\n".join(prompt_parts)


def generate_coverage_followup_prompt(repo_path: Path, language: str, 
                                     execution_results: str = None, 
                                     file_results: str = None,
                                     encoding: tiktoken.Encoding = None) -> str:
    """Generate follow-up user prompt for coverage analysis based on previous results"""
    repo_path = Path(repo_path)
    
    # Get current project structure
    current_structure = get_project_structure(repo_path)
    current_structure = truncate_content(current_structure, 1000, encoding)
    
    prompt_parts = [
        f"## Current Project Structure",
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
                "### Command Execution Results",
                execution_results,
                ""
            ])
    
    # Check for coverage-related files
    coverage_files = []
    common_coverage_files = [
        ".coverage", "coverage.xml", "coverage.json", "lcov.info", 
        "coverage.info", "jacoco.xml", "cobertura.xml"
    ]
    coverage_dirs = ["coverage", "htmlcov", "target/site/jacoco", "build/reports/jacoco"]
    
    for file_name in common_coverage_files:
        file_path = repo_path / file_name
        if file_path.exists():
            coverage_files.append(f"File: {file_name}")
    
    for dir_name in coverage_dirs:
        dir_path = repo_path / dir_name
        if dir_path.exists() and dir_path.is_dir():
            coverage_files.append(f"Directory: {dir_name}/")
    
    if coverage_files:
        prompt_parts.extend([
            "## Detected Coverage Files/Directories",
            "\n".join(coverage_files),
            ""
        ])
    
    # Complete the prompt
    prompt_parts.extend([
        "## Your Task",
        "Based on the results above, continue with coverage analysis:",
        "",
        "- If coverage tools are not installed, install them",
        "- If `run_coverage.sh` doesn't exist or needs updates, create/update it", 
        "- If coverage analysis fails, debug and fix the issues",
        "- Extract and output the line coverage percentage in the required format",
        "",
        "## Coverage Output Requirement",
        "When you successfully obtain coverage data, you MUST output it in this exact format:",
        "```coverage",
        "XX.X",
        "```",
        "Where XX.X is the line coverage percentage (e.g., 75.5 for 75.5% coverage)",
        "",
        "## Termination Conditions",
        "Output one of these status blocks:",
        "",
        "If coverage cannot be measured after multiple attempts:",
        "```status",
        "failed",
        "```",
        "",
        "If coverage is successfully measured and reported:",
        "```status", 
        "success",
        "```"
    ])
    
    return "\n".join(prompt_parts)


def extract_coverage_percentage(text: str) -> float:
    """Extract coverage percentage from model response"""
    import re
    
    # Look for coverage blocks
    coverage_pattern = r'```coverage\s*\n\s*([0-9]+\.?[0-9]*)\s*\n```'
    matches = re.findall(coverage_pattern, text, re.IGNORECASE)
    
    if matches:
        try:
            return float(matches[-1])  # Return the last match
        except ValueError:
            pass
    
    # Fallback: look for percentage patterns in text
    percentage_patterns = [
        r'coverage[:\s]+([0-9]+\.?[0-9]*)%',
        r'line coverage[:\s]+([0-9]+\.?[0-9]*)%',
        r'([0-9]+\.?[0-9]*)%\s+coverage',
        r'total.*?([0-9]+\.?[0-9]*)%'
    ]
    
    for pattern in percentage_patterns:
        matches = re.findall(pattern, text, re.IGNORECASE)
        if matches:
            try:
                return float(matches[-1])
            except ValueError:
                continue
    
    return None