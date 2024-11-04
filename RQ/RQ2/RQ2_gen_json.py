import os
import re
import pandas as pd
import json
import shutil


def parse_test_results(file_path):
    """
    Parse each txt file to determine if execution was successful, if compilation passed, and how many tests passed.
    :param file_path: Path to the txt file
    :return: Whether execution was successful (boolean), whether compilation passed (boolean), number of passed tests (integer), total number of tests (integer)
    """
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    # Determine if execution was successful
    execution_status = lines[1].strip() if len(lines) > 1 else "Failure"
    is_successful = (execution_status == "Success")

    # Determine if compilation passed and parse the pass rate
    is_compiled = False
    total_tests = 0
    passed_tests = 0

    for line in lines:
        if "T E S T S" in line:
            is_compiled = True
        match = re.search(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)", line)
        if match:
            run_tests = int(match.group(1))
            failures = int(match.group(2))
            errors = int(match.group(3))
            passed_tests += run_tests - (failures + errors)
            total_tests += run_tests

    return is_successful, is_compiled, passed_tests, total_tests


def get_repo_repo_name(repo_path):
    """
    Generate the Java repo name based on the repo_path from info_raw.jsonl
    :param repo_path: repo_path string
    :return: Generated Java path name
    """
    return ''.join([item.replace('-', '').replace('_', '').replace('.', '').capitalize() for item in repo_path.split('/')]) + 'Java'


info_file = '../../repos/info_raw.jsonl'
repo_name_list = []
# Read the info_raw.jsonl file
with open(info_file, 'r', encoding='utf-8') as f:
    for line in f:
        repo_info = json.loads(line)
        repo_name = get_repo_repo_name(repo_info['repo_path'])
        repo_name_list.append(repo_name)

model_list = ['llama_3_1_8B', 'llama_3_1_70B', 'llama_3_1_405B', 'deepseek_v2_5', 'deepseekcoder_v2', 
              'codestral_22B', 'codellama_34B', 'gpt_3_5_turbo', 'gpt_4', 'gpt_4o', 'claude_3_5_sonnet']
round_list = ['round_1', 'round_2', 'round_3']
json_list = []

base_dir = '../../experiment_results'

for model in model_list:
    for round in round_list:
        for repo_name in repo_name_list:
                exec_results_dir = os.path.join(base_dir, model, round, "exec_results", repo_name)
                # print(exec_results_path)
                if os.path.isdir(exec_results_dir):
                    exec_results_list = os.listdir(exec_results_dir)
                    for exec_results_file in exec_results_list:
                        exec_results_path = os.path.join(exec_results_dir, exec_results_file)
                        if exec_results_file.endswith('_.txt'):
                            iter = 0
                        elif 'Debug' in exec_results_file:
                            iter = int(re.search(r'Debug(\d)', exec_results_file).group(1)) + 1
                        else:
                            continue
                        is_successful, is_compiled, passed_tests, total_tests = parse_test_results(exec_results_path)
                        json_list.append({
                            'model': model,
                            'round': round,
                            'repo_name': repo_name,
                            'iter': iter,
                            'success': 1 if is_successful else 0,
                            'compiled': 1 if is_compiled else 0,
                            'passed_tests': passed_tests,
                            'total_tests': total_tests,
                        })

with open('result.jsonl', 'w', encoding='utf-8') as f:
    for item in json_list:
        f.write(json.dumps(item) + '\n')


import pandas as pd
import json

# Load the jsonl file
file_path = 'result.jsonl'
data = []

# Reading the jsonl file
with open(file_path, 'r') as file:
    for line in file:
        data.append(json.loads(line))

# Convert the list of dicts to a DataFrame
df = pd.DataFrame(data)

# Create an Excel writer
output_path = 'result.xlsx'
with pd.ExcelWriter(output_path, engine='xlsxwriter') as writer:
    # Get the unique models
    models = df['model'].unique()
    
    # Create a sheet for each model
    for model in models:
        # Filter the dataframe for the specific model
        model_df = df[df['model'] == model].drop(columns=['model'])
        
        # Create a MultiIndex based on repo_name, round, and iter
        model_df.set_index(['repo_name', 'round', 'iter'], inplace=True)
        
        # Write the dataframe to the sheet
        model_df.to_excel(writer, sheet_name=model)
