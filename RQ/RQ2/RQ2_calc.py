import pandas as pd
import numpy as np
import json

# Read the repo_name.txt file
with open('../../repos/repo_name.txt', 'r') as f:
    repo_names = [line.strip() for line in f.readlines()]
# print(len(repo_names))

# Initialize a dictionary to store data
data_list = []
# Read the result.jsonl file
with open('result.jsonl', 'r') as f:
    for line in f:
        line = json.loads(line.strip())
        data_list.append(line)
df = pd.DataFrame(data_list)
# print(df.head())

# Define a function to calculate pass@1, pass@2, pass@3
def calculate_pass(df, model, iter_val, metric='success'):
    # Filter data for the current model and iteration value
    df_iter = df[(df['model'] == model) & (df['iter'] <= iter_val)]
    
    # Group by repo_name and round, then calculate pass@1, pass@2, pass@3
    repo_names = df_iter['repo_name'].unique()
    
    pass_at_1, pass_at_2, pass_at_3 = [], [], []
    
    for repo in repo_names:
        # Get success values for each round for each repo
        repo_data = df_iter[df_iter['repo_name'] == repo]
        
        # Calculate pass@1
        success_round_1 = repo_data[repo_data['round'] == 'round_1'][metric].sum()
        success_round_2 = repo_data[repo_data['round'] == 'round_2'][metric].sum()
        success_round_3 = repo_data[repo_data['round'] == 'round_3'][metric].sum()
        
        pass_at_1.append(np.mean([success_round_1 > 0, success_round_2 > 0, success_round_3 > 0]))
        
        # Calculate pass@2
        success_r12 = (success_round_1 > 0) | (success_round_2 > 0)
        success_r13 = (success_round_1 > 0) | (success_round_3 > 0)
        success_r23 = (success_round_2 > 0) | (success_round_3 > 0)
        
        pass_at_2.append(np.mean([success_r12, success_r13, success_r23]))
        # Calculate pass@3
        success_r123 = (success_round_1 > 0) | (success_round_2 > 0) | (success_round_3 > 0)
        
        pass_at_3.append(success_r123)

    # Keep two decimal places and add percentage sign
    pass_at_1_avg = f"{(sum(pass_at_1) / len(repo_names)) * 100:.2f}%"
    pass_at_2_avg = f"{(sum(pass_at_2) / len(repo_names)) * 100:.2f}%"
    pass_at_3_avg = f"{(sum(pass_at_3) / len(repo_names)) * 100:.2f}%"

    return {
        'pass@1': pass_at_1_avg,
        'pass@2': pass_at_2_avg,
        'pass@3': pass_at_3_avg
    }

# Create an empty DataFrame to store results
def create_pass_results(df, metric='success'):
    results = []
    models = df['model'].unique()

    # Iterate over each model and iteration value
    for model in models:
        model_result = {}
        for iter_val in range(6):  # Assume iter has 0-5, a total of 6 times
            result = calculate_pass(df, model, iter_val, metric=metric)
            # Use tuples for column names (pass@k, iter_x)
            model_result[(f'pass@1', f'iter_{iter_val}')] = result['pass@1']
            model_result[(f'pass@2', f'iter_{iter_val}')] = result['pass@2']
            model_result[(f'pass@3', f'iter_{iter_val}')] = result['pass@3']
        # Add the model column to each row of results and format it as a multi-level index tuple
        model_result[('model', '')] = model
        results.append(model_result)

    # Create a DataFrame with multi-level indexing
    result_df = pd.DataFrame(results)
    
    # Set MultiIndex for columns
    result_df.columns = pd.MultiIndex.from_tuples(result_df.columns)
    return result_df

# Calculate results for success corresponding to pass@1, pass@2, pass@3
success_df = create_pass_results(df, metric='success')

# Calculate results for compiled corresponding to pass@1, pass@2, pass@3
compiled_df = create_pass_results(df, metric='compiled')

# Reorder columns, placing the model column at the front
# First convert columns to a list and adjust the order
columns = list(success_df.columns)
columns.remove(('model', ''))  # Remove 'model' column
sorted_columns = [('model', '')] + sorted(columns, key=lambda x: (x[0], x[1]))  # Sort by Pass@k

# Reset column order
success_df = success_df[sorted_columns]
compiled_df = compiled_df[sorted_columns]

# Save to Excel file
with pd.ExcelWriter('RQ2.xlsx') as writer:
    success_df.to_excel(writer, sheet_name='success', index=True)  # Keep index
    compiled_df.to_excel(writer, sheet_name='compiled', index=True)
