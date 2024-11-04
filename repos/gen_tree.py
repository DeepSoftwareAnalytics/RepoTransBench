import os

def generate_java_tree(directory, prefix=""):
    """Generate a tree structure for .java files and pom.xml in the given directory."""
    tree = []
    contents = sorted(os.listdir(directory))
    java_files_or_dirs = []
    
    # Filter contents to include only .java files, pom.xml, and directories that contain them
    for name in contents:
        path = os.path.join(directory, name)
        if os.path.isdir(path):
            # Only include directories that contain .java files or pom.xml
            if any(f.endswith('.java') or f == 'pom.xml' for _, _, files in os.walk(path) for f in files):
                java_files_or_dirs.append(name)
        elif name.endswith('.java') or name == 'pom.xml':
            java_files_or_dirs.append(name)

    for index, name in enumerate(java_files_or_dirs):
        path = os.path.join(directory, name)
        connector = "└── " if index == len(java_files_or_dirs) - 1 else "├── "
        tree.append(prefix + connector + name)
        if os.path.isdir(path):
            extension = "    " if index == len(java_files_or_dirs) - 1 else "│   "
            tree.extend(generate_java_tree(path, prefix + extension))
    
    return tree

def save_tree_to_file(tree, file_path):
    """Save the tree structure to a text file."""
    with open(file_path, 'w', encoding='utf-8') as f:
        for line in tree:
            f.write(line + '\n')

def scan_java_projects(java_dir, output_dir):
    """Scan all Java projects and save their directory trees containing only .java files."""
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    
    for project_name in os.listdir(java_dir):
        project_path = os.path.join(java_dir, project_name)
        if os.path.isdir(project_path):
            # Generate the tree structure
            tree = generate_java_tree(project_path)
            
            # Save the tree to a file only if it contains .java files
            if tree:
                output_file = os.path.join(output_dir, f"{project_name}")
                save_tree_to_file(tree, output_file)
                # print(f"Saved tree for project {project_name} to {output_file}")

# Specify the directory containing the Java projects
java_dir = './java_repos'

# Specify the output directory for the tree files
output_dir = 'java_tree'

# Scan the Java projects and generate tree files
scan_java_projects(java_dir, output_dir)
