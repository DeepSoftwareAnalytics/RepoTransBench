import json
import os
import shutil
import subprocess

def load_test_data(args):
    repos_info_path = args.repos_info_path
    with open(repos_info_path, encoding='utf-8') as f:
        lines = f.readlines()
    repos_info_list = [json.loads(line) for line in lines]
    return repos_info_list


def init_result(args, java_repo_name):
    results_dir = args.results_dir
    os.makedirs(f'{results_dir}/java_repos_{args.current_time}/{java_repo_name}/src/test', exist_ok=True)
    src_dir = f'{args.repos_dir}/java_repos/{java_repo_name}/src/test'
    dst_dir = f'{results_dir}/java_repos_{args.current_time}/{java_repo_name}/src/test'
    shutil.copytree(src_dir, dst_dir, dirs_exist_ok=True)

    os.makedirs(f'{results_dir}/translate_response_{args.current_time}', exist_ok=True)
    os.makedirs(f'{results_dir}/debug_response_{args.current_time}', exist_ok=True)
    os.makedirs(f'{results_dir}/exec_results_{args.current_time}', exist_ok=True)


def extract_code(project_path, language='python'):
    code_dict = {}

    if language == 'java':
        for root, _, files in os.walk(project_path):
            for file in files:
                file_path = os.path.join(root, file)
                relative_path = os.path.relpath(file_path, project_path)
                with open(file_path, 'r', encoding='utf-8') as f:
                    relative_path = relative_path.replace('\\', '/')
                    code_dict[relative_path] = f.read()

    elif language == 'python':
        for root, _, files in os.walk(project_path):
            for file in files:
                if file.endswith('.py'):
                    file_path = os.path.join(root, file)
                    relative_path = os.path.relpath(file_path, project_path)
                    with open(file_path, 'r', encoding='utf-8') as f:
                        relative_path = relative_path.replace('\\', '/')
                        code_dict[relative_path] = f.read()

    return code_dict


def build_tree_string(root, prefix=""):
    tree_string = ""
    files = os.listdir(root)
    files.sort()

    for count, filename in enumerate(files):
        path = os.path.join(root, filename)
        is_last = count == len(files) - 1
        connector = "└── " if is_last else "├── "

        if os.path.isdir(path):
            tree_string += f"{prefix}{connector}{filename}/\n"
            new_prefix = f"{prefix}    " if is_last else f"{prefix}│   "
            tree_string += build_tree_string(path, new_prefix)
        else:
            if filename.endswith(".java") or filename.endswith(".xml"):
                tree_string += f"{prefix}{connector}{filename}\n"

    return tree_string


from tree_sitter import Language, Parser
import tree_sitter_java as tsjava

JAVA_LANGUAGE = Language(tsjava.language())
parser = Parser(JAVA_LANGUAGE)

def get_all_nodes(node):
    """
    Get all child nodes, including the current node
    """
    nodes = [node]
    for child in node.children:
        nodes.extend(get_all_nodes(child))
    return nodes

def replace_private_with_public(java_code):
    tree = parser.parse(bytes(java_code, "utf8"))
    root_node = tree.root_node

    # Get all nodes
    nodes = get_all_nodes(root_node)

    # Locate modifiers
    edits = []
    for node in nodes:
        if node.type == 'modifiers' and b'private' in node.text:
            start, end = node.start_byte, node.end_byte
            new_text = node.text.replace(b'private', b'public')
            edits.append((start, end, new_text))

    # Generate modified code
    modified_code = bytearray(java_code, "utf8")
    for start, end, replacement in reversed(edits):
        modified_code[start:end] = replacement

    return modified_code.decode("utf8")
