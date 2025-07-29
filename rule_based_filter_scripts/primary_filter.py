#!/usr/bin/env python3
"""
代码项目过滤脚本
用于过滤出适合跨语言翻译的项目，排除那些依赖特定语言生态系统的项目
"""

import os
import shutil
import re
from pathlib import Path
from typing import Dict, List, Set
import json

class ProjectFilter:
    def __init__(self, source_dir: str, target_dir: str):
        self.source_dir = Path(source_dir)
        self.target_dir = Path(target_dir)
        
        # 创建目标目录
        self.target_dir.mkdir(exist_ok=True)
        
        # 定义各语言的过滤规则
        self.filter_rules = {
            'Python': {
                'file_extensions': ['.py', '.pyx', '.pyi'],
                'exclude_imports': {
                    # 深度学习/机器学习框架
                    'torch', 'tensorflow', 'keras', 'sklearn', 'numpy', 'pandas',
                    'scipy', 'matplotlib', 'seaborn', 'plotly', 'cv2', 'PIL', 'openai',
                    # Web框架
                    'django', 'flask', 'fastapi', 'tornado', 'pyramid',
                    # GUI框架
                    'tkinter', 'PyQt5', 'PyQt6', 'PySide2', 'PySide6', 'kivy',
                    # 系统级操作
                    'psutil', 'win32api', 'win32gui', 'ctypes',
                    # 数据库ORM
                    'sqlalchemy', 'django.db', 'peewee',
                    # 异步框架
                    'asyncio', 'aiohttp', 'celery',
                    # 科学计算
                    'sympy', 'statsmodels', 'networkx',
                },
                'exclude_keywords': ['__pycache__', '.pyc', 'setup.py', 'requirements.txt']
            },
            
            'JavaScript': {
                'file_extensions': ['.js', '.jsx', '.ts', '.tsx'],
                'exclude_imports': {
                    # 前端框架
                    'react', 'vue', 'angular', '@angular', 'svelte',
                    # Node.js特定
                    'express', 'koa', 'fastify', 'nest',
                    # 构建工具
                    'webpack', 'vite', 'rollup', 'parcel',
                    # UI库
                    'antd', 'material-ui', '@mui', 'bootstrap',
                    # 状态管理
                    'redux', 'mobx', 'vuex', 'pinia',
                    # DOM操作
                    'jquery', 'lodash', 'underscore',
                    # 测试框架
                    'jest', 'mocha', 'cypress', 'playwright'
                },
                'exclude_keywords': ['package.json', 'node_modules', '.npm', 'yarn.lock']
            },
            
            'Java': {
                'file_extensions': ['.java'],
                'exclude_imports': {
                    # Spring框架
                    'org.springframework', 'springframework',
                    # Android开发
                    'android', 'androidx',
                    # Web框架
                    'javax.servlet', 'jakarta.servlet',
                    # GUI框架
                    'javax.swing', 'java.awt', 'javafx',
                    # 企业级框架
                    'hibernate', 'mybatis', 'struts',
                    # 大数据框架
                    'org.apache.spark', 'org.apache.hadoop',
                    # 消息队列
                    'org.apache.kafka', 'rabbitmq'
                },
                'exclude_keywords': ['pom.xml', 'build.gradle', '.class', 'META-INF']
            },
            
            'C++': {
                'file_extensions': ['.cpp', '.cc', '.cxx', '.h', '.hpp'],
                'exclude_imports': {
                    # GUI框架
                    'Qt', 'gtk', 'wxWidgets', 'FLTK',
                    # 游戏引擎
                    'unreal', 'unity', 'ogre',
                    # 图形库
                    'OpenGL', 'DirectX', 'Vulkan',
                    # 系统API
                    'windows.h', 'win32', 'linux',
                    # 网络库
                    'boost/asio', 'poco',
                    # 机器学习
                    'opencv', 'eigen', 'armadillo'
                },
                'exclude_keywords': ['CMakeLists.txt', 'Makefile', '.so', '.dll']
            },
            
            'C': {
                'file_extensions': ['.c', '.h'],
                'exclude_imports': {
                    # 系统头文件
                    'windows.h', 'unistd.h', 'sys/',
                    # GUI库
                    'gtk', 'qt',
                    # 网络编程
                    'socket.h', 'netinet',
                    # 线程库
                    'pthread.h',
                    # 数据库
                    'sqlite3.h', 'mysql.h'
                },
                'exclude_keywords': ['Makefile', '.so', '.dll', '.a']
            },
            
            'C#': {
                'file_extensions': ['.cs'],
                'exclude_imports': {
                    # .NET框架特定
                    'System.Windows', 'Microsoft',
                    # Web框架
                    'ASP.NET', 'Blazor',
                    # GUI框架
                    'WPF', 'WinForms', 'MAUI',
                    # 游戏开发
                    'Unity', 'MonoGame',
                    # 数据库
                    'Entity Framework', 'Dapper'
                },
                'exclude_keywords': ['.csproj', '.sln', 'packages.config', 'bin/', 'obj/']
            },
            
            'Go': {
                'file_extensions': ['.go'],
                'exclude_imports': {
                    # Web框架
                    'gin', 'echo', 'fiber', 'beego',
                    # gRPC
                    'grpc', 'protobuf',
                    # 数据库驱动
                    'gorm', 'sqlx',
                    # 系统调用
                    'syscall', 'os/exec',
                    # 网络库
                    'net/http', 'gorilla'
                },
                'exclude_keywords': ['go.mod', 'go.sum', 'vendor/']
            },
            
            'Rust': {
                'file_extensions': ['.rs'],
                'exclude_imports': {
                    # Web框架
                    'actix', 'rocket', 'warp', 'axum',
                    # GUI框架
                    'egui', 'tauri', 'gtk',
                    # 异步运行时
                    'tokio', 'async-std',
                    # 系统编程
                    'winapi', 'libc',
                    # 序列化
                    'serde', 'bincode'
                },
                'exclude_keywords': ['Cargo.toml', 'Cargo.lock', 'target/']
            }
        }
    
    def should_exclude_file(self, file_path: Path, language: str) -> bool:
        """检查文件是否应该被排除"""
        if language not in self.filter_rules:
            return False
            
        rules = self.filter_rules[language]
        
        # 检查文件扩展名
        if not any(str(file_path).endswith(ext) for ext in rules['file_extensions']):
            return False
            
        try:
            content = file_path.read_text(encoding='utf-8', errors='ignore')
            
            # 检查导入语句
            for exclude_import in rules['exclude_imports']:
                if self._check_import_pattern(content, exclude_import, language):
                    return True
                    
            return False
        except Exception as e:
            print(f"Error reading file {file_path}: {e}")
            return False
    
    def _check_import_pattern(self, content: str, import_name: str, language: str) -> bool:
        """根据语言检查导入模式"""
        content_lower = content.lower()
        import_lower = import_name.lower()
        
        if language == 'Python':
            patterns = [
                rf'\bimport\s+{re.escape(import_lower)}\b',
                rf'\bfrom\s+{re.escape(import_lower)}\b',
                rf'\bimport\s+\w*{re.escape(import_lower)}\w*\b'
            ]
        elif language == 'JavaScript':
            patterns = [
                rf'\bimport\s+.*{re.escape(import_lower)}',
                rf'\brequire\s*\(\s*["\'].*{re.escape(import_lower)}',
                rf'\bfrom\s+["\'].*{re.escape(import_lower)}'
            ]
        elif language == 'Java':
            patterns = [
                rf'\bimport\s+{re.escape(import_lower)}',
                rf'\bimport\s+static\s+{re.escape(import_lower)}'
            ]
        elif language in ['C++', 'C']:
            patterns = [
                rf'#include\s*[<"]{re.escape(import_lower)}',
                rf'#include\s*[<"].*{re.escape(import_lower)}'
            ]
        elif language == 'C#':
            patterns = [
                rf'\busing\s+{re.escape(import_lower)}',
                rf'\busing\s+.*{re.escape(import_lower)}'
            ]
        elif language == 'Go':
            patterns = [
                rf'\bimport\s+["\'].*{re.escape(import_lower)}',
                rf'^\s*["\'].*{re.escape(import_lower)}'
            ]
        elif language == 'Rust':
            patterns = [
                rf'\buse\s+{re.escape(import_lower)}',
                rf'\bextern\s+crate\s+{re.escape(import_lower)}'
            ]
        else:
            return False
            
        for pattern in patterns:
            if re.search(pattern, content_lower, re.MULTILINE | re.IGNORECASE):
                return True
                
        return False
    
    def should_exclude_project(self, project_path: Path, language: str) -> tuple[bool, List[str]]:
        """检查整个项目是否应该被排除"""
        if language not in self.filter_rules:
            return False, []
            
        rules = self.filter_rules[language]
        exclusion_reasons = []
        
        # 检查是否包含排除的关键词文件
        for exclude_keyword in rules['exclude_keywords']:
            if list(project_path.rglob(exclude_keyword)):
                exclusion_reasons.append(f"Contains {exclude_keyword}")
                
        # 检查代码文件
        excluded_files = []
        total_files = 0
        
        for ext in rules['file_extensions']:
            for file_path in project_path.rglob(f"*{ext}"):
                total_files += 1
                if self.should_exclude_file(file_path, language):
                    excluded_files.append(file_path.name)
                    
        # 如果超过30%的文件被排除，则排除整个项目
        if total_files > 0 and len(excluded_files) / total_files > 0.3:
            exclusion_reasons.append(f"Too many excluded files: {len(excluded_files)}/{total_files}")
            
        return len(exclusion_reasons) > 0, exclusion_reasons
    
    def filter_projects(self):
        """执行项目过滤"""
        stats = {}
        
        for language_dir in self.source_dir.iterdir():
            if not language_dir.is_dir():
                continue
                
            language = language_dir.name
            print(f"\n处理语言: {language}")
            
            # 创建目标语言目录
            target_lang_dir = self.target_dir / language
            target_lang_dir.mkdir(exist_ok=True)
            
            total_projects = 0
            filtered_projects = 0
            excluded_projects = 0
            
            for project_dir in language_dir.iterdir():
                if not project_dir.is_dir():
                    continue
                    
                total_projects += 1
                project_name = project_dir.name
                
                # 检查项目是否应该被排除
                should_exclude, reasons = self.should_exclude_project(project_dir, language)
                
                if should_exclude:
                    excluded_projects += 1
                    print(f"  排除项目: {project_name}")
                    for reason in reasons:
                        print(f"    原因: {reason}")
                else:
                    # 复制项目到过滤后的目录
                    target_project_dir = target_lang_dir / project_name
                    try:
                        shutil.copytree(project_dir, target_project_dir, dirs_exist_ok=True)
                        filtered_projects += 1
                        print(f"  保留项目: {project_name}")
                    except Exception as e:
                        print(f"  复制项目失败 {project_name}: {e}")
            
            stats[language] = {
                'total': total_projects,
                'filtered': filtered_projects,
                'excluded': excluded_projects
            }
            
            print(f"  统计: 总计 {total_projects}, 保留 {filtered_projects}, 排除 {excluded_projects}")
        
        # 保存统计信息
        stats_file = self.target_dir / 'filter_stats.json'
        with open(stats_file, 'w', encoding='utf-8') as f:
            json.dump(stats, f, indent=2, ensure_ascii=False)
            
        print(f"\n过滤完成！统计信息已保存到: {stats_file}")
        return stats

def main():
    # 配置路径
    source_dir = "github_repos"
    target_dir = "primary_filter"
    
    # 检查源目录是否存在
    if not os.path.exists(source_dir):
        print(f"错误: 源目录 {source_dir} 不存在")
        return
    
    # 创建过滤器并执行过滤
    filter_instance = ProjectFilter(source_dir, target_dir)
    stats = filter_instance.filter_projects()
    
    # 打印总体统计
    print("\n=== 总体统计 ===")
    total_all = sum(s['total'] for s in stats.values())
    filtered_all = sum(s['filtered'] for s in stats.values())
    excluded_all = sum(s['excluded'] for s in stats.values())
    
    print(f"总项目数: {total_all}")
    print(f"保留项目数: {filtered_all}")
    print(f"排除项目数: {excluded_all}")
    print(f"保留比例: {filtered_all/total_all*100:.1f}%")

if __name__ == "__main__":
    main()
    