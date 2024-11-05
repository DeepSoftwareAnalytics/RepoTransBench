import json
from datetime import datetime
import os
import time
import func_timeout
import requests
from itertools import cycle
from func_timeout import func_set_timeout
import prompts
import pandas as pd
import subprocess
from openai import OpenAI


class Generator:
    def __init__(self, args, logger=None):
        self.logger = logger
        self.args = args
        self.model_name = self.args.model_name
        self.init_conversation()

        if self.model_name in ["gpt-4o", "gpt-3.5-turbo", "gpt-4-0125-preview"]:
            self.load_gpt()
        else:
            self.load_misc()

    def init_conversation(self, repo_info=None):
        self.messages = [
            {"role": "system", "content": prompts.translate.sys.sys_prompt_template},
            {"role": "assistant", "content": ""},
        ]
        if repo_info:
            repo_name = repo_info['repo_path'].replace('/', '___')
            java_repo_name = repo_name.replace('-', '').replace('_', '').replace('.', '').capitalize() + 'Java'
            java_test_paths = repo_info['java_test_paths']
            java_tests_code = ''
            for java_test_path in java_test_paths:
                with open(f"{self.args.repos_dir}/java_repos/{java_repo_name}/src/test/java/{java_test_path}", encoding='utf-8') as f:
                    test_code = f.read()
                java_tests_code += f'### {java_test_path}\n```java\n{test_code}\n```\n\n'
            sys_prompt = prompts.translate.sys.sys_prompt_template.format(java_tests_code=java_tests_code)
            self.messages = [
                {"role": "system", "content": sys_prompt},
                {"role": "assistant", "content": ""},
            ]
    
    def clean_conversation(self):
        self.messages = self.messages[:2]
        

    def load_gpt(self):
        # load api keys
        api_keys_path = 'api_keys.txt'
        with open(api_keys_path, encoding='utf-8') as f:
            api_key_list = f.readlines()
        self.api_key_list = cycle([api_keys.strip() for api_keys in api_key_list])
        self.api_key = next(self.api_key_list)
    
    def load_misc(self):
        api_key_list = [
            # add api keys here
        ]
        self.api_key_list = cycle([api_keys.strip() for api_keys in api_key_list])
        self.api_key = next(self.api_key_list)
    
    def load_ckpt(self):
        pass

    def get_response(self, user_prompt, repo_name, history_conversation=None):
        if history_conversation:
            self.messages = history_conversation

        if True:#self.model_name in ["gpt-4o", "claude-3-5-sonnet", "deepseek-chat"]:
            base_url = 'https://api.openai.com/v1/chat/completions'  # add proxy url here
            if self.model_name in ["deepseek-chat", "Meta-Llama-3.1-405B-Instruct", "Meta-Llama-3.1-70B-Instruct", 
                                   "Meta-Llama-3.1-8B-Instruct", "deepseek-ai/DeepSeek-Coder-V2-Instruct", 
                                   "CodeLlama-34b-Instruct-hf", "CodeLlama-70b-Instruct-hf", "codestral-22B", "claude-3-5-sonnet"]:
                base_url = ''  # add proxy url here
            
            unset_proxy()
            
            self.messages.append({"role": "user", "content": user_prompt})
            data = {
                "model": self.model_name,
                "messages": self.messages,
                "temperature": 0.7,
                # "top_k": 50,
                # "top_p": 0.95,
            }

            cnt = 0
            while True:
                try:
                    self.logger.info(f"Using key: {self.api_key}")
                    self.logger.info(f"Using base url: {base_url.rstrip('/chat/completions')}")
                    headers = {"Authorization": f"Bearer {self.api_key}"}
                    # response = requests.post(base_url, json=data, headers=headers, timeout=180).json()
                    
                    timeout = 80
                    client = OpenAI(
                        api_key = self.api_key,
                        base_url = base_url.rstrip('/chat/completions')
                    )
                    
                    chat_completion = client.chat.completions.create(
                        messages=self.messages,
                        model=self.model_name,
                        temperature=0.7,
                        # top_k=50,
                        # top_p=0.95,
                        timeout=timeout,
                    )
                    content = chat_completion.choices[0].message.content

                    record_conversation(headers, self.model_name, self.messages, content, repo_name)
                    # content = response['choices'][0]['message']['content']
                    break
                except requests.exceptions.Timeout:
                    record_conversation(headers, self.model_name, self.messages, '[Requests Timeout]', repo_name)
                except requests.exceptions.RequestException as e:
                    record_conversation(headers, self.model_name, self.messages, f'[Requests Exception: {e}]', repo_name)
                except KeyError as e:
                    record_conversation(headers, self.model_name, self.messages, f'[KeyError: {e}]', repo_name)
                except Exception as e:
                    record_conversation(headers, self.model_name, self.messages, f'[Exception: {e}]', repo_name)

                time.sleep(5)
                self.api_key = next(self.api_key_list)

                cnt += 1
                if cnt >= 3:
                    return -1
            
            self.messages.append(
                {"role": "assistant", "content": content}
            )

            return content


def set_proxy(proxy_port='127.0.0.1:12000'):
    proxy_url = f"http://0.0.0.0:{proxy_port}"
    subprocess.run(['git', 'config', '--global', 'http.proxy', proxy_url], check=True)
    subprocess.run(['git', 'config', '--global', 'https.proxy', proxy_url], check=True)
    os.environ['http_proxy'] = proxy_url
    os.environ['https_proxy'] = proxy_url

def unset_proxy():
    if 'http_proxy' in os.environ:
        del os.environ['http_proxy']
    if 'https_proxy' in os.environ:
        del os.environ['https_proxy']

def record_conversation(headers, model_name, messages, response, repo_name=None):
    file_path = 'conversation_log/log.json'
    os.makedirs('conversation_log', exist_ok=True)
    if repo_name:
        file_path = f'conversation_log/{repo_name}.json'

    conversation_data = {
        "headers": headers,
        "model_name": model_name,
        "messages": messages,
        "response": response,
        "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    }

    with open(file_path, 'a', encoding='utf-8') as file:
        json.dump(conversation_data, file, ensure_ascii=False, indent=4)
        file.write(',\n')
