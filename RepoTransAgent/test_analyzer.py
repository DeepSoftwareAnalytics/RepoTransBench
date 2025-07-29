# file path: RepoTransAgent/test_analyzer.py

import re
import subprocess
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List, Optional, Tuple
from abc import ABC, abstractmethod


@dataclass
class TestResult:
    """Single test result"""
    name: str
    module: str
    status: str  # 'passed', 'failed', 'error', 'skipped'
    message: str = ""


@dataclass
class ModuleResult:
    """Test results for a module"""
    module_name: str
    total_tests: int
    passed_tests: int
    failed_tests: int
    error_tests: int
    skipped_tests: int
    
    @property
    def pass_rate(self) -> float:
        if self.total_tests == 0:
            return 0.0
        return (self.passed_tests / self.total_tests) * 100
    
    @property
    def is_module_passed(self) -> bool:
        """Module passes only if ALL tests in it pass"""
        return self.total_tests > 0 and self.passed_tests == self.total_tests


@dataclass
class CompilationResult:
    """Compilation result"""
    success: bool
    errors: int
    warnings: int
    message: str = ""


@dataclass
class AnalysisResult:
    """Complete test analysis result"""
    compilation: CompilationResult
    total_tests: int
    passed_tests: int
    failed_tests: int
    error_tests: int
    skipped_tests: int
    modules: Dict[str, ModuleResult]
    individual_tests: List[TestResult]
    
    @property
    def overall_pass_rate(self) -> float:
        if self.total_tests == 0:
            return 0.0
        return (self.passed_tests / self.total_tests) * 100
    
    @property
    def module_pass_rate(self) -> float:
        if not self.modules:
            return 0.0
        passed_modules = sum(1 for m in self.modules.values() if m.is_module_passed)
        return (passed_modules / len(self.modules)) * 100
    
    @property
    def passed_modules(self) -> int:
        return sum(1 for m in self.modules.values() if m.is_module_passed)
    
    @property
    def total_modules(self) -> int:
        return len(self.modules)


class BaseTestAnalyzer(ABC):
    """Base class for language-specific test analyzers"""
    
    @abstractmethod
    def analyze_output(self, stdout: str, stderr: str, return_code: int) -> AnalysisResult:
        pass
    
    @abstractmethod
    def get_test_commands(self) -> List[str]:
        pass
    
    def extract_module_name(self, test_name: str) -> str:
        """Extract module name from test name"""
        # Default implementation - override for language-specific logic
        if '::' in test_name:
            return test_name.split('::')[0]
        elif '.' in test_name:
            parts = test_name.split('.')
            return '.'.join(parts[:-1]) if len(parts) > 1 else test_name
        else:
            return test_name.split('_')[0] if '_' in test_name else 'main'


class PythonTestAnalyzer(BaseTestAnalyzer):
    """Analyzer for Python pytest output"""
    
    def get_test_commands(self) -> List[str]:
        return ["python -m pytest -v", "pytest -v", "python -m unittest discover -v"]
    
    def analyze_output(self, stdout: str, stderr: str, return_code: int) -> AnalysisResult:
        compilation = CompilationResult(success=True, errors=0, warnings=0)
        
        # Check for import/syntax errors in stderr
        if "SyntaxError" in stderr or "ImportError" in stderr or "ModuleNotFoundError" in stderr:
            error_count = stderr.count("Error")
            compilation = CompilationResult(success=False, errors=error_count, warnings=0, message=stderr[:500])
        
        tests = []
        modules = {}
        
        # Parse pytest output
        test_pattern = r'(\S+)::\S+\s+(PASSED|FAILED|ERROR|SKIPPED)'
        matches = re.findall(test_pattern, stdout)
        
        for match in matches:
            test_file, status = match
            module_name = test_file.replace('.py', '').replace('/', '.').replace('test_', '').replace('_test', '')
            
            test = TestResult(
                name=f"{test_file}::{status.lower()}",
                module=module_name,
                status=status.lower()
            )
            tests.append(test)
            
            # Update module stats
            if module_name not in modules:
                modules[module_name] = ModuleResult(module_name, 0, 0, 0, 0, 0)
            
            modules[module_name].total_tests += 1
            if status == 'PASSED':
                modules[module_name].passed_tests += 1
            elif status == 'FAILED':
                modules[module_name].failed_tests += 1
            elif status == 'ERROR':
                modules[module_name].error_tests += 1
            elif status == 'SKIPPED':
                modules[module_name].skipped_tests += 1
        
        # Parse summary
        summary_pattern = r'=+ (\d+) failed,?\s*(\d+) passed'
        summary_match = re.search(summary_pattern, stdout)
        
        total = len(tests)
        passed = sum(1 for t in tests if t.status == 'passed')
        failed = sum(1 for t in tests if t.status == 'failed')
        error = sum(1 for t in tests if t.status == 'error')
        skipped = sum(1 for t in tests if t.status == 'skipped')
        
        return AnalysisResult(
            compilation=compilation,
            total_tests=total,
            passed_tests=passed,
            failed_tests=failed,
            error_tests=error,
            skipped_tests=skipped,
            modules=modules,
            individual_tests=tests
        )


class JavaTestAnalyzer(BaseTestAnalyzer):
    """Analyzer for Java JUnit output"""
    
    def get_test_commands(self) -> List[str]:
        return ["mvn test", "gradle test", "java -cp ... org.junit.runner.JUnitCore"]
    
    def analyze_output(self, stdout: str, stderr: str, return_code: int) -> AnalysisResult:
        # Check compilation
        compilation_success = "BUILD SUCCESS" in stdout or "BUILD SUCCESSFUL" in stdout
        compile_errors = stdout.count("COMPILATION ERROR") + stderr.count("error:")
        
        compilation = CompilationResult(
            success=compilation_success,
            errors=compile_errors,
            warnings=stdout.count("warning:"),
            message=stderr[:500] if compile_errors > 0 else ""
        )
        
        tests = []
        modules = {}
        
        # Parse Maven/Gradle test output
        test_pattern = r'(\S+\.\S+)\s+Time elapsed.*?(PASSED|FAILED|ERROR|SKIPPED)'
        matches = re.findall(test_pattern, stdout)
        
        for match in matches:
            test_name, status = match
            module_name = '.'.join(test_name.split('.')[:-1])  # Remove method name
            
            test = TestResult(
                name=test_name,
                module=module_name,
                status=status.lower()
            )
            tests.append(test)
            
            # Update module stats
            if module_name not in modules:
                modules[module_name] = ModuleResult(module_name, 0, 0, 0, 0, 0)
            
            modules[module_name].total_tests += 1
            if status == 'PASSED':
                modules[module_name].passed_tests += 1
            elif status == 'FAILED':
                modules[module_name].failed_tests += 1
            elif status == 'ERROR':
                modules[module_name].error_tests += 1
            elif status == 'SKIPPED':
                modules[module_name].skipped_tests += 1
        
        total = len(tests)
        passed = sum(1 for t in tests if t.status == 'passed')
        failed = sum(1 for t in tests if t.status == 'failed')
        error = sum(1 for t in tests if t.status == 'error')
        skipped = sum(1 for t in tests if t.status == 'skipped')
        
        return AnalysisResult(
            compilation=compilation,
            total_tests=total,
            passed_tests=passed,
            failed_tests=failed,
            error_tests=error,
            skipped_tests=skipped,
            modules=modules,
            individual_tests=tests
        )


class CppTestAnalyzer(BaseTestAnalyzer):
    """Analyzer for C++ test output (Google Test, CMake)"""
    
    def get_test_commands(self) -> List[str]:
        return ["make test", "ctest --output-on-failure", "cmake --build . --target test", "dotnet build", "./run_tests.sh"]
    
    def analyze_output(self, stdout: str, stderr: str, return_code: int) -> AnalysisResult:
        # Check compilation - more comprehensive
        compilation_success = True
        compile_errors = 0
        error_message = ""
        
        # Check for various C++ compilation errors
        error_patterns = [
            r'error:',
            r'undefined reference',
            r'fatal error:',
            r'compilation terminated',
            r'collect2: error:'
        ]
        
        combined_output = stdout + "\n" + stderr
        
        for pattern in error_patterns:
            if re.search(pattern, combined_output, re.IGNORECASE):
                compilation_success = False
                compile_errors += len(re.findall(pattern, combined_output, re.IGNORECASE))
        
        if not compilation_success:
            # Extract error messages
            error_lines = []
            for line in combined_output.split('\n'):
                if any(pattern in line.lower() for pattern in ['error:', 'undefined reference', 'fatal error']):
                    error_lines.append(line.strip())
            error_message = ' | '.join(error_lines[:3])  # First 3 errors
        
        compilation = CompilationResult(
            success=compilation_success,
            errors=compile_errors,
            warnings=len(re.findall(r'warning:', combined_output)),
            message=error_message
        )
        
        tests = []
        modules = {}
        
        # Parse various C++ test output formats
        test_patterns = [
            r'\[\s+(OK|FAILED)\s+\]\s+(\S+)\.(\S+)',  # Google Test
            r'(\S+)\s+\.\.\.\s+(PASS|FAIL)',           # Custom test format
            r'Test\s+(\S+)\s+:\s+(PASSED|FAILED)',     # Another format
        ]
        
        for pattern in test_patterns:
            matches = re.findall(pattern, stdout)
            if matches:
                for match in matches:
                    if len(match) == 3:  # Google Test format
                        status_raw, module_name, test_name = match
                        status = 'passed' if status_raw == 'OK' else 'failed'
                        full_name = f"{module_name}.{test_name}"
                    else:  # Other formats
                        test_name, status_raw = match
                        module_name = test_name.split('.')[0] if '.' in test_name else 'main'
                        status = 'passed' if status_raw in ['PASS', 'PASSED'] else 'failed'
                        full_name = test_name
                    
                    test = TestResult(
                        name=full_name,
                        module=module_name,
                        status=status
                    )
                    tests.append(test)
                    
                    # Update module stats
                    if module_name not in modules:
                        modules[module_name] = ModuleResult(module_name, 0, 0, 0, 0, 0)
                    
                    modules[module_name].total_tests += 1
                    if status == 'passed':
                        modules[module_name].passed_tests += 1
                    else:
                        modules[module_name].failed_tests += 1
                break  # Use first matching pattern
        
        total = len(tests)
        passed = sum(1 for t in tests if t.status == 'passed')
        failed = sum(1 for t in tests if t.status == 'failed')
        
        return AnalysisResult(
            compilation=compilation,
            total_tests=total,
            passed_tests=passed,
            failed_tests=failed,
            error_tests=0,
            skipped_tests=0,
            modules=modules,
            individual_tests=tests
        )


class RustTestAnalyzer(BaseTestAnalyzer):
    """Analyzer for Rust cargo test output"""
    
    def get_test_commands(self) -> List[str]:
        return ["cargo test", "cargo test --verbose"]
    
    def analyze_output(self, stdout: str, stderr: str, return_code: int) -> AnalysisResult:
        # Check compilation
        compilation_success = "error[E" not in stderr and "could not compile" not in stderr
        compile_errors = stderr.count("error[E")
        
        compilation = CompilationResult(
            success=compilation_success,
            errors=compile_errors,
            warnings=stderr.count("warning:"),
            message=stderr[:500] if compile_errors > 0 else ""
        )
        
        tests = []
        modules = {}
        
        # Parse cargo test output
        test_pattern = r'test\s+(\S+)\s+\.\.\.\s+(ok|FAILED|ignored)'
        matches = re.findall(test_pattern, stdout)
        
        for match in matches:
            test_name, status_raw = match
            module_name = test_name.split('::')[0] if '::' in test_name else 'main'
            status = 'passed' if status_raw == 'ok' else 'failed' if status_raw == 'FAILED' else 'skipped'
            
            test = TestResult(
                name=test_name,
                module=module_name,
                status=status
            )
            tests.append(test)
            
            # Update module stats
            if module_name not in modules:
                modules[module_name] = ModuleResult(module_name, 0, 0, 0, 0, 0)
            
            modules[module_name].total_tests += 1
            if status == 'passed':
                modules[module_name].passed_tests += 1
            elif status == 'failed':
                modules[module_name].failed_tests += 1
            elif status == 'skipped':
                modules[module_name].skipped_tests += 1
        
        total = len(tests)
        passed = sum(1 for t in tests if t.status == 'passed')
        failed = sum(1 for t in tests if t.status == 'failed')
        skipped = sum(1 for t in tests if t.status == 'skipped')
        
        return AnalysisResult(
            compilation=compilation,
            total_tests=total,
            passed_tests=passed,
            failed_tests=failed,
            error_tests=0,
            skipped_tests=skipped,
            modules=modules,
            individual_tests=tests
        )


class GoTestAnalyzer(BaseTestAnalyzer):
    """Analyzer for Go test output"""
    
    def get_test_commands(self) -> List[str]:
        return ["go test ./...", "go test -v ./..."]
    
    def analyze_output(self, stdout: str, stderr: str, return_code: int) -> AnalysisResult:
        # Check compilation
        compilation_success = "# " not in stderr and "undefined:" not in stderr
        compile_errors = stderr.count("# ")
        
        compilation = CompilationResult(
            success=compilation_success,
            errors=compile_errors,
            warnings=0,
            message=stderr[:500] if compile_errors > 0 else ""
        )
        
        tests = []
        modules = {}
        
        # Parse go test output
        test_pattern = r'=== RUN\s+(\S+).*?--- (PASS|FAIL):\s+\1'
        matches = re.findall(test_pattern, stdout, re.DOTALL)
        
        for match in matches:
            test_name, status_raw = match
            module_name = test_name.split('/')[0] if '/' in test_name else 'main'
            status = 'passed' if status_raw == 'PASS' else 'failed'
            
            test = TestResult(
                name=test_name,
                module=module_name,
                status=status
            )
            tests.append(test)
            
            # Update module stats
            if module_name not in modules:
                modules[module_name] = ModuleResult(module_name, 0, 0, 0, 0, 0)
            
            modules[module_name].total_tests += 1
            if status == 'passed':
                modules[module_name].passed_tests += 1
            else:
                modules[module_name].failed_tests += 1
        
        total = len(tests)
        passed = sum(1 for t in tests if t.status == 'passed')
        failed = sum(1 for t in tests if t.status == 'failed')
        
        return AnalysisResult(
            compilation=compilation,
            total_tests=total,
            passed_tests=passed,
            failed_tests=failed,
            error_tests=0,
            skipped_tests=0,
            modules=modules,
            individual_tests=tests
        )


class CSharpTestAnalyzer(BaseTestAnalyzer):
    """Analyzer for C# dotnet test output"""
    
    def get_test_commands(self) -> List[str]:
        return ["dotnet test", "dotnet test --verbosity normal", "dotnet build"]
    
    def analyze_output(self, stdout: str, stderr: str, return_code: int) -> AnalysisResult:
        # Check compilation - more comprehensive checks
        compilation_success = True
        compile_errors = 0
        error_message = ""
        
        # Check for build failures
        if "Build FAILED" in stdout or "Build FAILED" in stderr:
            compilation_success = False
        
        # Count C# compiler errors
        cs_errors_stdout = len(re.findall(r'error CS\d+:', stdout))
        cs_errors_stderr = len(re.findall(r'error CS\d+:', stderr))
        compile_errors = cs_errors_stdout + cs_errors_stderr
        
        if compile_errors > 0:
            compilation_success = False
            # Extract error messages
            error_lines = []
            for line in (stdout + "\n" + stderr).split('\n'):
                if 'error CS' in line:
                    error_lines.append(line.strip())
            error_message = ' | '.join(error_lines[:3])  # First 3 errors
            
        compilation = CompilationResult(
            success=compilation_success,
            errors=compile_errors,
            warnings=len(re.findall(r'warning CS\d+:', stdout + stderr)),
            message=error_message
        )
        
        tests = []
        modules = {}
        
        # Parse dotnet test output - more flexible patterns
        test_patterns = [
            r'(\S+\.\S+\.\S+)\s+(Passed|Failed|Skipped)',
            r'(\S+)\s+\[(.*?)\]\s+(Passed|Failed|Skipped)',
            r'(Test\w+)\s+(Passed|Failed|Skipped)'
        ]
        
        for pattern in test_patterns:
            matches = re.findall(pattern, stdout)
            if matches:
                break
        
        for match in matches:
            if len(match) >= 2:
                test_name = match[0]
                status_raw = match[-1]  # Last element is status
                module_name = '.'.join(test_name.split('.')[:-1]) if '.' in test_name else 'main'
                status = status_raw.lower()
                
                test = TestResult(
                    name=test_name,
                    module=module_name,
                    status=status
                )
                tests.append(test)
                
                # Update module stats
                if module_name not in modules:
                    modules[module_name] = ModuleResult(module_name, 0, 0, 0, 0, 0)
                
                modules[module_name].total_tests += 1
                if status == 'passed':
                    modules[module_name].passed_tests += 1
                elif status == 'failed':
                    modules[module_name].failed_tests += 1
                elif status == 'skipped':
                    modules[module_name].skipped_tests += 1
        
        total = len(tests)
        passed = sum(1 for t in tests if t.status == 'passed')
        failed = sum(1 for t in tests if t.status == 'failed')
        skipped = sum(1 for t in tests if t.status == 'skipped')
        
        return AnalysisResult(
            compilation=compilation,
            total_tests=total,
            passed_tests=passed,
            failed_tests=failed,
            error_tests=0,
            skipped_tests=skipped,
            modules=modules,
            individual_tests=tests
        )


class TestAnalyzer:
    """Main test analyzer that delegates to language-specific analyzers"""
    
    def __init__(self, language: str):
        self.language = language.lower()
        self.analyzer = self._get_analyzer()
    
    def _get_analyzer(self) -> BaseTestAnalyzer:
        analyzers = {
            'python': PythonTestAnalyzer(),
            'java': JavaTestAnalyzer(),
            'cpp': CppTestAnalyzer(),
            'c++': CppTestAnalyzer(),
            'rust': RustTestAnalyzer(),
            'go': GoTestAnalyzer(),
            'golang': GoTestAnalyzer(),
            'csharp': CSharpTestAnalyzer(),
            'c#': CSharpTestAnalyzer(),
        }
        return analyzers.get(self.language, PythonTestAnalyzer())  # Default to Python
    
    def run_and_analyze(self, working_path: Path) -> AnalysisResult:
        """Run tests and analyze results"""
        commands = self.analyzer.get_test_commands()
        
        for command in commands:
            try:
                # Use shell=True for complex commands that might contain special characters
                result = subprocess.run(
                    command,
                    shell=True,
                    capture_output=True,
                    text=True,
                    cwd=working_path,
                    timeout=120
                )
                
                # Analyze the output
                analysis = self.analyzer.analyze_output(result.stdout, result.stderr, result.returncode)
                
                # If we got meaningful test results, return them
                if analysis.total_tests > 0 or not analysis.compilation.success:
                    return analysis
                    
            except (subprocess.TimeoutExpired, FileNotFoundError):
                continue
        
        # No tests found or all commands failed
        return AnalysisResult(
            compilation=CompilationResult(success=False, errors=1, warnings=0, message="No test runner found"),
            total_tests=0,
            passed_tests=0,
            failed_tests=0,
            error_tests=0,
            skipped_tests=0,
            modules={},
            individual_tests=[]
        )
    
    def format_results(self, analysis: AnalysisResult) -> str:
        """Format analysis results for logging"""
        lines = []
        
        # Compilation status
        if analysis.compilation.success:
            lines.append("✅ Compilation: SUCCESS")
        else:
            lines.append(f"❌ Compilation: FAILED ({analysis.compilation.errors} errors, {analysis.compilation.warnings} warnings)")
            if analysis.compilation.message:
                # Show first 500 characters of error message
                error_msg = analysis.compilation.message[:500].replace('\n', ' ').strip()
                lines.append(f"   Error Details: {error_msg}")
                if len(analysis.compilation.message) > 500:
                    lines.append("   ... (truncated)")
        
        # Overall test results
        lines.append(f"📊 Tests: {analysis.passed_tests}/{analysis.total_tests} passed ({analysis.overall_pass_rate:.1f}%)")
        
        # Show failed test details if any
        if analysis.total_tests > 0 and analysis.failed_tests > 0:
            failed_tests = [t for t in analysis.individual_tests if t.status == 'failed']
            if failed_tests:
                lines.append(f"❌ Failed Tests ({len(failed_tests)}):")
                for test in failed_tests[:3]:  # Show first 3 failed tests
                    lines.append(f"   - {test.name}")
                if len(failed_tests) > 3:
                    lines.append(f"   ... and {len(failed_tests) - 3} more")
        
        # Module results
        if analysis.modules:
            lines.append(f"📦 Modules: {analysis.passed_modules}/{analysis.total_modules} passed ({analysis.module_pass_rate:.1f}%)")
            
            for module_name, module_result in analysis.modules.items():
                status = "✅" if module_result.is_module_passed else "❌"
                lines.append(f"   {status} {module_name}: {module_result.passed_tests}/{module_result.total_tests}")
        
        return "\n".join(lines)