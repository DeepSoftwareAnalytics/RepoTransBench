import os
import json
import re
from pathlib import Path
import tiktoken
import csv

class PythonFileAnalyzer:
    def __init__(self, file_path):
        self.file_path = file_path
        self.content = self._read_file()
        self.cleaned_content = self._remove_comments()

    def _read_file(self):
        """Read file content"""
        with open(self.file_path, 'r', encoding='utf-8') as f:
            return f.read()

    def _remove_comments(self):
        """Remove comments from the file"""
        # Remove single-line comments
        cleaned_code = re.sub(r'#.*', '', self.content)
        # Remove multi-line comments (''' and """ )
        cleaned_code = re.sub(r'\'\'\'.*?\'\'\'', '', cleaned_code, flags=re.DOTALL)
        cleaned_code = re.sub(r'\"\"\".*?\"\"\"', '', cleaned_code, flags=re.DOTALL)
        return cleaned_code

    def count_tokens(self):
        """Count tokens in the code using tiktoken tokenizer"""
        encoder = tiktoken.get_encoding("cl100k_base")  # Use GPT-4's basic tokenizer
        tokens = encoder.encode(self.cleaned_content)
        return len(tokens)

    def count_code_lines(self):
        """Count effective code lines, excluding empty and comment lines"""
        lines = self.cleaned_content.splitlines()
        code_lines = [line for line in lines if line.strip() != '']
        return len(code_lines)

    def count_functions(self):
        """Count the number of functions"""
        function_pattern = re.compile(r'^\s*def', re.MULTILINE)
        functions = function_pattern.findall(self.cleaned_content)
        return len(functions)

    def count_classes(self):
        """Count the number of classes"""
        class_pattern = re.compile(r'^\s*class', re.MULTILINE)
        classes = class_pattern.findall(self.cleaned_content)
        return len(classes)

    def count_imports(self):
        """Count the number of import statements"""
        import_pattern = re.compile(r'^\s*(import|from)', re.MULTILINE)
        imports = import_pattern.findall(self.cleaned_content)
        return len(imports)

    def analyze(self):
        """Return analysis results"""
        return {
            'token_count': self.count_tokens(),
            'code_lines_count': self.count_code_lines(),
            'function_count': self.count_functions(),
            'class_count': self.count_classes(),
            'import_count': self.count_imports(),
        }

class RepoAnalyzer:
    def __init__(self, repos_base_path, info_file_path):
        self.repos_base_path = Path(repos_base_path)
        self.info_file_path = Path(info_file_path)
        self.repos_info = self._load_info()

    def _load_info(self):
        """Load info_raw.jsonl file"""
        repos_info = {}
        with open(self.info_file_path, 'r', encoding='utf-8') as f:
            for line in f:
                repo_info = json.loads(line)
                repos_info[repo_info['repo_path'].replace('/', '___')] = {
                    'tests_path': repo_info['tests_path'],
                }
        return repos_info

    def _is_test_file(self, repo_path, file_path, repo_info):
        """Determine if a file is a test file"""
        relative_path = str(file_path.relative_to(repo_path))
        return any(relative_path == test_path for test_path in repo_info['tests_path'])

    def analyze_repo(self, repo_path, repo_info):
        """Analyze a single repository"""
        repo_results = {
            'total_token_count': 0,
            'total_code_lines_count': 0,
            'total_function_count': 0,
            'total_class_count': 0,
            'total_import_count': 0,
        }
        
        for root, _, files in os.walk(repo_path):
            for file in files:
                if file.endswith('.py'):  # Analyze only Python files
                    file_path = Path(root) / file
                    # Skip test files
                    tests_path = [path.split('/')[-1] for path in repo_info['tests_path']]
                    if file in tests_path:
                        continue
                    analyzer = PythonFileAnalyzer(file_path)
                    result = analyzer.analyze()
                    repo_results['total_token_count'] += result['token_count']
                    repo_results['total_code_lines_count'] += result['code_lines_count']
                    repo_results['total_function_count'] += result['function_count']
                    repo_results['total_class_count'] += result['class_count']
                    repo_results['total_import_count'] += result['import_count']

        return repo_results

    def analyze_all_repos(self):
        """Analyze all repositories and save to CSV file"""
        all_results = []
        fieldnames = ['repo_name', 'total_token_count', 'total_code_lines_count', 'total_function_count', 'total_class_count', 'total_import_count']
        total_repos = 0
        sum_values = {
            'total_token_count': 0,
            'total_code_lines_count': 0,
            'total_function_count': 0,
            'total_class_count': 0,
            'total_import_count': 0
        }

        for repo_name, repo_info in self.repos_info.items():
            repo_path = self.repos_base_path / repo_name
            if repo_path.exists():
                result = self.analyze_repo(repo_path, repo_info)
                total_repos += 1
                # Accumulate results to calculate averages
                for key in sum_values:
                    sum_values[key] += result[key]
                
                # Add results to the list
                all_results.append({
                    'repo_name': repo_name,
                    **result
                })
            else:
                print(f"Repository {repo_name} not found in {self.repos_base_path}")

        # Calculate averages
        avg_values = {key: int(round(sum_values[key] / total_repos, 0)) for key in sum_values}
        avg_values['repo_name'] = 'average'

        # Add average values to the results list
        all_results.append(avg_values)

        # Save to CSV file
        output_file = 'repo_stats.csv'
        with open(output_file, 'w', newline='', encoding='utf-8') as csvfile:
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            writer.writeheader()
            writer.writerows(all_results)

        return all_results

# Example usage
if __name__ == "__main__":
    analyzer = RepoAnalyzer('python_repos', 'info_raw.jsonl')
    results = analyzer.analyze_all_repos()
