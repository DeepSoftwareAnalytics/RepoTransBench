import os
import argparse
from tqdm import tqdm
from datetime import datetime
from translator import Translator
from debuger import Debuger
from docker import Docker
from generator import Generator
from utils.misc_utils import load_test_data, init_result

import logging
logging.basicConfig(format='%(asctime)s - %(levelname)s - %(name)s - %(message)s', datefmt='%m/%d/%Y %H:%M:%S', level=logging.INFO)
logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)


def run(args):
    docker = Docker(args, logger)
    generator = Generator(args, logger)
    translator = Translator(args, generator, logger)
    debuger = Debuger(args, generator, logger)

    repos_info_list = load_test_data(args)
    logger.info(f'Test Dataset Size: {len(repos_info_list)}')

    for i in tqdm(range(len(repos_info_list))):
        repo_info = repos_info_list[i]
        repo_name = repo_info['repo_path'].replace('/', '___')
        generator.init_conversation(repo_info)
        # generator.init_conversation()

        java_repo_name = ''.join([item.replace('-', '').replace('_', '').replace('.', '').capitalize() for item in repo_info['repo_path'].split('/')]) + 'Java'  # java项目名称
        logger.info(java_repo_name)

        if args.enable_translate and not args.enable_history:
            init_result(args, java_repo_name)
            status = translator.translate(repo_info, repo_name, java_repo_name)

            if status == -1:
                continue
        # continue
        docker.transfer_java_project(java_repo_name, args.debug_suffix)
        exec_result = docker.exec_java_testcase(java_repo_name, args.debug_suffix)
        if exec_result['status'] == 'Success':
            logger.info(f"{repo_info['repo_path']} Execute Successfully!")
            continue
        
        logger.info(f"{repo_info['repo_path']} Execute Failed!")
        
        new_debug_suffix = ''
        for _ in range(5):
            generator.clean_conversation()
            # generator.init_conversation()
            new_debug_suffix = debuger.debug(repo_info, repo_name, java_repo_name, exec_result, new_debug_suffix)

            docker.transfer_java_project(java_repo_name, new_debug_suffix)
            exec_result = docker.exec_java_testcase(java_repo_name, new_debug_suffix)
            if exec_result['status'] == 'Success':
                logger.info(f"{repo_info['repo_path']} Execute Successfully!")
                break
            logger.info(f"{repo_info['repo_path']} Execute Failed!")


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    # Basic settings
    parser.add_argument('--model_name', type=str, default="gpt-4o", help="Base model name")

    # Translation settings
    parser.add_argument("--enable_translate", action="store_true", help="Enable translation")
    parser.add_argument("--translate_mode", default="project_level", type=str, choices=["project_level", "file_level", "func_level"], help="Translation mode")
    
    # Debug settings
    parser.add_argument("--enable_debug", action="store_true", help="Enable debug")
    parser.add_argument("--debug_mode", default="direct", type=str, help="Debug mode", choices=['direct', 'filter'])
    parser.add_argument('--debug_suffix', type=str, default="", help="Repo suffix used in debug mode")

    # Other settings
    parser.add_argument("--iter_times", default=1, type=int, help="Number of iterations")
    parser.add_argument("--enable_line_num", action="store_true", help="Enable line number annotation")

    # File path settings
    parser.add_argument("--repos_info_path", default="repos/info_raw.jsonl", type=str, help="Path to repo information file")
    parser.add_argument("--repos_dir", default="./repos", type=str, help="Path where repo source code and test cases are located")
    parser.add_argument("--results_dir", default="./results", type=str, help="Evaluation records")

    # Enable history logging
    parser.add_argument("--enable_history", action="store_true", help="Enable history generation records")
    parser.add_argument("--history_time", default="", type=str, help="History generation record, used for debugging only")

    # Docker configuration
    parser.add_argument("--docker_container_name", default="", type=str, help="Docker container ID")
    parser.add_argument("--docker_target_dir", default="/home/repos", type=str, help="Execution path for repo in Docker container")

    args = parser.parse_args()

    now = datetime.now()
    current_time = now.strftime("%y%m%d_%H%M%S")

    args.current_time = current_time

    args.current_time = args.model_name.replace('/', '_').replace('-', '_') + '_' + args.current_time
    if args.enable_history:
        args.current_time = args.history_time
    logger.info(f'Current Time: {args.current_time}')

    run(args)
