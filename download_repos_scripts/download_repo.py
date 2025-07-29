#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
GitHub仓库收集脚本
支持多语言、星数筛选、大小限制、断点续传、多Token自动切换等功能
改进版：会先检查repo_statistics.jsonl文件，跳过已统计的仓库
"""

import os
import json
import time
import shutil
import subprocess
from pathlib import Path
from typing import Dict, List, Optional, Set
import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

class GitHubRepoCollector:
    def __init__(self, 
                 token_file: str = "GITHUB_TOKEN.txt",
                 base_dir: str = "./github_repos",
                 min_stars: int = 50,
                 max_stars: int = None,
                 max_size_mb: int = 5,
                 repos_per_language: int = 500,
                 statistics_file: str = "repo_statistics.jsonl"):
        
        self.token_file = token_file
        self.base_dir = Path(base_dir)
        self.min_stars = min_stars
        self.max_stars = max_stars
        self.max_size_kb = max_size_mb * 1024  # 转换为KB
        self.repos_per_language = repos_per_language
        self.statistics_file = statistics_file
        
        # 支持的编程语言
        self.languages = [
            # "Python", "C++", "C", "Java", "C#", "JavaScript", "Go", "Rust"
            "Matlab",
        ]
        
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
        """从repo_statistics.jsonl文件加载已统计的仓库名称"""
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
            'User-Agent': 'GitHub-Repo-Collector/1.0',
            'Accept': 'application/vnd.github.v3+json'
        }
        
        current_token = self._get_current_token()
        if current_token:
            headers['Authorization'] = f'token {current_token}'
        
        session.headers.update(headers)
        
        # 配置重试策略（不包含429，因为我们要手动处理速率限制）
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
                        wait_time = max(0, reset_timestamp - int(time.time()) + 60)  # 多等待1分钟
                        print(f"所有Token都达到限制，等待 {wait_time} 秒后重试")
                        time.sleep(wait_time)
                        return True
        
        return False
    
    def _load_progress(self) -> Dict:
        """加载进度文件"""
        # 首先初始化所有当前语言的默认进度
        default_progress = {lang: {"collected": 0, "repos": []} for lang in self.languages}
        
        if self.progress_file.exists():
            try:
                with open(self.progress_file, 'r', encoding='utf-8') as f:
                    loaded_progress = json.load(f)
                
                # 合并加载的进度和默认进度
                for lang in self.languages:
                    if lang in loaded_progress:
                        default_progress[lang] = loaded_progress[lang]
                    else:
                        print(f"警告: 进度文件中未找到语言 '{lang}' 的记录，使用默认值")
                
                return default_progress
                
            except Exception as e:
                print(f"加载进度文件失败: {e}")
        
        return default_progress
    
    def _save_progress(self):
        """保存进度文件"""
        try:
            with open(self.progress_file, 'w', encoding='utf-8') as f:
                json.dump(self.progress, f, ensure_ascii=False, indent=2)
        except Exception as e:
            print(f"保存进度文件失败: {e}")
    
    def search_repositories(self, language: str, page: int = 1, max_retries: int = 3) -> List[Dict]:
        """搜索指定语言的仓库"""
        url = "https://api.github.com/search/repositories"
        
        # 构建搜索查询
        if self.max_stars is None:
            query = f"language:{language} stars:>={self.min_stars} size:<={self.max_size_kb}"
        else:
            query = f"language:{language} stars:{self.min_stars}..{self.max_stars} size:<={self.max_size_kb}"
            
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
                
                response.raise_for_status()
                data = response.json()
                return data.get('items', [])
                
            except requests.exceptions.RequestException as e:
                print(f"搜索仓库失败 (尝试 {attempt + 1}/{max_retries}): {e}")
                if attempt < max_retries - 1:
                    time.sleep(2 ** attempt)  # 指数退避
                    continue
            except Exception as e:
                print(f"解析响应失败: {e}")
                break
        
        return []
    
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
                
                if remaining < 10:  # 剩余请求数很少时提醒
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
        
        # 如果目录已存在，跳过
        if repo_dir.exists():
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
        
        # 检查API速率限制
        if not self.check_api_rate_limit():
            print(f"API速率限制耗尽，跳过 {language}")
            return
        
        current_count = self.progress[language]["collected"]
        target_count = self.repos_per_language
        
        if current_count >= target_count:
            print(f"{language} 已完成收集 ({current_count}/{target_count})")
            return
        
        page = 1
        collected = current_count
        consecutive_failures = 0  # 连续失败计数
        skipped_existing = 0  # 跳过的已存在仓库数
        
        while collected < target_count:
            print(f"搜索 {language} 第 {page} 页...")
            
            repos = self.search_repositories(language, page)
            if not repos:
                consecutive_failures += 1
                if consecutive_failures >= 3:
                    print(f"连续 {consecutive_failures} 次搜索失败，停止收集 {language}")
                    break
                print(f"搜索失败，等待后重试...")
                time.sleep(5)
                continue
            else:
                consecutive_failures = 0  # 重置失败计数
            
            for repo in repos:
                if collected >= target_count:
                    break
                
                repo_name = repo['full_name']
                
                # 检查是否已在统计文件中存在
                if repo_name in self.existing_repos:
                    print(f"仓库已在统计文件中，跳过: {repo_name}")
                    skipped_existing += 1
                    continue
                
                # 检查是否已在进度文件中处理过
                if repo_name in [r['full_name'] for r in self.progress[language]["repos"]]:
                    print(f"仓库已在进度中，跳过: {repo_name}")
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
                    'url': repo['html_url']
                }
                
                self.progress[language]["repos"].append(repo_info)
                
                if success:
                    collected += 1
                    self.progress[language]["collected"] = collected
                    print(f"{language}: {collected}/{target_count} (跳过已统计: {skipped_existing})")
                
                # 保存进度
                self._save_progress()
                
                # 避免API限制
                time.sleep(0.5)
            
            page += 1
            
            # 避免过度请求
            time.sleep(1)
        
        print(f"{language} 收集完成: {collected}/{target_count} (跳过已统计: {skipped_existing})")
    
    def run(self):
        """运行收集程序"""
        print("GitHub 仓库收集器启动")
        print(f"目标: 每种语言 {self.repos_per_language} 个仓库")
        print(f"条件: {self.min_stars} <= 星数 <= {self.max_stars}, 大小 <= {self.max_size_kb}KB")
        print(f"存储目录: {self.base_dir}")
        print(f"Token文件: {self.token_file}")
        print(f"统计文件: {self.statistics_file}")
        print(f"已统计仓库数: {len(self.existing_repos)}")
        
        if not self.tokens:
            print("\n警告: 未找到有效的GitHub Token，API限制为60次/小时")
        else:
            print(f"\n已加载 {len(self.tokens)} 个GitHub Token")
            # 显示当前API状态
            self.check_api_rate_limit()
        
        print(f"\n支持的语言: {', '.join(self.languages)}")
        
        # 检查git是否可用
        try:
            subprocess.run(['git', '--version'], capture_output=True, check=True)
        except (subprocess.CalledProcessError, FileNotFoundError):
            print("错误: 未找到git命令，请安装git")
            return
        
        # 收集各语言仓库
        for language in self.languages:
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
        print("\n" + "="*50)
        print("收集统计:")
        print("="*50)
        
        total_collected = 0
        for language in self.languages:
            collected = self.progress[language]["collected"]
            target = self.repos_per_language
            total_collected += collected
            print(f"{language:12}: {collected:3d}/{target} ({collected/target*100:.1f}%)")
        
        print("-"*50)
        print(f"总计: {total_collected}/{len(self.languages) * self.repos_per_language}")
        print(f"已统计仓库: {len(self.existing_repos)}")
        print("="*50)


def main():
    """主函数"""
    for max_stars in [25]:
        # 创建收集器实例
        collector = GitHubRepoCollector(
            token_file="GITHUB_TOKEN.txt",  # Token文件路径
            base_dir="./github_repos",      # 仓库存储目录
            min_stars=20,                   # 最小星数
            max_stars=max_stars,            # 最大星数
            max_size_mb=1,                  # 最大仓库大小(MB)
            repos_per_language=4000,        # 每种语言目标仓库数
            statistics_file="repo_statistics.jsonl"  # 统计文件路径
        )
        
        # 运行收集
        collector.run()


if __name__ == "__main__":
    main()