import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from scipy.stats import ttest_ind

sns.set(style='whitegrid', font_scale=1.4, font='serif') # Microsoft YaHei
sns.set(style='whitegrid', font_scale=1.4, font='Microsoft YaHei') # Microsoft YaHei

# Load the repo_info CSV
repo_info_path = '../../repos/repo_stats_v2.csv'
repo_info_df = pd.read_csv(repo_info_path)
repo_info_df['repo_name'] = repo_info_df['repo_name'].apply(lambda x: ''.join([item.replace('-', '').replace('_', '').replace('.', '').capitalize() for item in x.split('/')]) + 'Java')
repo_info_df['repo_name'] = repo_info_df['repo_name'].apply(lambda x : x.capitalize())
# print(repo_info_df.head())

# Load the result JSONL file
results_path = '../RQ2/result.jsonl'
results_df = pd.read_json(results_path, lines=True)
results_df['repo_name'] = results_df['repo_name'].apply(lambda x : x.capitalize())
results_df['Avg.PR'] = results_df.apply(lambda x: (x['passed_tests'] / x['total_tests']) if x['total_tests'] != 0 else 0, axis=1).round(2)
# print(results_df.tail(5))

# Group by repo_name and model to process success and compiled
grouped_results = results_df.groupby(['repo_name', 'model']).agg({
    'success': 'max',
    'compiled': 'max'
}).reset_index()

# Merge repo_info and processed results
merged_df = pd.merge(repo_info_df, grouped_results, on='repo_name', how='inner')
print(merged_df.head())

# Filter results for iter == 0
results_df_iter0 = results_df[results_df['iter'] == 0]
grouped_results_iter0 = results_df_iter0.groupby(['repo_name', 'model']).agg({
    'success': 'max',
    'compiled': 'max'
}).reset_index()
merged_df_iter0 = pd.merge(repo_info_df, grouped_results_iter0, on='repo_name', how='inner')

merged_df = merged_df[merged_df['total_token_count'] <= 8000]
merged_df = merged_df[merged_df['total_code_lines_count'] <= 800]
merged_df = merged_df[merged_df['total_function_count'] <= 80]
# merged_df = merged_df[merged_df['total_import_count'] <= 100]
merged_df_iter0 = merged_df_iter0[merged_df_iter0['total_token_count'] <= 8000]
merged_df_iter0 = merged_df_iter0[merged_df_iter0['total_code_lines_count'] <= 800]
merged_df_iter0 = merged_df_iter0[merged_df_iter0['total_function_count'] <= 80]
# merged_df_iter0 = merged_df_iter0[merged_df_iter0['total_import_count'] <= 100]

# Define complexity columns for the box plots
complexity_columns = ['total_token_count', 'total_code_lines_count', 'total_function_count', 'total_import_count']

# Prepare data for plotting by adding conditions
plot_data = []

# For each complexity metric, add all conditions and success/compiled outcomes
for column in complexity_columns:
    for condition, df in zip(
        ['success_all_iter', 'success_iter0', 'compiled_all_iter', 'compiled_iter0'],
        [merged_df, merged_df_iter0, merged_df, merged_df_iter0]
    ):
        for outcome in [0, 1]:
            data = df[df['success'] == outcome] if 'success' in condition else df[df['compiled'] == outcome]
            plot_data.extend([
                {'Complexity Metric': column, 'Condition': condition, 'Outcome': outcome, 'Value': v} 
                for v in data[column]
            ])

# Convert plot_data to a DataFrame for easy plotting
plot_df = pd.DataFrame(plot_data)

# Define a custom color palette for each group in each subplot
group_colors = [
    ['#aec7e8', '#1f77b4'],  # Colors for the first group (success_all_iter)
    ['#ffbb78', '#ff7f0e'],  # Colors for the second group (success_iter0)
    ['#98df8a', '#2ca02c'],  # Colors for the third group (compiled_all_iter)
    ['#ff9896', '#d62728']   # Colors for the fourth group (compiled_iter0)
]

# Set up the figure for subplots (1x4 grid: 1 row, 4 columns for each complexity metric)
fig, axes = plt.subplots(1, 4, figsize=(24, 6))
plot_titles = ['# Tokens', '# Lines', '# Functions', '# Imports']
xticks = ['Success-Debug', 'Success-NonDebug', 'Build-Debug', 'Build-NonDebug']

# Set font size for all text elements
plt.rcParams.update({'font.size': 13})

# Plot each complexity metric in its own subplot
for i, column in enumerate(complexity_columns):
    ax = axes[i]
    condition_data = plot_df[plot_df['Complexity Metric'] == column]
    sns.boxplot(
        data=condition_data, 
        x='Condition', y='Value', hue='Outcome', 
        ax=ax, 
        # palette=group_colors[j],  # Set the color palette for each group
        showfliers=True,
    )
    print(ax.patches)
    # Change colors after plotting
    cnt = 0
    for j, patch in enumerate(ax.patches):
        if isinstance(patch, plt.Rectangle):
            continue
        print(patch)
        patch.set_facecolor(group_colors[cnt // 2][cnt % 2])
        cnt += 1

    ax.set_title(plot_titles[i], fontsize=24)
    ax.set_xlabel('')
    ax.set_ylabel('')
    ax.tick_params(axis='y', labelsize=16)  # Set font size for y-axis ticks
    ax.set_xticklabels('')  # Set custom x-axis labels

# Remove individual legends from each subplot
for ax in axes:
    ax.legend_.remove()

# Add a single legend below all subplots with simplified labels
custom_handles = [
    plt.Line2D([0], [0], color=group_colors[0][0], lw=4, label='w/ Debug & Non Success'),
    plt.Line2D([0], [0], color=group_colors[0][1], lw=4, label='w/ Debug & Success'),
    plt.Line2D([0], [0], color=group_colors[1][0], lw=4, label='w/o Debug & Non Success'),
    plt.Line2D([0], [0], color=group_colors[1][1], lw=4, label='w/o Debug & Success'),
    plt.Line2D([0], [0], color=group_colors[2][0], lw=4, label='w/ Debug & Non Build'),
    plt.Line2D([0], [0], color=group_colors[2][1], lw=4, label='w/ Debug & Build'),
    plt.Line2D([0], [0], color=group_colors[3][0], lw=4, label='w/o Debug & Non Build'),
    plt.Line2D([0], [0], color=group_colors[3][1], lw=4, label='w/o Debug & Build'),
]

fig.legend(handles=custom_handles, loc='lower center', ncol=4, fontsize=18)

# Adjust layout and show plot
plt.tight_layout(rect=[0, 0.13, 1, 1])  # Leave space at the bottom for the legend
plt.savefig('RQ3.pdf', format='pdf')
