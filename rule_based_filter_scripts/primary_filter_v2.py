#!/usr/bin/env python3
"""
优化版代码项目过滤脚本 - 多进程并行处理
用于过滤出适合跨语言翻译的项目，排除那些依赖特定语言生态系统的项目
"""

import os
import shutil
import re
from pathlib import Path
from typing import Dict, List, Set, Tuple
import json
import multiprocessing as mp
from concurrent.futures import ProcessPoolExecutor, as_completed
import threading
from collections import defaultdict
import time
import functools

class OptimizedProjectFilter:
    def __init__(self, source_dir: str, target_dir: str, max_workers: int = None):
        self.source_dir = Path(source_dir)
        self.target_dir = Path(target_dir)
        self.max_workers = max_workers or min(mp.cpu_count(), 8)  # 限制最大进程数
        
        # 创建目标目录
        self.target_dir.mkdir(exist_ok=True)
        
        # 定义各语言的过滤规则（同原代码）
        self.filter_rules = {
            'Python': {
                'file_extensions': ['.py', '.pyx', '.pyi'],
                'exclude_imports': {
                    'torch', 'tensorflow', 'keras', 'sklearn', 'numpy', 'pandas',
                    'scipy', 'matplotlib', 'seaborn', 'plotly', 'cv2', 'PIL', 'openai',
                    'django', 'flask', 'fastapi', 'tornado', 'pyramid',
                    'tkinter', 'PyQt5', 'PyQt6', 'PySide2', 'PySide6', 'kivy',
                    'psutil', 'win32api', 'win32gui', 'ctypes',
                    'sqlalchemy', 'django.db', 'peewee',
                    'asyncio', 'aiohttp', 'celery',
                    'sympy', 'statsmodels', 'networkx',
                },
                'exclude_keywords': ['__pycache__', '.pyc', 'setup.py', 'requirements.txt']
            },
            'JavaScript': {
                'file_extensions': ['.js', '.jsx', '.ts', '.tsx'],
                'exclude_imports': {
                    'react', 'vue', 'angular', '@angular', 'svelte',
                    'express', 'koa', 'fastify', 'nest',
                    'webpack', 'vite', 'rollup', 'parcel',
                    'antd', 'material-ui', '@mui', 'bootstrap',
                    'redux', 'mobx', 'vuex', 'pinia',
                    'jquery', 'lodash', 'underscore',
                    'jest', 'mocha', 'cypress', 'playwright'
                },
                'exclude_keywords': ['package.json', 'node_modules', '.npm', 'yarn.lock']
            },
            'Java': {
                'file_extensions': ['.java'],
                'exclude_imports': {
                    'org.springframework', 'springframework',
                    'android', 'androidx',
                    'javax.servlet', 'jakarta.servlet',
                    'javax.swing', 'java.awt', 'javafx',
                    'hibernate', 'mybatis', 'struts',
                    'org.apache.spark', 'org.apache.hadoop',
                    'org.apache.kafka', 'rabbitmq'
                },
                'exclude_keywords': ['pom.xml', 'build.gradle', '.class', 'META-INF']
            },
            'C++': {
                'file_extensions': ['.cpp', '.cc', '.cxx', '.h', '.hpp'],
                'exclude_imports': {
                    'Qt', 'gtk', 'wxWidgets', 'FLTK',
                    'unreal', 'unity', 'ogre',
                    'OpenGL', 'DirectX', 'Vulkan',
                    'windows.h', 'win32', 'linux',
                    'boost/asio', 'poco',
                    'opencv', 'eigen', 'armadillo'
                },
                'exclude_keywords': ['CMakeLists.txt', 'Makefile', '.so', '.dll']
            },
            'C': {
                'file_extensions': ['.c', '.h'],
                'exclude_imports': {
                    'windows.h', 'unistd.h', 'sys/',
                    'gtk', 'qt',
                    'socket.h', 'netinet',
                    'pthread.h',
                    'sqlite3.h', 'mysql.h'
                },
                'exclude_keywords': ['Makefile', '.so', '.dll', '.a']
            },
            'C#': {
                'file_extensions': ['.cs'],
                'exclude_imports': {
                    'System.Windows', 'Microsoft',
                    'ASP.NET', 'Blazor',
                    'WPF', 'WinForms', 'MAUI',
                    'Unity', 'MonoGame',
                    'Entity Framework', 'Dapper'
                },
                'exclude_keywords': ['.csproj', '.sln', 'packages.config', 'bin/', 'obj/']
            },
            'Go': {
                'file_extensions': ['.go'],
                'exclude_imports': {
                    'gin', 'echo', 'fiber', 'beego',
                    'grpc', 'protobuf',
                    'gorm', 'sqlx',
                    'syscall', 'os/exec',
                    'net/http', 'gorilla'
                },
                'exclude_keywords': ['go.mod', 'go.sum', 'vendor/']
            },
            'Rust': {
                'file_extensions': ['.rs'],
                'exclude_imports': {
                    'actix', 'rocket', 'warp', 'axum',
                    'egui', 'tauri', 'gtk',
                    'tokio', 'async-std',
                    'winapi', 'libc',
                    'serde', 'bincode'
                },
                'exclude_keywords': ['Cargo.toml', 'Cargo.lock', 'target/']
            }
        }
        
        # 编译正则表达式模式以提高性能
        self._compile_patterns()
    
    def _compile_patterns(self):
        """预编译正则表达式模式以提高性能"""
        self.compiled_patterns = {}
        
        for language, rules in self.filter_rules.items():
            patterns = {}
            for import_name in rules['exclude_imports']:
                import_lower = import_name.lower()
                
                if language == 'Python':
                    pattern_list = [
                        rf'\bimport\s+{re.escape(import_lower)}\b',
                        rf'\bfrom\s+{re.escape(import_lower)}\b',
                        rf'\bimport\s+\w*{re.escape(import_lower)}\w*\b'
                    ]
                elif language == 'JavaScript':
                    pattern_list = [
                        rf'\bimport\s+.*{re.escape(import_lower)}',
                        rf'\brequire\s*\(\s*["\'].*{re.escape(import_lower)}',
                        rf'\bfrom\s+["\'].*{re.escape(import_lower)}'
                    ]
                elif language == 'Java':
                    pattern_list = [
                        rf'\bimport\s+{re.escape(import_lower)}',
                        rf'\bimport\s+static\s+{re.escape(import_lower)}'
                    ]
                elif language in ['C++', 'C']:
                    pattern_list = [
                        rf'#include\s*[<"]{re.escape(import_lower)}',
                        rf'#include\s*[<"].*{re.escape(import_lower)}'
                    ]
                elif language == 'C#':
                    pattern_list = [
                        rf'\busing\s+{re.escape(import_lower)}',
                        rf'\busing\s+.*{re.escape(import_lower)}'
                    ]
                elif language == 'Go':
                    pattern_list = [
                        rf'\bimport\s+["\'].*{re.escape(import_lower)}',
                        rf'^\s*["\'].*{re.escape(import_lower)}'
                    ]
                elif language == 'Rust':
                    pattern_list = [
                        rf'\buse\s+{re.escape(import_lower)}',
                        rf'\bextern\s+crate\s+{re.escape(import_lower)}'
                    ]
                else:
                    continue
                    
                # 编译所有模式
                compiled_patterns = []
                for pattern in pattern_list:
                    try:
                        compiled_patterns.append(re.compile(pattern, re.MULTILINE | re.IGNORECASE))
                    except re.error:
                        continue
                        
                patterns[import_name] = compiled_patterns
                
            self.compiled_patterns[language] = patterns
    
    @staticmethod
    def should_exclude_file_static(file_path_str: str, language: str, compiled_patterns: dict) -> bool:
        """静态方法版本的文件检查，用于多进程"""
        file_path = Path(file_path_str)
        
        try:
            # 限制文件大小，避免读取过大的文件
            if file_path.stat().st_size > 1024 * 1024:  # 1MB
                return False
                
            content = file_path.read_text(encoding='utf-8', errors='ignore')
            content_lower = content.lower()
            
            # 使用预编译的模式进行匹配
            if language in compiled_patterns:
                for import_name, patterns in compiled_patterns[language].items():
                    for pattern in patterns:
                        if pattern.search(content_lower):
                            return True
                            
            return False
        except Exception:
            return False
    
    @staticmethod
    def analyze_project_static(args: tuple) -> tuple:
        """静态方法版本的项目分析，用于多进程"""
        project_path_str, language, filter_rules, compiled_patterns = args
        project_path = Path(project_path_str)
        
        if language not in filter_rules:
            return project_path_str, False, [], 0, 0
            
        rules = filter_rules[language]
        exclusion_reasons = []
        
        # 快速检查：检查是否包含排除的关键词文件
        for exclude_keyword in rules['exclude_keywords']:
            # 使用更高效的方式检查文件存在性
            if exclude_keyword.endswith('/'):
                # 目录检查
                if any(project_path.rglob(exclude_keyword.rstrip('/'))):
                    exclusion_reasons.append(f"Contains directory {exclude_keyword}")
                    break
            else:
                # 文件检查
                if any(project_path.rglob(exclude_keyword)):
                    exclusion_reasons.append(f"Contains {exclude_keyword}")
                    break
        
        # 如果已经有排除原因，直接返回
        if exclusion_reasons:
            return project_path_str, True, exclusion_reasons, 0, 0
        
        # 检查代码文件（限制数量以提高性能）
        excluded_files = []
        total_files = 0
        max_files_to_check = 50  # 限制检查的文件数量
        
        for ext in rules['file_extensions']:
            for file_path in project_path.rglob(f"*{ext}"):
                if total_files >= max_files_to_check:
                    break
                    
                total_files += 1
                if OptimizedProjectFilter.should_exclude_file_static(
                    str(file_path), language, compiled_patterns
                ):
                    excluded_files.append(file_path.name)
            
            if total_files >= max_files_to_check:
                break
        
        # 如果超过30%的文件被排除，则排除整个项目
        should_exclude = False
        if total_files > 0 and len(excluded_files) / total_files > 0.3:
            exclusion_reasons.append(f"Too many excluded files: {len(excluded_files)}/{total_files}")
            should_exclude = True
            
        return project_path_str, should_exclude, exclusion_reasons, total_files, len(excluded_files)
    
    def get_all_projects(self) -> List[Tuple[str, str]]:
        """获取所有项目路径和对应的语言"""
        projects = []
        for language_dir in self.source_dir.iterdir():
            if not language_dir.is_dir():
                continue
                
            language = language_dir.name
            for project_dir in language_dir.iterdir():
                if project_dir.is_dir():
                    projects.append((str(project_dir), language))
        
        return projects
    
    def copy_project_safe(self, source_path: str, target_path: str) -> bool:
        """安全地复制项目"""
        try:
            source = Path(source_path)
            target = Path(target_path)
            target.parent.mkdir(parents=True, exist_ok=True)
            
            shutil.copytree(source, target, dirs_exist_ok=True)
            return True
        except Exception as e:
            print(f"复制项目失败 {source_path}: {e}")
            return False
    
    def filter_projects(self):
        """执行项目过滤（多进程版本）"""
        print(f"使用 {self.max_workers} 个进程进行并行处理...")
        
        # 获取所有项目
        all_projects = self.get_all_projects()
        print(f"总共找到 {len(all_projects)} 个项目")
        
        # 准备多进程参数
        process_args = [
            (project_path, language, self.filter_rules, self.compiled_patterns)
            for project_path, language in all_projects
        ]
        
        stats = defaultdict(lambda: {'total': 0, 'filtered': 0, 'excluded': 0})
        
        # 使用进度指示器
        start_time = time.time()
        processed = 0
        
        # 分批处理以控制内存使用
        batch_size = 100
        
        with ProcessPoolExecutor(max_workers=self.max_workers) as executor:
            for i in range(0, len(process_args), batch_size):
                batch = process_args[i:i + batch_size]
                
                # 提交批次任务
                futures = [executor.submit(self.analyze_project_static, args) for args in batch]
                
                # 处理结果
                for future in as_completed(futures):
                    try:
                        project_path, should_exclude, reasons, total_files, excluded_files = future.result()
                        
                        # 从路径中提取语言
                        language = Path(project_path).parent.name
                        project_name = Path(project_path).name
                        
                        stats[language]['total'] += 1
                        processed += 1
                        
                        if should_exclude:
                            stats[language]['excluded'] += 1
                            print(f"[{processed}/{len(all_projects)}] 排除项目: {language}/{project_name}")
                            for reason in reasons:
                                print(f"    原因: {reason}")
                        else:
                            # 复制项目到过滤后的目录
                            target_lang_dir = self.target_dir / language
                            target_lang_dir.mkdir(exist_ok=True)
                            target_project_path = str(target_lang_dir / project_name)
                            
                            if self.copy_project_safe(project_path, target_project_path):
                                stats[language]['filtered'] += 1
                                print(f"[{processed}/{len(all_projects)}] 保留项目: {language}/{project_name}")
                        
                        # 显示进度
                        if processed % 10 == 0:
                            elapsed = time.time() - start_time
                            rate = processed / elapsed
                            eta = (len(all_projects) - processed) / rate if rate > 0 else 0
                            print(f"进度: {processed}/{len(all_projects)} ({processed/len(all_projects)*100:.1f}%) "
                                  f"速度: {rate:.1f} 项目/秒 预计剩余: {eta:.0f}秒")
                    
                    except Exception as e:
                        print(f"处理项目时出错: {e}")
                        processed += 1
        
        # 转换为普通字典
        stats_dict = dict(stats)
        
        # 保存统计信息
        stats_file = self.target_dir / 'filter_stats.json'
        with open(stats_file, 'w', encoding='utf-8') as f:
            json.dump(stats_dict, f, indent=2, ensure_ascii=False)
            
        total_time = time.time() - start_time
        print(f"\n过滤完成！耗时: {total_time:.1f}秒")
        print(f"统计信息已保存到: {stats_file}")
        
        return stats_dict

def main():
    # 配置路径
    source_dir = "github_repos"
    target_dir = "primary_filter"
    
    # 检查源目录是否存在
    if not os.path.exists(source_dir):
        print(f"错误: 源目录 {source_dir} 不存在")
        return
    
    # 获取最优的进程数
    max_workers = min(mp.cpu_count(), 8)  # 限制最大进程数，避免系统过载
    print(f"检测到 {mp.cpu_count()} 个CPU核心，将使用 {max_workers} 个进程")
    
    # 创建过滤器并执行过滤
    filter_instance = OptimizedProjectFilter(source_dir, target_dir, max_workers)
    stats = filter_instance.filter_projects()
    
    # 打印总体统计
    print("\n=== 总体统计 ===")
    if stats:
        total_all = sum(s['total'] for s in stats.values())
        filtered_all = sum(s['filtered'] for s in stats.values())
        excluded_all = sum(s['excluded'] for s in stats.values())
        
        print(f"总项目数: {total_all}")
        print(f"保留项目数: {filtered_all}")
        print(f"排除项目数: {excluded_all}")
        if total_all > 0:
            print(f"保留比例: {filtered_all/total_all*100:.1f}%")
    else:
        print("没有找到任何项目")

if __name__ == "__main__":
    main()
    