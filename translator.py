from utils.misc_utils import extract_code
from utils.topological_sort import topological_sort
import prompts
import re
import os

class Translator:
    def __init__(self, args, generator, logger):
        self.args = args
        self.logger = logger
        self.generator = generator
        self.mode_map = {
            'project_level': 0,
            'file_level': 1,
            'func_level': 2
        }
        self.mode = self.mode_map[args.translate_mode]

    def translate(self, repo_info, repo_name, java_repo_name):
        tests_path = [path.replace('\\', '/') for path in repo_info['tests_path']]

        python_project_path = f'{self.args.repos_dir}/python_repos/{repo_name}'
        code_map = extract_code(python_project_path)
        self.logger.info(code_map.keys())

        if 'setup.py' in code_map:
            del code_map['setup.py']
        # Separate test and code files
        test_code_map = {}
        for test_path in tests_path:
            test_code_map[test_path] = code_map[test_path]
            del code_map[test_path]
        path_order_list = topological_sort(code_map)

        self.logger.info(path_order_list)
        self.logger.info(code_map.keys())
        self.logger.info(test_code_map.keys())

        # Read Java project tree
        with open(f'{self.args.repos_dir}/java_tree/{java_repo_name}', encoding='utf-8') as f:
            java_project_tree = f.read()
        # self.logger.info(java_project_tree)
            
        # Read Java test code
        java_test_paths = repo_info['java_test_paths']
        java_tests_code = ''
        for java_test_path in java_test_paths:
            with open(f'{self.args.repos_dir}/java_repos/{java_repo_name}/src/test/java/{java_test_path}', encoding='utf-8') as f:
                java_test_code = f.read()
            java_tests_code += f'### {java_test_path}\n```java\n{java_test_code}\n```\n\n'

        if self.mode == 0:
            python_project_context = ''
            for path in path_order_list:
                code = code_map[path]
            # for path, code in code_map.items():
                python_project_context += f'### {path}\n```python\n{code}\n```\n\n'
            # self.logger.info(python_project_context)
            # translate2java_prompt = prompts.translate.project_level.project_level_prompt_template.format(python_project_context=python_project_context, java_project_tree=java_project_tree, java_tests_code=java_tests_code)
            translate2java_prompt = prompts.translate.project_level.project_level_prompt_template.format(python_project_context=python_project_context, java_project_tree=java_project_tree)
            response = self.generator.get_response(translate2java_prompt, repo_name)
            if response == -1:
                return -1
            # If not generated completely, continue generating
            cnt = 0
            while True:
                if response.count('```') % 2 == 0:
                    break
                continue_response = self.generator.get_response('Continue', repo_name)
                if continue_response == -1:
                    return -1
                response = '\n'.join(response.splitlines()[:-1]) + '\n' + '\n'.join(continue_response.splitlines()[1:] if continue_response else [])
                
                cnt += 1
                if cnt > 5:
                    response += '```'
                    break

            with open(f"{self.args.results_dir}/translate_response_{self.args.current_time}/{java_repo_name}.txt", 'w', encoding='utf-8') as f:
                f.write(response)
            # Extract Java project code
            path_code_block_pattern = re.compile(r"### (.*?)\n```(java|xml)\n(.*?)```", re.DOTALL)
            matches = path_code_block_pattern.finditer(response)
            for match in matches:
                path, file_type, code = match.groups()
                path = path.strip().strip('`')
                if 'src/test/java' in path:
                    continue
                try:
                    if file_type == 'java':
                        os.makedirs(f"{self.args.results_dir}/java_repos_{self.args.current_time}/{java_repo_name}/{os.path.dirname(path)}", exist_ok=True)
                    with open(f"{self.args.results_dir}/java_repos_{self.args.current_time}/{java_repo_name}/{path}", 'w', encoding='utf-8') as f:
                        f.write(code)
                except:
                    pass

        elif self.mode == 1:
            python_project_context = ''
            java_project_context = ''
            for path in path_order_list:
                code = code_map[path]
            # for path, code in code_map.items():
                self.generator.clean_conversation()
                python_file_context = f'### {path}\n```python\n{code}\n```\n\n'
                # translate2java_file_level_prompt = prompts.translate.file_level.file_level_java_file_prompt_template.format(python_file_context=python_file_context, java_project_tree=java_project_tree)
                translate2java_file_level_prompt = prompts.translate.file_level.file_level_java_file_prompt_template.format(python_file_context=python_file_context, java_project_tree=java_project_tree)
                response = self.generator.get_response(translate2java_file_level_prompt, repo_name)
                if response == -1:
                    return -1
                # If not generated completely, continue generating
                cnt = 0
                while True:
                    if response.count('```') % 2 == 0:
                        break
                    cnt += 1
                    if cnt > 5:
                        response += '```'
                        break
                    response = '\n'.join(response.splitlines()[:-1]) + '\n' + '\n'.join(self.generator.get_response('Continue', repo_name).splitlines()[1:])

                with open(f"{self.args.results_dir}/translate_response_{self.args.current_time}/{java_repo_name}.txt", 'w', encoding='utf-8') as f:
                    f.write(response)
                # Extract Java project code
                path_code_block_pattern = re.compile(r"### (.*?)\n```(java|xml)\n(.*?)```", re.DOTALL)
                matches = path_code_block_pattern.finditer(response)
                for match in matches:
                    path, file_type, code = match.groups()
                    path = path.strip().strip('`')
                    if 'src/test/java' in path:
                        continue
                    try:
                        if file_type == 'java':
                            os.makedirs(f"{self.args.results_dir}/java_repos_{self.args.current_time}/{java_repo_name}/{os.path.dirname(path)}", exist_ok=True)
                            java_project_context += f'### {path}\n```java\n{code}\n```\n\n'
                        with open(f"{self.args.results_dir}/java_repos_{self.args.current_time}/{java_repo_name}/{path}", 'w', encoding='utf-8') as f:
                            f.write(code)
                    except:
                        pass
            
            ############## Generate XML files
            self.generator.clean_conversation()
            # translate2xml_file_level_prompt = prompts.translate.file_level.file_level_xml_file_prompt_template.format(java_project_context=java_project_context, java_project_tree=java_project_tree)
            translate2xml_file_level_prompt = prompts.translate.file_level.file_level_xml_file_prompt_template.format(java_project_context=java_project_context, java_project_tree=java_project_tree)
            response = self.generator.get_response(translate2xml_file_level_prompt, repo_name)
            if response == -1:
                return -1
            # If not generated completely, continue generating
            cnt = 0
            while True:
                if response.count('```') % 2 == 0:
                    break
                cnt += 1
                if cnt > 2:
                    response += '```'
                    break
                response = '\n'.join(response.splitlines()[:-1]) + '\n' + '\n'.join(self.generator.get_response('Continue', repo_name).splitlines()[1:])
            with open(f"{self.args.results_dir}/translate_response_{self.args.current_time}/{java_repo_name}.txt", 'w', encoding='utf-8') as f:
                f.write(response)
            # Extract Java project code
            path_code_block_pattern = re.compile(r"### (.*?)\n```(java|xml)\n(.*?)```", re.DOTALL)
            matches = path_code_block_pattern.finditer(response)
            for match in matches:
                path, file_type, code = match.groups()
                path = path.strip().strip('`')
                if 'src/test/java' in path:
                    continue
                try:
                    if file_type == 'xml':
                        os.makedirs(f"{self.args.results_dir}/java_repos_{self.args.current_time}/{java_repo_name}/{os.path.dirname(path)}", exist_ok=True)
                        java_project_context += f'### {path}\n```java\n{code}\n```\n\n'
                    with open(f"{self.args.results_dir}/java_repos_{self.args.current_time}/{java_repo_name}/{path}", 'w', encoding='utf-8') as f:
                        f.write(code)
                except:
                    pass

        elif self.mode == 2:
            pass
