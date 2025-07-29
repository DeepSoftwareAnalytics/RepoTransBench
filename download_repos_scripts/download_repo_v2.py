#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
GitHub仓库收集脚本 - 修复版
修复了搜索查询和星数范围问题
"""

import os
import json
import time
import shutil
import subprocess
from pathlib import Path
from typing import Dict, List, Optional, Set, Tuple
import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

class GitHubRepoCollector:
    def __init__(self, 
                 token_file: str = "GITHUB_TOKEN.txt",
                 base_dir: str = "./github_repos_v2",
                 min_stars: int = 50,
                 max_size_mb: int = 1,
                 repos_per_language: int = 5000,
                 statistics_file: str = "repo_statistics_v2.jsonl"):
        
        self.token_file = token_file
        self.base_dir = Path(base_dir)
        self.min_stars = min_stars
        self.max_size_kb = max_size_mb * 1024  # 转换为KB
        self.repos_per_language = repos_per_language
        self.statistics_file = statistics_file
        
        # 修复后的语言配置 - 从低星数开始，查询更宽松
        # self.language_configs = {
            # "Rust": {
            #     "query_template": "language:rust",  # 简化查询，Rust项目通常都有Cargo.toml
            #     "star_ranges": [(50, 200), (201, 500), (501, 1000), (1001, 2000), (2001, 5000), (5001, 10000), (10001, None)]
            # },
            # "Go": {
            #     "query_template": "language:go",  # Go模块化项目通常都有go.mod
            #     "star_ranges": [(50, 200), (201, 500), (501, 1000), (1001, 2000), (2001, 5000), (5001, 10000), (10001, None)]
            # },
            # "JavaScript": {
            #     "query_template": "language:javascript filename:package.json",
            #     "star_ranges": [(50, 200), (201, 500), (501, 1000), (1001, 2000), (2001, 5000), (5001, 10000), (10001, None)]
            # },
        #     "Python": {
        #         "query_template": "language:python (filename:requirements.txt OR filename:pyproject.toml OR filename:setup.py)",
        #         "star_ranges": [(50, 200), (201, 500), (501, 1000), (1001, 2000), (2001, 5000), (5001, 10000), (10001, None)]
        #     },
        #     "Java": {
        #         "query_template": "language:java (filename:pom.xml OR filename:build.gradle)",  # 增加Gradle支持
        #         "star_ranges": [(50, 200), (201, 500), (501, 1000), (1001, 2000), (2001, 5000), (5001, 10000), (10001, None)]
        #     },
        #     "C#": {
        #         "query_template": "language:c# (filename:csproj OR filename:packages.config OR filename:project.json)",  # 修复通配符问题
        #         "star_ranges": [(50, 200), (201, 500), (501, 1000), (1001, 2000), (2001, 5000), (5001, 10000), (10001, None)]
        #     },
        #     "C++": {
        #         "query_template": "language:cpp (filename:CMakeLists.txt OR filename:conanfile.txt OR filename:vcpkg.json OR filename:Makefile)",
        #         "star_ranges": [(50, 150), (151, 300), (301, 600), (601, 1200), (1201, 2500), (2501, 5000), (5001, None)]
        #     },
        #     "C": {
        #         "query_template": "language:c (filename:Makefile OR filename:CMakeLists.txt OR filename:configure.ac)",
        #         "star_ranges": [(50, 150), (151, 300), (301, 600), (601, 1200), (1201, 2500), (2501, 5000), (5001, None)]
        #     }
        # }
        # 如果你特别需要包含包管理工具的项目，可以使用这个更有针对性的配置：
        self.language_configs = {
            "Python": {
                # 使用OR条件，包含常见的Python包管理文件
                "query_template": "language:python (requirements.txt OR pyproject.toml OR setup.py OR setup.cfg OR Pipfile OR poetry.lock)",
                "star_ranges": [(10, 50), (51, 100), (101, 200), (201, 500), (501, 1000), (1001, 2000), (2001, 5000), (5001, None)]
            },
            "Java": {
                # Maven和Gradle项目
                "query_template": "language:java (pom.xml OR build.gradle OR gradle.properties)",
                "star_ranges": [(10, 50), (51, 100), (101, 200), (201, 500), (501, 1000), (1001, 2000), (2001, 5000), (5001, None)]
            },
            "JavaScript": {
                # Node.js项目
                "query_template": "language:javascript package.json",
                "star_ranges": [(10, 50), (51, 100), (101, 200), (201, 500), (501, 1000), (1001, 2000), (2001, 5000), (5001, None)]
            },
            "C#": {
                # .NET项目
                "query_template": "language:c# (*.csproj OR *.sln OR packages.config OR project.json)",
                "star_ranges": [(10, 50), (51, 100), (101, 200), (201, 500), (501, 1000), (1001, 2000), (2001, 5000), (5001, None)]
            },
            "Go": {
                # Go模块项目
                "query_template": "language:go go.mod",
                "star_ranges": [(10, 50), (51, 100), (101, 200), (201, 500), (501, 1000), (1001, 2000), (2001, 5000), (5001, None)]
            },
            "Rust": {
                # Cargo项目
                "query_template": "language:rust Cargo.toml",
                "star_ranges": [(10, 50), (51, 100), (101, 200), (201, 500), (501, 1000), (1001, 2000), (2001, 5000), (5001, None)]
            }
        }
        
        # 加载GitHub tokens
        self.tokens = self._load_tokens()
        self.current_token_index = 0
        
        # 创建目录结构
        self.base_dir.mkdir(exist_ok=True)
        self.progress_file = self.base_dir / "progress.json"
        
        # 初始化HTTP会话
        self.session = self._create_session()
        
        # 加载进度
        self.progress = self._load_progress()
        
        # 加载已统计的仓库列表
        self.existing_repos = self._load_existing_repos()
    
    def _load_existing_repos(self) -> Set[str]:
        """从repo_statistics_v2.jsonl文件加载已统计的仓库名称"""
        existing_repos = set()
        statistics_path = Path(self.statistics_file)
        
        if not statistics_path.exists():
            print(f"统计文件不存在: {self.statistics_file}")
            return existing_repos
        
        try:
            with open(statistics_path, 'r', encoding='utf-8') as f:
                line_count = 0
                for line in f:
                    line = line.strip()
                    if not line:
                        continue
                    
                    try:
                        data = json.loads(line)
                        # 提取仓库的full_name
                        if 'full_name' in data and data['full_name']:
                            existing_repos.add(data['full_name'])
                            line_count += 1
                        elif 'github_info' in data and data['github_info'] and 'full_name' in data['github_info']:
                            existing_repos.add(data['github_info']['full_name'])
                            line_count += 1
                    except json.JSONDecodeError as e:
                        print(f"解析统计文件行失败: {e}")
                        continue
                
                print(f"从统计文件加载了 {line_count} 个已存在的仓库记录")
                
        except Exception as e:
            print(f"读取统计文件失败: {e}")
        
        return existing_repos
    
    def _load_tokens(self) -> List[str]:
        """从文件加载GitHub tokens"""
        tokens = []
        token_file_path = Path(self.token_file)
        
        if not token_file_path.exists():
            print(f"警告: Token文件 {self.token_file} 不存在")
            return []
        
        try:
            with open(token_file_path, 'r', encoding='utf-8') as f:
                for line_num, line in enumerate(f, 1):
                    line = line.strip()
                    if not line or line.startswith('#'):
                        continue
                    
                    # 提取----之前的部分
                    if '----' in line:
                        token = line.split('----')[0].strip()
                    else:
                        token = line.strip()
                    
                    if token:
                        tokens.append(token)
                        print(f"加载Token {len(tokens)}: {token[:20]}...")
        
        except Exception as e:
            print(f"读取Token文件失败: {e}")
        
        if not tokens:
            print("警告: 未找到有效的GitHub Token")
        else:
            print(f"成功加载 {len(tokens)} 个GitHub Token")
        
        return tokens
    
    def _get_current_token(self) -> Optional[str]:
        """获取当前使用的token"""
        if not self.tokens:
            return None
        return self.tokens[self.current_token_index]
    
    def _switch_to_next_token(self) -> bool:
        """切换到下一个token"""
        if len(self.tokens) <= 1:
            print("没有更多Token可切换")
            return False
        
        old_index = self.current_token_index
        self.current_token_index = (self.current_token_index + 1) % len(self.tokens)
        new_token = self.tokens[self.current_token_index]
        
        print(f"切换Token: {old_index + 1} -> {self.current_token_index + 1} ({new_token[:20]}...)")
        
        # 更新session的Authorization头
        if new_token:
            self.session.headers['Authorization'] = f'token {new_token}'
        else:
            self.session.headers.pop('Authorization', None)
        
        return True
    
    def _create_session(self) -> requests.Session:
        """创建带重试机制的HTTP会话"""
        session = requests.Session()
        
        # 设置请求头
        headers = {
            'User-Agent': 'GitHub-Repo-Collector/2.0',
            'Accept': 'application/vnd.github.v3+json'
        }
        
        current_token = self._get_current_token()
        if current_token:
            headers['Authorization'] = f'token {current_token}'
        
        session.headers.update(headers)
        
        # 配置重试策略
        retry_strategy = Retry(
            total=3,
            backoff_factor=1,
            status_forcelist=[500, 502, 503, 504]
        )
        
        adapter = HTTPAdapter(max_retries=retry_strategy)
        session.mount("http://", adapter)
        session.mount("https://", adapter)
        
        return session
    
    def _handle_rate_limit(self, response: requests.Response) -> bool:
        """处理速率限制，返回是否应该重试"""
        if response.status_code == 403:
            # 检查是否是速率限制
            rate_limit_remaining = response.headers.get('X-RateLimit-Remaining', '0')
            if rate_limit_remaining == '0':
                print(f"当前Token达到速率限制")
                
                # 尝试切换到下一个token
                if self._switch_to_next_token():
                    print("已切换到新Token，将重试请求")
                    return True
                else:
                    # 没有更多token，等待重置
                    reset_time = response.headers.get('X-RateLimit-Reset')
                    if reset_time:
                        reset_timestamp = int(reset_time)
                        wait_time = max(0, reset_timestamp - int(time.time()) + 60)
                        print(f"所有Token都达到限制，等待 {wait_time} 秒后重试")
                        time.sleep(wait_time)
                        return True
        
        return False
    
    def _load_progress(self) -> Dict:
        """加载进度文件"""
        if self.progress_file.exists():
            try:
                with open(self.progress_file, 'r', encoding='utf-8') as f:
                    progress = json.load(f)
                    
                    # 重置进度 - 修复星数范围索引问题
                    print("检测到进度异常，重置进度...")
                    for language in self.language_configs.keys():
                        if language not in progress:
                            progress[language] = {
                                "collected": 0,
                                "repos": [],
                                "current_range_index": 0,  # 从0开始（50-200星数范围）
                                "current_page": 1
                            }
                        else:
                            # 重置到正确的起始位置
                            progress[language]["current_range_index"] = 0
                            progress[language]["current_page"] = 1
                    
                    return progress
            except Exception as e:
                print(f"加载进度文件失败: {e}")
        
        return {lang: {
            "collected": 0,
            "repos": [],
            "current_range_index": 0,  # 从0开始
            "current_page": 1
        } for lang in self.language_configs.keys()}
    
    def _save_progress(self):
        """保存进度文件"""
        try:
            with open(self.progress_file, 'w', encoding='utf-8') as f:
                json.dump(self.progress, f, ensure_ascii=False, indent=2)
        except Exception as e:
            print(f"保存进度文件失败: {e}")
    
    def _build_search_query(self, language: str, star_range: Tuple[int, Optional[int]]) -> str:
        """构建搜索查询字符串"""
        config = self.language_configs[language]
        base_query = config["query_template"]
        
        min_stars, max_stars = star_range
        if max_stars is None:
            star_query = f"stars:>={min_stars}"
        else:
            star_query = f"stars:{min_stars}..{max_stars}"
        
        size_query = f"size:<={self.max_size_kb}"
        
        return f"{base_query} {star_query} {size_query}"
    
    def search_repositories(self, language: str, star_range: Tuple[int, Optional[int]], page: int = 1, max_retries: int = 3) -> List[Dict]:
        """搜索指定语言和星数范围的仓库"""
        url = "https://api.github.com/search/repositories"
        
        query = self._build_search_query(language, star_range)
        print(f"搜索查询: {query}")  # 添加调试信息
        
        params = {
            'q': query,
            'sort': 'stars',
            'order': 'desc',
            'per_page': 100,
            'page': page
        }
        
        for attempt in range(max_retries):
            try:
                response = self.session.get(url, params=params, timeout=30)
                
                # 处理速率限制
                if response.status_code == 403 and self._handle_rate_limit(response):
                    print(f"重试搜索请求 (尝试 {attempt + 1}/{max_retries})")
                    continue
                
                if response.status_code == 422:
                    print(f"搜索查询无效: {query}")
                    print(f"响应内容: {response.text}")  # 添加调试信息
                    return []
                
                response.raise_for_status()
                data = response.json()
                
                # 添加调试信息
                total_count = data.get('total_count', 0)
                items_count = len(data.get('items', []))
                print(f"找到 {total_count} 个仓库，本页 {items_count} 个")
                
                # 检查是否超过了GitHub的1000条限制
                if total_count > 1000 and page == 1:
                    print(f"查询结果超过1000条 ({total_count})，将使用分段搜索")
                
                return data.get('items', [])
                
            except requests.exceptions.RequestException as e:
                print(f"搜索仓库失败 (尝试 {attempt + 1}/{max_retries}): {e}")
                if attempt < max_retries - 1:
                    time.sleep(2 ** attempt)
                    continue
            except Exception as e:
                print(f"解析响应失败: {e}")
                break
        
        return []
    
    def _is_empty_repository(self, repo_dir: Path) -> bool:
        """检查仓库是否为空"""
        if not repo_dir.exists():
            return True
        
        # 检查是否只包含.git目录
        contents = list(repo_dir.iterdir())
        if not contents:
            return True
        
        # 过滤掉隐藏文件和.git目录
        meaningful_files = []
        for item in contents:
            if item.name.startswith('.'):
                continue
            meaningful_files.append(item)
        
        # 如果没有有意义的文件，认为是空仓库
        if not meaningful_files:
            return True
        
        # 检查文件总大小
        total_size = 0
        for item in repo_dir.rglob('*'):
            if item.is_file() and not item.name.startswith('.'):
                try:
                    total_size += item.stat().st_size
                except:
                    pass
        
        # 如果总大小小于1KB，认为是空仓库
        return total_size < 1024
    
    def check_api_rate_limit(self):
        """检查当前API速率限制状态"""
        url = "https://api.github.com/rate_limit"
        try:
            response = self.session.get(url, timeout=10)
            if response.status_code == 200:
                data = response.json()
                core = data.get('resources', {}).get('core', {})
                remaining = core.get('remaining', 0)
                limit = core.get('limit', 0)
                reset_time = core.get('reset', 0)
                
                current_token = self._get_current_token()
                token_display = f"{current_token[:20]}..." if current_token else "无Token"
                
                print(f"当前Token ({token_display}) 剩余请求: {remaining}/{limit}")
                
                if remaining < 10:
                    reset_datetime = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(reset_time))
                    print(f"警告: 剩余请求数较少，重置时间: {reset_datetime}")
                
                return remaining > 0
            else:
                print(f"检查速率限制失败: {response.status_code}")
        except Exception as e:
            print(f"检查速率限制异常: {e}")
        
        return True
    
    def clone_repository(self, repo_info: Dict, language: str) -> bool:
        """克隆仓库"""
        repo_name = repo_info['full_name']
        clone_url = repo_info['clone_url']
        
        # 创建语言目录
        lang_dir = self.base_dir / language
        lang_dir.mkdir(exist_ok=True)
        
        # 目标目录
        repo_dir = lang_dir / repo_name.replace('/', '_')
        
        # 如果目录已存在，检查是否为空
        if repo_dir.exists():
            if self._is_empty_repository(repo_dir):
                print(f"删除空仓库: {repo_name}")
                shutil.rmtree(repo_dir, ignore_errors=True)
            else:
                print(f"仓库已存在，跳过: {repo_name}")
                return True
        
        print(f"克隆仓库: {repo_name}")
        
        try:
            # 使用git clone --depth=1进行浅克隆
            cmd = [
                'git', 'clone', 
                '--depth=1',
                '--single-branch',
                clone_url, 
                str(repo_dir)
            ]
            
            result = subprocess.run(
                cmd, 
                capture_output=True, 
                text=True, 
                timeout=300  # 5分钟超时
            )
            
            if result.returncode == 0:
                # 检查克隆的仓库是否为空
                if self._is_empty_repository(repo_dir):
                    print(f"克隆的仓库为空，删除: {repo_name}")
                    shutil.rmtree(repo_dir, ignore_errors=True)
                    return False
                
                print(f"克隆成功: {repo_name}")
                
                # 删除.git目录以节省空间
                git_dir = repo_dir / '.git'
                if git_dir.exists():
                    shutil.rmtree(git_dir, ignore_errors=True)
                
                return True
            else:
                print(f"克隆失败: {repo_name}, 错误: {result.stderr}")
                # 删除可能创建的不完整目录
                if repo_dir.exists():
                    shutil.rmtree(repo_dir, ignore_errors=True)
                return False
                
        except subprocess.TimeoutExpired:
            print(f"克隆超时: {repo_name}")
            if repo_dir.exists():
                shutil.rmtree(repo_dir, ignore_errors=True)
            return False
        except Exception as e:
            print(f"克隆异常: {repo_name}, 错误: {e}")
            if repo_dir.exists():
                shutil.rmtree(repo_dir, ignore_errors=True)
            return False
    
    def collect_language_repos(self, language: str):
        """收集指定语言的仓库"""
        print(f"\n开始收集 {language} 仓库...")
        
        if not self.check_api_rate_limit():
            print(f"API速率限制耗尽，跳过 {language}")
            return
        
        config = self.language_configs[language]
        star_ranges = config["star_ranges"]
        
        current_count = self.progress[language]["collected"]
        target_count = self.repos_per_language
        
        if current_count >= target_count:
            print(f"{language} 已完成收集 ({current_count}/{target_count})")
            return
        
        # 从上次的进度继续
        range_index = self.progress[language]["current_range_index"]
        current_page = self.progress[language]["current_page"]
        
        collected = current_count
        skipped_existing = 0
        
        while collected < target_count and range_index < len(star_ranges):
            star_range = star_ranges[range_index]
            min_stars, max_stars = star_range
            
            print(f"搜索 {language} 星数范围: {min_stars}-{max_stars or '∞'}, 第 {current_page} 页")
            
            repos = self.search_repositories(language, star_range, current_page)
            
            if not repos:
                print(f"当前星数范围搜索完毕，切换到下一个范围")
                range_index += 1
                current_page = 1
                self.progress[language]["current_range_index"] = range_index
                self.progress[language]["current_page"] = current_page
                self._save_progress()
                continue
            
            page_success_count = 0
            
            for repo in repos:
                if collected >= target_count:
                    break
                
                repo_name = repo['full_name']
                
                # 检查是否已在统计文件中存在
                if repo_name in self.existing_repos:
                    skipped_existing += 1
                    continue
                
                # 检查是否已在进度文件中处理过
                existing_repo_names = [r['full_name'] for r in self.progress[language]["repos"]]
                if repo_name in existing_repo_names:
                    continue
                
                # 检查仓库大小
                size_kb = repo.get('size', 0)
                if size_kb > self.max_size_kb:
                    print(f"仓库过大，跳过: {repo_name} ({size_kb}KB)")
                    continue
                
                # 克隆仓库
                success = self.clone_repository(repo, language)
                
                # 记录进度
                repo_info = {
                    'full_name': repo_name,
                    'stars': repo['stargazers_count'],
                    'size_kb': size_kb,
                    'cloned': success,
                    'url': repo['html_url'],
                    'star_range': f"{min_stars}-{max_stars or '∞'}"
                }
                
                self.progress[language]["repos"].append(repo_info)
                
                if success:
                    collected += 1
                    page_success_count += 1
                    self.progress[language]["collected"] = collected
                    print(f"{language}: {collected}/{target_count} (跳过已统计: {skipped_existing})")
                
                # 保存进度
                self._save_progress()
                
                # 避免API限制
                time.sleep(0.5)
            
            # 如果这一页没有成功克隆任何仓库，跳到下一个范围
            if page_success_count == 0 and len(repos) < 100:
                print(f"当前页面无有效仓库，切换到下一个星数范围")
                range_index += 1
                current_page = 1
            else:
                current_page += 1
            
            # 更新进度
            self.progress[language]["current_range_index"] = range_index
            self.progress[language]["current_page"] = current_page
            self._save_progress()
            
            # 避免过度请求
            time.sleep(1)
        
        print(f"{language} 收集完成: {collected}/{target_count} (跳过已统计: {skipped_existing})")
    
    def run(self):
        """运行收集程序"""
        print("GitHub 仓库收集器启动 - 修复版")
        print(f"目标: 每种语言 {self.repos_per_language} 个仓库")
        print(f"条件: 星数 >= {self.min_stars}, 大小 <= {self.max_size_kb}KB")
        print(f"存储目录: {self.base_dir}")
        print(f"Token文件: {self.token_file}")
        print(f"统计文件: {self.statistics_file}")
        print(f"已统计仓库数: {len(self.existing_repos)}")
        
        if not self.tokens:
            print("\n警告: 未找到有效的GitHub Token，API限制为60次/小时")
        else:
            print(f"\n已加载 {len(self.tokens)} 个GitHub Token")
            self.check_api_rate_limit()
        
        print(f"\n支持的语言及包管理工具:")
        for lang, config in self.language_configs.items():
            print(f"  {lang:12}: {config['query_template']}")
        
        # 检查git是否可用
        try:
            subprocess.run(['git', '--version'], capture_output=True, check=True)
        except (subprocess.CalledProcessError, FileNotFoundError):
            print("错误: 未找到git命令，请安装git")
            return
        
        # 收集各语言仓库
        for language in self.language_configs.keys():
            try:
                self.collect_language_repos(language)
            except KeyboardInterrupt:
                print(f"\n用户中断，保存进度...")
                self._save_progress()
                break
            except Exception as e:
                print(f"收集 {language} 时出错: {e}")
                continue
        
        # 打印统计信息
        self.print_statistics()
    
    def print_statistics(self):
        """打印统计信息"""
        print("\n" + "="*60)
        print("收集统计:")
        print("="*60)
        
        total_collected = 0
        for language in self.language_configs.keys():
            collected = self.progress[language]["collected"]
            target = self.repos_per_language
            range_index = self.progress[language]["current_range_index"]
            page = self.progress[language]["current_page"]
            total_collected += collected
            
            config = self.language_configs[language]
            total_ranges = len(config["star_ranges"])
            
            print(f"{language:12}: {collected:4d}/{target} ({collected/target*100:.1f}%) "
                  f"[范围 {range_index+1}/{total_ranges}, 页 {page}]")
        
        print("-"*60)
        total_target = len(self.language_configs) * self.repos_per_language
        print(f"总计: {total_collected}/{total_target} ({total_collected/total_target*100:.1f}%)")
        print(f"已统计仓库: {len(self.existing_repos)}")
        print("="*60)


def main():
    """主函数"""
    # 创建收集器实例
    collector = GitHubRepoCollector(
        token_file="GITHUB_TOKEN.txt",     # Token文件路径
        base_dir="./github_repos_v2",         # 仓库存储目录
        min_stars=50,                      # 最小星数
        max_size_mb=1,                     # 最大仓库大小(MB)
        repos_per_language=5000,           # 每种语言目标仓库数
        statistics_file="repo_statistics_v2.jsonl"  # 统计文件路径
    )
    
    # 运行收集
    collector.run()


if __name__ == "__main__":
    main()