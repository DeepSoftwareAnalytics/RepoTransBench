import os
from tree_sitter import Language, Parser
import tree_sitter_python as tspython
from collections import defaultdict, deque

PYTHON_LANGUAGE = Language(tspython.language())
parser = Parser(PYTHON_LANGUAGE)

# Parse Python files
def extract_imports(file_path, code):
    root_dir = '/'
    tree = parser.parse(bytes(code, 'utf8'))
    root_node = tree.root_node
    imports = set()
    
    # Get the directory of the current file to handle relative imports
    current_dir = os.path.dirname(file_path)
    
    for child in root_node.children:
        if child.type == 'import_statement' or child.type == 'import_from':
            for grandchild in child.children:
                if grandchild.type == 'dotted_name':
                    imported_module = grandchild.text.decode('utf8')
                    if child.type == 'import_from' and child.children[0].text.decode('utf8') == '.':
                        # Handle relative imports
                        relative_import_path = os.path.join(current_dir, imported_module.replace('.', os.sep) + '.py')
                        relative_import_path = os.path.relpath(relative_import_path, root_dir)
                        imports.add(relative_import_path)
                    else:
                        # Handle absolute imports
                        imports.add(imported_module.split('.')[0] + '.py')
    return imports

# Build dependency graph
def build_dependency_graph(code_map):
    all_files = [os.path.basename(path) for path in code_map.keys()]

    dependency_graph = {}
    for path, code in code_map.items():
        imports = extract_imports(path, code)
        # Filter imported files to ensure they are in the project files
        valid_imports = imports.intersection(all_files)
        dependency_graph[path] = valid_imports
    return dependency_graph

def topological_sort(code_map):
    dependency_graph = build_dependency_graph(code_map)
    # Initialize the in-degree of each file
    in_degree = {file: 0 for file in dependency_graph}
    adj_list = defaultdict(list)

    # Build in-degree table and adjacency list
    for file, dependencies in dependency_graph.items():
        for dep in dependencies:
            if dep in in_degree:
                in_degree[dep] += 1
                adj_list[file].append(dep)

    # Enqueue all files with in-degree of 0
    queue = deque([file for file in in_degree if in_degree[file] == 0])
    sorted_files = []

    while queue:
        file = queue.popleft()
        sorted_files.append(file)

        # Traverse all files connected to the current file
        for dep in adj_list[file]:
            in_degree[dep] -= 1
            if in_degree[dep] == 0:
                queue.append(dep)

    # If the number of sorted files does not equal the number of nodes in the graph, it indicates a cycle
    if len(sorted_files) != len(dependency_graph):
        raise ValueError("Graph has at least one cycle")

    return sorted_files[::-1]  # Reverse order to place dependent files first
