# file path: RepoTransAgent/generator.py

import json
from datetime import datetime
import os
import time
import func_timeout
import requests
from itertools import cycle
from func_timeout import func_set_timeout
# from RepoTransAgent.prompts.system_prompts import get_test_translation_prompt_for_target_language
import pandas as pd
import subprocess
import argparse
from pprint import pprint

import logging
logging.basicConfig(format='%(asctime)s - %(levelname)s - %(name)s - %(message)s', datefmt='%m/%d/%Y %H:%M:%S', level=logging.INFO)
logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)


class Generator:
    def __init__(self, args, logger=None, system_prompt='', config_file="API_KEY.txt"):
        self.logger = logger
        self.config_file = config_file
        self.args = args
        self.model_name = self.args.model_name
        self.system_prompt = system_prompt

        self.load_config()
        self.init_conversation()

    def load_config(self):
        with open(f'RepoTransAgent/{self.config_file}', encoding='utf-8') as f:
            api_keys = f.readlines()
        api_keys = [line.split()[1].strip() for line in api_keys]
        self.api_keys = cycle(api_keys)

        self.api_key = next(self.api_keys)
        self.base_url = "https://api.gptoai.top"
        # self.base_url = "https://35.aigcbest.top"
        # self.base_url = "https://api.minimaxi.com/v1/text/chatcompletion_v2"
        # self.base_url = 'https://www.gptapi.us'
        # self.base_url = 'https://api.siliconflow.cn'
        # self.base_url = 'https://api.just2chat.cn'
        # self.base_url = 'https://www.chataiapi.com'
        self.base_url = 'https://c.bt6.top'
        # self.base_url = 'https://okapi.aicohere.org'
        # self.base_url = 'https://api.agicto.cn'
        # self.base_url = 'https://www.chataiapi.com'
        # self.base_url = 'https://claude.aisonnet.org'
        self.log_path = "conversation_logs/logs.json"

    def init_conversation(self, repo_path=None):
        # self.messages = [{"role": "system", "content": self.system_prompt}]
        self.messages = [
            {
                "role": "system", 
                "content": [
                    {
                        "type": "text",
                        "text": self.system_prompt,
                    },
                ]
            },
        ]
    
    def clean_conversation(self):
        self.messages = []

    def slide_window_conversation(self):
        if len(self.messages) > 2:
            self.messages = self.messages[:1] + self.messages[-2:]
        # if len(self.messages) > 6:
        #     self.messages = self.messages[:1] + self.messages[-6:]

    def get_response(self, repo_name, history_conversation=None):
        if history_conversation:
            self.messages = history_conversation

        if True:
            data = {
                "model": self.model_name,
                "messages": self.messages,
                # "max_tokens": 5,
                # "reasoning_effort": "low",
            }

            retry_cnt = 0
            while True:
                try:
                    self.logger.info(f"Using key: {self.api_key}")
                    self.logger.info(f"Using base url: {self.base_url}")
                    headers = {
                        'Accept': 'application/json',
                        'Authorization': f'Bearer {self.api_key}',
                        'User-Agent': 'Apifox/1.0.0 (https://apifox.com)',
                        'Content-Type': 'application/json'
                    }
                    # print(f"{self.base_url}/v1/chat/completions")
                    # print(len(data['messages']))
                    # pprint(data['messages'])
                    # exit()
                    # print(headers)
                    response = requests.post(f"{self.base_url}/v1/chat/completions", json=data, headers=headers, timeout=180).json()
                    # response = requests.post(f"{self.base_url}", json=data, headers=headers, timeout=180).json()
                    # print(response)
                    # exit()
                    content = response['choices'][0]['message']['content']
                    # print(context)
                    self.record_conversation(headers, self.model_name, self.messages, response, repo_name)
                    break
                except requests.exceptions.Timeout as e:
                    print(e)
                    self.api_key = next(self.api_keys)
                    self.record_conversation(headers, self.model_name, self.messages, '[Requests Timeout]', repo_name)
                except requests.exceptions.RequestException as e:
                    print(e)
                    self.api_key = next(self.api_keys)
                    self.record_conversation(headers, self.model_name, self.messages, f'[Requests Exception: {e}]', repo_name)
                except KeyError as e:
                    print(e)
                    self.api_key = next(self.api_keys)
                    self.record_conversation(headers, self.model_name, self.messages, response, repo_name)
                except Exception as e:
                    print(e)
                    self.api_key = next(self.api_keys)
                    self.record_conversation(headers, self.model_name, self.messages, f'[Exception: {e}]', repo_name)

                time.sleep(5)

                retry_cnt += 1
                if retry_cnt >= 2:
                    return -1
            
            self.messages.append(
                {
                    "role": "assistant", 
                    "content": [
                        {
                            "type": "text",
                            "text": content,
                        },
                    ]
                }
            )

            return content

    def record_conversation(self, headers, model_name, messages, response, repo_name=None):
        log_path = self.log_path
        os.makedirs('conversation_logs', exist_ok=True)
        if repo_name:
            log_path = f'conversation_logs/{repo_name}.json'

        conversation_data = {
            "headers": headers,
            "model_name": model_name,
            "messages": messages,
            "response": response,
            "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        }

        with open(log_path, 'a', encoding='utf-8') as file:
            json.dump(conversation_data, file, ensure_ascii=False, indent=4)
            file.write(',\n')


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('--model_name', type=str, default="gpt-4.1", help="Base model name")
    parser.add_argument('--repo_path', type=str, default="secondary_filter/Python/58daojia-dba_mysqlbinlog_flashback", help="Repository path")
    args = parser.parse_args()

    # system_prompt = get_test_translation_prompt_for_target_language('Python')
    generator = Generator(args, logger, 'How are you?')
    generator.init_conversation(args.repo_path)
    generator.messages.append(
            {
                "role": "user", 
                "content": [
                    {
                        "type": "text",
                        "text": "How are you?",
                    },
                ]
            },
        )   
    response = generator.get_response("test")
    print(response)
