import subprocess
import time
import os

class Docker:
    def __init__(self, args, logger):
        self.args = args
        self.logger = logger
        self.container_name = args.docker_container_name
        self.target_dir = args.docker_target_dir
        # self.source_dir = ''
    
    def transfer_java_project(self, java_repo_name, debug_suffix=''):
        exec_command = f"rm -rf  {self.target_dir}/{java_repo_name}{debug_suffix}"
        subprocess.run(['docker', 'exec', self.container_name, 'bash', '-c', exec_command], check=False)
        subprocess.run(['docker', 'cp', f'{self.args.results_dir}/java_repos_{self.args.current_time}/{java_repo_name}{debug_suffix}', f'{self.container_name}:{self.target_dir}'], check=True)
    
    def exec_java_testcase(self, java_repo_name, debug_suffix=''):
        now = int(time.time())
        os.makedirs(f"{self.args.results_dir}/exec_results_{self.args.current_time}/{java_repo_name}", exist_ok=True)
        exec_command = f"cd {self.target_dir}/{java_repo_name}{debug_suffix} && mvn clean install"
        try:
            exec_result = subprocess.run(['docker', 'exec', self.container_name, 'bash', '-c', exec_command], text=True, capture_output=True, check=False, timeout=30)
        except subprocess.TimeoutExpired:
            exec_result_text = 'Timeout\n'
            with open(f"{self.args.results_dir}/exec_results_{self.args.current_time}/{java_repo_name}/{now}_{debug_suffix}.txt", 'w', encoding='utf-8') as f:
                f.write(exec_result_text)
            return {
                'status': 'Timeout',
                'output': '',
                'error': '',
                'now': now,
            }

        result = {
            'status': 'Success' if exec_result.returncode == 0 else 'Failed',
            'output': exec_result.stdout,
            'error': exec_result.stderr,
            'now': now,
        }
        exec_result_text = '[Status]\n' + result['status'] + '\n\n[Output]\n' + result['output'] + '\n\n[Error]\n' + result['error']
        with open(f"{self.args.results_dir}/exec_results_{self.args.current_time}/{java_repo_name}/{now}_{debug_suffix}.txt", 'w', encoding='utf-8') as f:
            f.write(exec_result_text)
        return result
    
