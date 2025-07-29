# file path: testcase_public_agent_batch/utils.py

import os
import subprocess
from pathlib import Path
import tiktoken


def get_project_structure(repo_path: Path) -> str:
    """Get project structure using tree command"""
    exclude_patterns = [
        '.git', '.svn', '.hg', '.DS_Store', 'Thumbs.db', '.*~', '*.tmp', '*.temp',  # 版本控制和系统文件
        '__pycache__', '*.pyc', '*.pyo', '.pytest_cache', '*.egg-info',  # Python
        'node_modules', '.npm', '.yarn',  # JavaScript/Node.js
        '*.class', 'target',  # Java
        '*.o', '*.obj', '*.exe', '*.dll', '*.so', '*.dylib',  # C/C++
        # 'bin', 
        'obj', '*.pdb',  # C#
        'build', 'dist', 'out',  # 构建目录
        '.vscode/settings.json', '.idea/workspace.xml', '*.swp', '*.swo'  # 编辑器临时文件
    ]
    exclude_patterns_str = '|'.join(exclude_patterns)
    result = subprocess.run(
        ['tree', str(repo_path), '-I', exclude_patterns_str],
        capture_output=True, text=True, timeout=30
    )
    # print(repo_path)
    # print(result.stdout)
    # exit()
    if result.returncode == 0:
        return result.stdout
    return ""

def truncate_content(content: str, max_tokens: int, encoding: tiktoken.Encoding = None) -> str:
    """Truncate content to fit within token limit"""
    token_list = encoding.encode(content)
    
    if len(token_list) <= max_tokens:
        return content
    
    original_length = len(token_list)
    token_list = token_list[:max_tokens]
    
    # 解码截断后的内容
    truncated_text = encoding.decode(token_list)
    
    # 计算原始内容和截断后内容的行数
    original_lines = len(content.splitlines())
    truncated_lines = len(truncated_text.splitlines())
    omitted_lines = original_lines - truncated_lines
    
    return truncated_text + f'\n({omitted_lines} lines omitted)'

def read_file_content(file_path: Path, max_tokens: int = 2000, encoding: tiktoken.Encoding = None) -> str:
    """Read and return file content with token limit"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            if content.strip():
                return truncate_content(content, max_tokens, encoding)
            return ""
    except (UnicodeDecodeError, PermissionError, FileNotFoundError):
        return ""
