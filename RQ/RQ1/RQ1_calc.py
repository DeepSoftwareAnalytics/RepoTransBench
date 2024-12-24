import os
import re
import pandas as pd
import json

def parse_test_results(file_path):
    """
    Parse each txt file to determine if execution was successful, if compilation passed, and the number of passed tests.
    :param file_path: Path to the txt file
    :return: Execution success (boolean), compilation success (boolean), number of passed tests (integer), total number of tests (integer)
    """
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    # Determine if execution was successful
    execution_status = lines[1].strip() if len(lines) > 1 else "Failure"
    is_successful = (execution_status == "Success")

    # Determine if compilation passed and parse pass rate
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


def get_repo_java_name(repo_path):
    """
    Generate the Java repo name based on repo_path from info_raw.jsonl
    :param repo_path: repo_path string
    :return: Generated Java path name
    """
    return ''.join([item.replace('-', '').replace('_', '').replace('.', '').capitalize() for item in repo_path.split('/')]) + 'Java'


def process_execution_results(base_dir, repo_java_name):
    """
    Recursively process execution results in the specified folder, returning statistics for multiple rounds and models.
    :param base_dir: The root path of the current model folder
    :param repo_java_name: The generated repo Java name
    :return: Execution success, pass percentage results for each model and round
    """
    result = {}

    for model_folder in os.listdir(base_dir):
        model_path = os.path.join(base_dir, model_folder)
        if os.path.isdir(model_path):
            for round_folder in os.listdir(model_path):
                round_path = os.path.join(model_path, round_folder)
                if round_folder.startswith("round") and os.path.isdir(round_path):
                    exec_results_path = os.path.join(round_path, "exec_results", repo_java_name)
                    if os.path.exists(exec_results_path):
                        for file_name in os.listdir(exec_results_path):
                            if file_name.endswith("_.txt"):
                                txt_file = os.path.join(exec_results_path, file_name)
                                is_successful, is_compiled, passed_tests, total_tests = parse_test_results(txt_file)
                                column_key = (model_folder, round_folder)
                                if column_key not in result:
                                    result[column_key] = {"success_rate": 0, "pass_percentage": "0/0", "compiled": 0, "test_pass_rate": []}

                                # Update success rate, compilation rate, and pass percentage
                                result[column_key]["success_rate"] = 1 if is_successful and passed_tests == total_tests else 0
                                result[column_key]["pass_percentage"] = f"{passed_tests}/{total_tests}" if total_tests > 0 else "0/0"
                                result[column_key]["compiled"] = 1 if is_compiled else 0

                                # If compiled, record test pass rate
                                if is_compiled and total_tests > 0:
                                    result[column_key]["test_pass_rate"].append(passed_tests / total_tests)
                                else:
                                    # If not compiled, test pass rate is 0
                                    result[column_key]["test_pass_rate"].append(0)

    return result


def calculate_model_summary(success_df, compiled_df, test_pass_rate_df):
    """
    Calculate summary information for each model, including the number of passes for each round, the number of passes if at least one round passes, and the average value over three rounds.
    :param success_df: DataFrame for complete pass rate
    :param compiled_df: DataFrame for compilation pass rate
    :param test_pass_rate_df: DataFrame for test pass rate
    :return: DataFrame containing summary information
    """
    summary = {}

    # Get all models and rounds
    models_rounds = success_df.columns.get_level_values(0).unique()

    for model in models_rounds:
        model_summary = {}
        # Count the number of passes for each round (pass/compiled/test_pass_rate)
        for round_ in success_df[model].columns:
            model_summary[(round_, 'Executable')] = f"{success_df[model][round_].sum()}/100"
            model_summary[(round_, 'Compilable')] = f"{compiled_df[model][round_].sum()}/100"
            
            # Calculate average test pass rate, denominator is 100
            if test_pass_rate_df[model][round_].notna().sum() > 0:
                average_test_pass_rate = test_pass_rate_df[model][round_].sum() * 100 / 100  # Denominator is 100
                model_summary[(round_, 'Average Test Pass Rate')] = f"{average_test_pass_rate:.1f}%"
            else:
                model_summary[(round_, 'Average Test Pass Rate')] = "N/A"

        # print(success_df[model].columns)

        # Calculate average value for three rounds, that is Pass@1
        model_summary[('Pass@1', 'Executable')] = f"{(success_df[model].mean(axis=1).mean() * 100):.2f}%"
        model_summary[('Pass@1', 'Compilable')] = f"{(compiled_df[model].mean(axis=1).mean() * 100):.2f}%"

        # Calculate the union of the results of randomly selected two rounds from three rounds, and take the average of the three situations, that is Pass@2
        # model_summary[('Pass@2', 'Executable')] = f"{(success_df[model][['round_1', 'round_2']].mean(axis=1).mean() * 100):.2f}"
        # model_summary[('Pass@2', 'Compilable')] = f"{(compiled_df[model].mean(axis=1).mean() * 100):.2f}"
        pass_at_2_success = (
            (success_df[model][['round_1', 'round_2']].sum(axis=1) > 0).sum() +
            (success_df[model][['round_1', 'round_3']].sum(axis=1) > 0).sum() +
            (success_df[model][['round_2', 'round_3']].sum(axis=1) > 0).sum()
        ) / 3
        model_summary[('Pass@2', 'Executable')] = f"{pass_at_2_success:.2f}%"
        pass_at_2_compiled = (
            (compiled_df[model][['round_1', 'round_2']].sum(axis=1) > 0).sum() +
            (compiled_df[model][['round_1', 'round_3']].sum(axis=1) > 0).sum() +
            (compiled_df[model][['round_2', 'round_3']].sum(axis=1) > 0).sum()
        ) / 3
        model_summary[('Pass@2', 'Compilable')] = f"{pass_at_2_compiled:.2f}%"

        # print(success_df[model][['round_1', 'round_2']].mean(axis=1).mean())

        # Count the number of passes for three rounds, if any round passes, that is Pass@3
        model_summary[('Pass@3', 'Executable')] = f"{(success_df[model].sum(axis=1) > 0).sum()}%"
        model_summary[('Pass@3', 'Compilable')] = f"{(compiled_df[model].sum(axis=1) > 0).sum()}%"
        
        # Calculate the average test pass rate for three rounds
        average_overall_test_pass_rate = test_pass_rate_df[model].sum().mean() * 100 / 100  # Denominator is 100
        model_summary[('Average Test Pass Rate', 'Average Test Pass Rate')] = f"{average_overall_test_pass_rate:.1f}%" if not pd.isna(average_overall_test_pass_rate) else "N/A"

        summary[model] = model_summary

    # Create a DataFrame with multi-level indexing
    summary_df = pd.DataFrame(summary).T
    summary_df.columns = pd.MultiIndex.from_tuples(summary_df.columns)

    return summary_df


def main():
    base_dir = '../../experiment_results'
    info_file = '../../repos/info_raw.jsonl'
    results = {}

    # Read info_raw.jsonl file
    with open(info_file, 'r', encoding='utf-8') as f:
        for line in f:
            repo_info = json.loads(line)
            repo_java_name = get_repo_java_name(repo_info['repo_path'])
            results[repo_java_name] = process_execution_results(base_dir, repo_java_name)

    # Prepare results for writing to Excel
    success_data = {}
    percentage_data = {}
    compiled_data = {}
    test_pass_rate_data = {}

    for repo_name, result_dict in results.items():
        for (model, round_), result in result_dict.items():
            if (model, round_) not in success_data:
                success_data[(model, round_)] = {}
                percentage_data[(model, round_)] = {}
                compiled_data[(model, round_)] = {}
                test_pass_rate_data[(model, round_)] = {}
            success_data[(model, round_)][repo_name] = result["success_rate"]
            percentage_data[(model, round_)][repo_name] = result["pass_percentage"]
            compiled_data[(model, round_)][repo_name] = result["compiled"]

            # Record test case pass rate, considering non-compilable cases, denominator is 100
            if result["test_pass_rate"]:
                test_pass_rate_data[(model, round_)][repo_name] = sum(result["test_pass_rate"]) / len(result["test_pass_rate"])
            else:
                test_pass_rate_data[(model, round_)][repo_name] = 0  # Non-compilable repo rate is 0

    # Create DataFrame
    success_df = pd.DataFrame(success_data).sort_index(axis=1)
    percentage_df = pd.DataFrame(percentage_data).sort_index(axis=1)
    compiled_df = pd.DataFrame(compiled_data).sort_index(axis=1)
    test_pass_rate_df = pd.DataFrame(test_pass_rate_data).sort_index(axis=1)

    # Calculate model summary information
    summary_df = calculate_model_summary(success_df, compiled_df, test_pass_rate_df)

    # Write to Excel
    with pd.ExcelWriter('RQ1_Results.xlsx') as writer:
        success_df.to_excel(writer, sheet_name='Success Rate')
        percentage_df.to_excel(writer, sheet_name='Pass Percentage')
        compiled_df.to_excel(writer, sheet_name='Compiled Rate')
        test_pass_rate_df.to_excel(writer, sheet_name='Test Pass Rate')
        summary_df.to_excel(writer, sheet_name='Model Summary')

    print("Analysis results have been written to RQ1_Results.xlsx")


if __name__ == "__main__":
    main()
