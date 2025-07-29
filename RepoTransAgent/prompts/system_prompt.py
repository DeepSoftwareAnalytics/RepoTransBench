# file path: RepoTransAgent/prompts/system_prompt.py

import json
import os
from RepoTransAgent.actions import AVAILABLE_ACTIONS


def get_react_translation_prompt(source_language, target_language, project_name):
    # Generate action descriptions
    action_descriptions = "\n".join([action_cls.get_action_description() for action_cls in AVAILABLE_ACTIONS])
    
    # Read project structure, source files, and tests content
    project_structure = get_project_structure(source_language, target_language, project_name)
    source_files_content = get_source_files_content(source_language, project_name)
    public_tests_content = get_public_tests_content(source_language, target_language, project_name)
    original_tests_content = get_original_tests_content(source_language, target_language, project_name)
    run_tests_script = get_run_tests_script(source_language, target_language, project_name)
    
    return f'''You are a code translator. Translate {source_language} to {target_language} for project {project_name}.

## 🎯 GOAL: Make all tests pass

## 📁 PROJECT STRUCTURE
{project_structure}

## 📄 SOURCE FILES (Reference - 5 sample files from {source_language} project)
{source_files_content}

## 📋 PUBLIC TESTS (Visible Reference Tests)
{public_tests_content}

## 🔧 TEST RUNNER SCRIPT
{run_tests_script}

## 🔄 WORKFLOW:
1. **GENERATE**: Directly create {target_language} implementations based on tests and source files
2. **TEST**: Run tests and fix until all pass

## 📋 STRICT RESPONSE FORMAT (REQUIRED):
You MUST respond using this EXACT format:

Thought: [your reasoning about what to do next]

Action: [one action from the list below]

EXAMPLE:
Thought: I need to create the main Python file based on the source code I can see in the prompt.

Action: CreateFile(filepath="src/main.py"):
```python
def hello_world():
    return "Hello, World!"
```

## 🛠️ Available Actions:

### CreateFile - Create implementation files
Format: CreateFile(filepath="path/to/file.ext"):
```
file_content_here
```

### ReadFile - Read existing files  
Format: ReadFile(filepath="path/to/file.ext")

### ExecuteCommand - Run commands (especially tests)
Format: ExecuteCommand(command="./run_tests.sh")

### SearchContent - Search in project files
Format: SearchContent(keyword="search_term")

### Finished - Mark task complete
Format: Finished(status="success") or Finished(status="failed")

{action_descriptions}

## 🚨 CRITICAL RULES:
- ALWAYS use the exact format: "Thought:" followed by "Action:"
- NO other text before or after this format
- NO explanations outside the Thought/Action structure
- Generate complete implementations immediately (no scanning phase)
- Use public tests as your specification reference
- Reference source files to understand the logic to translate
- Implement COMPLETE functionality (no stubs)
- Test frequently with ExecuteCommand

IMPORTANT: Your response must start with "Thought:" and contain exactly one "Action:". Any other format will fail to parse.

START: Create the required implementation files based on the tests and source file references.'''


def truncate_content(content, max_chars=20000):
    """Truncate content to maximum characters"""
    if len(content) <= max_chars:
        return content
    return content[:max_chars] + "\n... [TRUNCATED]"


def get_source_files_content(source_language, project_name):
    """Read and return sample source files content from the source project"""
    try:
        # Define code file extensions for different languages
        code_extensions = {
            'C++': ['.cpp', '.hpp', '.cc', '.hh', '.cxx', '.hxx', '.c++', '.h++', '.h'],
            'C': ['.c', '.h'],
            'JavaScript': ['.js', '.jsx', '.ts', '.tsx'],
            'Python': ['.py'],
            'Java': ['.java'],
            'Go': ['.go'],
            'Rust': ['.rs'],
            'C#': ['.cs'],
            'PHP': ['.php'],
            'Ruby': ['.rb'],
            'Swift': ['.swift'],
            'Kotlin': ['.kt', '.kts'],
            'Scala': ['.scala'],
            'R': ['.r', '.R'],
            'Matlab': ['.m'],
            'Shell': ['.sh', '.bash'],
            'Perl': ['.pl', '.pm'],
            'Lua': ['.lua']
        }
        
        extensions = code_extensions.get(source_language, ['.txt'])
        
        # Source project path
        source_path = f"/workspace/source_projects/{source_language}/{project_name}"
        if not os.path.exists(source_path):
            return f"Source project not found: {source_path}"
        
        # Find code files
        code_files = []
        for root, dirs, files in os.walk(source_path):
            for file in files:
                if any(file.lower().endswith(ext) for ext in extensions):
                    file_path = os.path.join(root, file)
                    rel_path = os.path.relpath(file_path, source_path)
                    code_files.append((rel_path, file_path))
        
        if not code_files:
            return f"No code files found with extensions {extensions}"
        
        # Select up to 5 files (prioritize main files)
        selected_files = []
        priority_names = ['main', 'index', 'app', 'core', 'base']
        
        # First, add priority files
        for name in priority_names:
            for rel_path, full_path in code_files:
                if any(name in rel_path.lower() for name in priority_names) and len(selected_files) < 5:
                    if (rel_path, full_path) not in selected_files:
                        selected_files.append((rel_path, full_path))
        
        # Then add other files up to 5 total
        for rel_path, full_path in code_files:
            if len(selected_files) >= 5:
                break
            if (rel_path, full_path) not in selected_files:
                selected_files.append((rel_path, full_path))
        
        # Read file contents
        files_content = []
        for rel_path, full_path in selected_files[:5]:
            try:
                with open(full_path, 'r', encoding='utf-8', errors='ignore') as f:
                    content = f.read()
                    truncated_content = truncate_content(content)
                    files_content.append(f"### {rel_path}:\n```{source_language.lower()}\n{truncated_content}\n```")
            except Exception as e:
                files_content.append(f"### {rel_path}: Error reading file - {str(e)}")
        
        return "\n\n".join(files_content) if files_content else "No source files could be read"
        
    except Exception as e:
        return f"Error reading source files: {str(e)}"


def get_project_structure(source_language, target_language, project_name):
    """Read and return project structure for the specified project"""
    try:
        # Read projects summary to get structure file path
        summary_path = "/workspace/target_projects/projects_summary.jsonl"
        if not os.path.exists(summary_path):
            return "Error: projects_summary.jsonl not found"
        
        # Find the project
        project_info = None
        with open(summary_path, 'r') as f:
            for line in f:
                data = json.loads(line.strip())
                if (data.get('project_name') == project_name and 
                    data.get('source_language') == source_language and 
                    data.get('target_language') == target_language):
                    project_info = data
                    break
        
        if not project_info:
            return f"Error: Project {project_name} not found"
        
        # Get project structure file path
        structure_file = project_info.get('project_structure')
        if not structure_file:
            return "No project structure file found"
        
        # Read project structure content
        project_path = f"/workspace/target_projects/{source_language}/{target_language}/{project_name}"
        structure_path = os.path.join(project_path, structure_file)
        
        if os.path.exists(structure_path):
            with open(structure_path, 'r') as f:
                content = f.read()
                truncated_content = truncate_content(content)
                return f"```\n{truncated_content}\n```"
        else:
            return f"Project structure file not found: {structure_path}"
        
    except Exception as e:
        return f"Error reading project structure: {str(e)}"


def get_run_tests_script(source_language, target_language, project_name):
    """Read and return run_tests.sh script content"""
    try:
        # Read projects summary to get script path
        summary_path = "/workspace/target_projects/projects_summary.jsonl"
        if not os.path.exists(summary_path):
            return "Error: projects_summary.jsonl not found"
        
        # Find the project
        project_info = None
        with open(summary_path, 'r') as f:
            for line in f:
                data = json.loads(line.strip())
                if (data.get('project_name') == project_name and 
                    data.get('source_language') == source_language and 
                    data.get('target_language') == target_language):
                    project_info = data
                    break
        
        if not project_info:
            return f"Error: Project {project_name} not found"
        
        # Get test script file path
        script_file = project_info.get('run_tests_script', 'run_tests.sh')
        
        # Read script content
        project_path = f"/workspace/target_projects/{source_language}/{target_language}/{project_name}"
        script_path = os.path.join(project_path, script_file)
        
        if os.path.exists(script_path):
            with open(script_path, 'r') as f:
                content = f.read()
                truncated_content = truncate_content(content)
                return f"### {script_file}:\n```bash\n{truncated_content}\n```"
        else:
            return f"Test script file not found: {script_path}"
        
    except Exception as e:
        return f"Error reading test script: {str(e)}"


def get_original_tests_content(source_language, target_language, project_name):
    """Read and return original tests content for the specified project"""
    try:
        # Read projects summary
        summary_path = "/workspace/target_projects/projects_summary.jsonl"
        if not os.path.exists(summary_path):
            return "Error: projects_summary.jsonl not found"
        
        # Find the project
        project_info = None
        with open(summary_path, 'r') as f:
            for line in f:
                data = json.loads(line.strip())
                if (data.get('project_name') == project_name and 
                    data.get('source_language') == source_language and 
                    data.get('target_language') == target_language):
                    project_info = data
                    break
        
        if not project_info:
            return f"Error: Project {project_name} not found"
        
        # Get original test paths
        target_original_tests = project_info.get('target_original_tests', [])
        if not target_original_tests:
            return "No original tests found for this project"
        
        # Read original test contents
        project_path = f"/workspace/target_projects/{source_language}/{target_language}/{project_name}"
        tests_content = []
        
        for test_path in target_original_tests:
            full_path = os.path.join(project_path, test_path)
            if os.path.exists(full_path):
                with open(full_path, 'r') as f:
                    content = f.read()
                    truncated_content = truncate_content(content)
                    tests_content.append(f"### {test_path}:\n```python\n{truncated_content}\n```")
            else:
                tests_content.append(f"### {test_path}: FILE NOT FOUND")
        
        return "\n\n".join(tests_content) if tests_content else "No original test files found"
        
    except Exception as e:
        return f"Error reading original tests: {str(e)}"


def get_public_tests_content(source_language, target_language, project_name):
    """Read and return public tests content for the specified project"""
    try:
        # Read projects summary
        summary_path = "/workspace/target_projects/projects_summary.jsonl"
        if not os.path.exists(summary_path):
            return "Error: projects_summary.jsonl not found"
        
        # Find the project
        project_info = None
        with open(summary_path, 'r') as f:
            for line in f:
                data = json.loads(line.strip())
                if (data.get('project_name') == project_name and 
                    data.get('source_language') == source_language and 
                    data.get('target_language') == target_language):
                    project_info = data
                    break
        
        if not project_info:
            return f"Error: Project {project_name} not found"
        
        # Get public test paths
        target_public_tests = project_info.get('target_public_tests', [])
        if not target_public_tests:
            return "No public tests found for this project"
        
        # Read public test contents
        project_path = f"/workspace/target_projects/{source_language}/{target_language}/{project_name}"
        tests_content = []
        
        for test_path in target_public_tests:
            full_path = os.path.join(project_path, test_path)
            if os.path.exists(full_path):
                with open(full_path, 'r') as f:
                    content = f.read()
                    truncated_content = truncate_content(content)
                    tests_content.append(f"### {test_path}:\n```python\n{truncated_content}\n```")
            else:
                tests_content.append(f"### {test_path}: FILE NOT FOUND")
        
        return "\n\n".join(tests_content) if tests_content else "No public test files found"
        
    except Exception as e:
        return f"Error reading public tests: {str(e)}"


if __name__ == "__main__":
    print(get_react_translation_prompt("JavaScript", "Python", "6_stopwords-json"))