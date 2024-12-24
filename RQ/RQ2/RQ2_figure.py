import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import numpy as np

sns.set(style='whitegrid', font_scale=1.4, font='serif')
sns.set(style='whitegrid', font_scale=1.4, font='Microsoft YaHei')
# sns.set(font_scale=1.2)

# Define the models and their data for Pass@1, Pass@2, Pass@3
model_order = [
    'Llama-3.1-8B-Instruct', 'Llama-3.1-70B-Instruct', 'Llama-3.1-405B-Instruct',
    'DeepSeek-V2.5', 'GPT-3.5-Turbo-16k', 'GPT-4', 'GPT-4o',
    'Claude-3.5-Sonnet', 'CodeLlama-34B-Instruct', 'Codestral-22B', 
    'DeepSeek-Coder-V2-Instruct'
]

iterations = ['Origin', '1', '2', '3', '4', '5']

# Define additional data for Success@k metrics
success_1 = np.array([
    [0.00, 0.00, 0.33, 0.33, 0.33, 0.33],
    [1.33, 3.00, 3.33, 3.67, 4.67, 6.00],
    [2.67, 3.67, 4.67, 7.67, 9.00, 10.67],
    [3.00, 4.33, 6.67, 9.33, 10.67, 12.00],
    [0.67, 2.33, 3.33, 4.00, 5.33, 7.00],
    [2.33, 4.67, 6.67, 7.00, 9.00, 10.00],
    [4.00, 6.33, 12.00, 15.33, 18.67, 21.00],
    [7.33, 10.00, 13.33, 14.33, 15.33, 16.00],
    [0.00, 0.00, 0.00, 0.00, 0.00, 0.00],
    [1.74, 2.78, 4.17, 4.17, 4.86, 5.21],
    [4.86, 10.07, 14.24, 15.28, 15.97, 17.71]
])

success_2 = np.array([
    [0.00, 0.00, 0.67, 0.67, 0.67, 0.67],
    [2.33, 5.00, 5.67, 6.00, 8.00, 10.67],
    [3.33, 5.33, 7.33, 12.33, 14.67, 17.00],
    [4.67, 6.33, 10.33, 15.00, 16.67, 18.00],
    [1.00, 3.33, 4.67, 6.00, 8.33, 11.33],
    [3.33, 7.67, 10.67, 11.33, 15.00, 16.33],
    [6.33, 10.67, 19.67, 24.67, 28.67, 30.33],
    [10.33, 13.67, 17.67, 18.67, 19.67, 20.67],
    [0.00, 0.00, 0.00, 0.00, 0.00, 0.00],
    [3.47, 5.21, 7.99, 7.99, 9.38, 9.72],
    [6.60, 13.19, 19.44, 20.49, 21.18, 21.88]
])

success_3 = np.array([
    [0.00, 0.00, 1.00, 1.00, 1.00, 1.00],
    [3.00, 6.00, 7.00, 7.00, 10.00, 14.00],
    [4.00, 7.00, 10.00, 16.00, 19.00, 22.00],
    [6.00, 8.00, 13.00, 19.00, 21.00, 23.00],
    [1.00, 4.00, 6.00, 8.00, 11.00, 15.00],
    [4.00, 10.00, 14.00, 15.00, 20.00, 21.00],
    [8.00, 14.00, 24.00, 30.00, 34.00, 34.00],
    [12.00, 15.00, 20.00, 21.00, 22.00, 23.00],
    [0.00, 0.00, 0.00, 0.00, 0.00, 0.00],
    [5.21, 7.29, 11.46, 11.46, 13.54, 13.54],
    [7.29, 15.62, 22.92, 22.92, 23.96, 25.00]
])


# Sample data for Pass@1, Pass@2, and Pass@3
build_1 = np.array([
    [0.00, 1.00, 1.67, 1.67, 1.67, 1.67],
    [2.67, 6.33, 11.67, 17.33, 21.33, 24.33],
    [5.67, 12.67, 20.00, 25.00, 27.67, 32.33],
    [12.00, 23.67, 29.67, 32.67, 34.00, 36.00],
    [2.33, 7.67, 14.33, 21.33, 25.00, 27.67],
    [4.33, 9.33, 13.00, 15.33, 17.33, 18.67],
    [9.00, 23.00, 36.00, 41.00, 47.00, 51.33],
    [28.33, 38.00, 45.67, 50.33, 52.33, 54.33],
    [0.37, 0.74, 0.74, 0.74, 0.74, 0.74],
    [4.51, 9.37, 11.81, 14.93, 17.71, 22.22],
    [16.32, 35.07, 45.83, 49.65, 53.13, 54.86]
])

build_2 = np.array([
    [0.00, 2.00, 3.33, 3.33, 3.33, 3.33],
    [4.33, 10.33, 19.00, 26.67, 32.33, 37.33],
    [8.00, 19.67, 30.00, 37.00, 39.33, 45.33],
    [17.00, 30.00, 36.67, 40.00, 42.00, 45.00],
    [4.00, 11.67, 21.67, 33.33, 38.67, 42.00],
    [7.00, 15.67, 22.00, 26.33, 29.33, 31.67],
    [14.67, 36.33, 52.67, 57.33, 63.00, 67.00],
    [37.67, 48.67, 58.33, 63.67, 65.33, 68.33],
    [0.74, 1.48, 1.48, 1.48, 1.48, 1.48],
    [8.68, 16.67, 19.79, 25.35, 29.17, 34.72],
    [21.18, 46.87, 57.29, 60.76, 63.54, 65.28]
])

build_3 = np.array([
    [0.00, 3.00, 5.00, 5.00, 5.00, 5.00],
    [6.00, 13.00, 24.00, 33.00, 39.00, 45.00],
    [10.00, 25.00, 36.00, 45.00, 46.00, 53.00],
    [20.00, 34.00, 41.00, 46.00, 48.00, 52.00],
    [5.00, 15.00, 27.00, 42.00, 48.00, 51.00],
    [9.00, 20.00, 28.00, 34.00, 37.00, 40.00],
    [19.00, 46.00, 60.00, 63.00, 69.00, 73.00],
    [42.00, 54.00, 64.00, 68.00, 69.00, 72.00],
    [1.11, 2.22, 2.22, 2.22, 2.22, 2.22],
    [12.50, 21.88, 25.00, 32.29, 36.46, 42.71],
    [25.00, 54.17, 62.50, 65.62, 68.75, 69.79]
])

# Create a DataFrame for plotting
data = {
    'Model': np.repeat(model_order, 6),
    'Iteration': iterations * len(model_order),
    'Success@1': success_1.flatten(),
    'Success@2': success_2.flatten(),
    'Success@3': success_3.flatten(),
    'Build@1': build_1.flatten(),
    'Build@2': build_2.flatten(),
    'Build@3': build_3.flatten()
}
df = pd.DataFrame(data)

# Set iteration and model as ordered categorical types for correct ordering in the heatmap
df['Iteration'] = pd.Categorical(df['Iteration'], categories=iterations, ordered=True)
df['Model'] = pd.Categorical(df['Model'], categories=model_order, ordered=True)

# Plot heatmaps for Pass@1, Pass@2, and Pass@3
fig, axs = plt.subplots(2, 3, figsize=(20, 12))
# fig.suptitle('Pass@1, Pass@2, Pass@3 Performance Heatmaps (Simplified Labels)')

cbar_ax1 = fig.add_axes([.91, .55, .02, .35])
cbar_ax2 = fig.add_axes([.91, .1, .02, .35])

# Pass@1 Heatmap with y-axis labels
sns.heatmap(df.pivot(index='Model', columns='Iteration', values='Success@1'), annot=True, fmt=".2f", cmap='OrRd', ax=axs[0, 0], cbar=False)
axs[0, 0].set_title('Success@1 (%)', fontdict={'size': 18})
axs[0, 0].set_xlabel('Iteration')
axs[0, 0].set_ylabel('')  # Display model names here

# Pass@2 Heatmap without y-axis labels
sns.heatmap(df.pivot(index='Model', columns='Iteration', values='Success@2'), annot=True, fmt=".2f", cmap='OrRd', ax=axs[0, 1], cbar=False, yticklabels=False)
axs[0, 1].set_title('Success@2 (%)', fontdict={'size': 18})
axs[0, 1].set_xlabel('Iteration')
axs[0, 1].set_ylabel('')

# Pass@3 Heatmap without y-axis labels
sns.heatmap(df.pivot(index='Model', columns='Iteration', values='Success@3'), annot=True, fmt=".2f", cmap='OrRd', ax=axs[0, 2], cbar=True, yticklabels=False, cbar_ax=cbar_ax1)
axs[0, 2].set_title('Success@3 (%)', fontdict={'size': 18})
axs[0, 2].set_xlabel('Iteration')
axs[0, 2].set_ylabel('')

# Pass@1 Heatmap with y-axis labels
sns.heatmap(df.pivot(index='Model', columns='Iteration', values='Build@1'), annot=True, fmt=".2f", cmap='YlGnBu', ax=axs[1, 0], cbar=False)
axs[1, 0].set_title('Build@1 (%)', fontdict={'size': 18})
axs[1, 0].set_xlabel('Iteration')
axs[1, 0].set_ylabel('')  # Display model names here

# Pass@2 Heatmap without y-axis labels
sns.heatmap(df.pivot(index='Model', columns='Iteration', values='Build@2'), annot=True, fmt=".2f", cmap='YlGnBu', ax=axs[1, 1], cbar=False, yticklabels=False)
axs[1, 1].set_title('Build@2 (%)', fontdict={'size': 18})
axs[1, 1].set_xlabel('Iteration')
axs[1, 1].set_ylabel('')

# Pass@3 Heatmap without y-axis labels
sns.heatmap(df.pivot(index='Model', columns='Iteration', values='Build@3'), annot=True, fmt=".2f", cmap='YlGnBu', ax=axs[1, 2], cbar=True, yticklabels=False, cbar_ax=cbar_ax2)
axs[1, 2].set_title('Build@3 (%)', fontdict={'size': 18})
axs[1, 2].set_xlabel('Iteration')
axs[1, 2].set_ylabel('')

plt.tight_layout(rect=[0, 0.03, 0.9, 0.95])
# plt.show()
plt.savefig('RQ2.pdf', format='pdf')
