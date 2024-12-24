from utils.misc_utils import extract_code, build_tree_string, replace_private_with_public
import prompts
import re
import os
import shutil
import ast

class Debuger:
    def __init__(self, args, generator, logger):
        self.args = args
        self.logger = logger
        self.generator = generator
        self.mode_map = {
            'direct': 0,
            'filter': 1,
        }
        self.mode = self.mode_map[args.debug_mode]

    def debug(self, repo_info, repo_name, java_repo_name, exec_result, debug_suffix=''):
        now = exec_result['now']
        tests_path = [path.replace('\\', '/') for path in repo_info['tests_path']]

        with open(f"{self.args.results_dir}/exec_results_{self.args.current_time}/{java_repo_name}/{now}_{debug_suffix}.txt", encoding='utf-8') as f:
            exec_result_text = f.read()

        code_map = extract_code(f"{self.args.results_dir}/java_repos_{self.args.current_time}/{java_repo_name}{debug_suffix}", language='java')
        
        # Filename after debugging
        new_debug_suffix = 'Debug0' if not debug_suffix else 'Debug' + str(int(debug_suffix[5:])+1)
        new_java_repo_name = java_repo_name + new_debug_suffix
        shutil.copytree(f"{self.args.results_dir}/java_repos_{self.args.current_time}/{java_repo_name}{debug_suffix}", f"{self.args.results_dir}/java_repos_{self.args.current_time}/{new_java_repo_name}")

        java_project_context = ''
        for path, code in code_map.items():
            java_project_context += f'### {path}\n```java\n{code}\n```\n\n'
        
        java_project_tree = build_tree_string(f"{self.args.results_dir}/java_repos_{self.args.current_time}/{java_repo_name}{debug_suffix}")

        if self.mode == 0:
            java_debug_direct_prompt = prompts.debug.direct.debug_direct_prompt_template.format(java_project_context=java_project_context, exec_result_text=exec_result_text)
            response = self.generator.get_response(java_debug_direct_prompt, repo_name)
            if response.count('```') % 2 == 0:
                 response += '```'

            with open(f"{self.args.results_dir}/debug_response_{self.args.current_time}/{java_repo_name}.txt", 'w', encoding='utf-8') as f:
                f.write(response)
            # Extract java project code
            path_code_block_pattern = re.compile(r"### (.*?)\n```(java|xml)\n(.*?)```", re.DOTALL)
            matches = path_code_block_pattern.finditer(response)
            for match in matches:
                path, file_type, code = match.groups()
                path = path.strip()
                if 'src/test/java' in path:
                    continue
                if file_type == 'java':
                    os.makedirs(f"{self.args.results_dir}/java_repos_{self.args.current_time}/{new_java_repo_name}/{os.path.dirname(path)}", exist_ok=True)
                with open(f"{self.args.results_dir}/java_repos_{self.args.current_time}/{new_java_repo_name}/{path}", 'w', encoding='utf-8') as f:
                    f.write(code)
        
        elif self.mode == 1:
            filter_exec_result_text = ''
            add_flag = False
            cnt = 0
            for idx, line in enumerate(exec_result_text.splitlines()):
                if line.startswith('[\x1b'):
                    add_flag = False
                    if cnt > 10:
                        break
                if line.startswith('[\x1b[1;31mERROR\x1b[m]'):
                    add_flag = True
                    cnt += 1
                    
                if add_flag:
                    filter_exec_result_text += line + '\n'
            exec_result_text = filter_exec_result_text
            self.logger.info(exec_result_text)

            java_debug_filter_prompt = prompts.debug.filter.debug_filter_prompt_template.format(java_project_context=java_project_context, exec_result_text=exec_result_text)
            # with open('test.txt', 'w', encoding='utf-8') as f:
            #     f.write(java_debug_filter_prompt)
            response = self.generator.get_response(java_debug_filter_prompt, repo_name)
            # If not generated completely, continue generating
            # while True:
            if response == -1:
                response = ''
            for _ in range(5):
                if response.count('```') % 2 == 0:
                    break
                continue_response = self.generator.get_response('Continue', repo_name)
                if continue_response == -1:
                    continue_response = ''
                response = '\n'.join(response.splitlines()[:-1]) + '\n' + '\n'.join(continue_response.splitlines()[1:] if continue_response else [])
            with open(f"{self.args.results_dir}/debug_response_{self.args.current_time}/{java_repo_name}.txt", 'w', encoding='utf-8') as f:
                f.write(response)
            # Extract java project code
            path_code_block_pattern = re.compile(r"### (.*?)\n```(java|xml)\n(.*?)```", re.DOTALL)
            matches = path_code_block_pattern.finditer(response)
            for match in matches:
                path, file_type, code = match.groups()
                path = path.strip()
                if '`' in path:
                    path = path.split('`')[1].strip()
                if 'src/test/java' in path:
                    continue
                try:
                    if file_type not in ['java', 'xml']:
                        continue
                    if file_type == 'java':
                        os.makedirs(f"{self.args.results_dir}/java_repos_{self.args.current_time}/{new_java_repo_name}/{os.path.dirname(path)}", exist_ok=True)
                    with open(f"{self.args.results_dir}/java_repos_{self.args.current_time}/{new_java_repo_name}/{path}", 'w', encoding='utf-8') as f:
                        f.write(code)
                except:
                    pass
