# file path: testcase_target_agent_batch/prompts/user_prompts.py

import os
import re
from pathlib import Path
import tiktoken
from testcase_target_agent_batch.utils import get_project_structure, truncate_content, read_file_content


def split_file_into_chunks(file_path: Path, content: str, max_tokens_per_chunk: int, encoding: tiktoken.Encoding, overlap_lines: int = 5) -> list:
    """Split a large file into smaller chunks based on token count"""
    if not encoding:
        # Fallback: use character count estimation (roughly 4 chars per token)
        max_chars_per_chunk = max_tokens_per_chunk * 4
        chunks = []
        lines = content.split('\n')
        current_chunk = []
        current_length = 0
        
        for line in lines:
            line_length = len(line) + 1  # +1 for newline
            if current_length + line_length > max_chars_per_chunk and current_chunk:
                chunks.append('\n'.join(current_chunk))
                # Keep overlap_lines for context
                current_chunk = current_chunk[-overlap_lines:] if len(current_chunk) > overlap_lines else []
                current_length = sum(len(l) + 1 for l in current_chunk)
            
            current_chunk.append(line)
            current_length += line_length
        
        if current_chunk:
            chunks.append('\n'.join(current_chunk))
        
        return chunks
    
    # Use actual token encoding
    tokens = encoding.encode(content)
    total_tokens = len(tokens)
    
    if total_tokens <= max_tokens_per_chunk:
        return [content]  # No need to split
    
    chunks = []
    lines = content.split('\n')
    current_chunk_lines = []
    current_tokens = 0
    
    for line in lines:
        line_tokens = len(encoding.encode(line + '\n'))
        
        # If adding this line would exceed the limit and we have content, create a chunk
        if current_tokens + line_tokens > max_tokens_per_chunk and current_chunk_lines:
            chunk_content = '\n'.join(current_chunk_lines)
            chunks.append(chunk_content)
            
            # Keep overlap_lines for context continuity
            overlap_start = max(0, len(current_chunk_lines) - overlap_lines)
            current_chunk_lines = current_chunk_lines[overlap_start:]
            current_tokens = len(encoding.encode('\n'.join(current_chunk_lines)))
        
        current_chunk_lines.append(line)
        current_tokens += line_tokens
    
    # Add the last chunk
    if current_chunk_lines:
        chunk_content = '\n'.join(current_chunk_lines)
        chunks.append(chunk_content)
    
    return chunks


def calculate_content_tokens(content: str, encoding: tiktoken.Encoding) -> int:
    """Calculate token count for content"""
    if not encoding:
        # Rough estimation: 4 characters per token
        return len(content) // 4
    return len(encoding.encode(content))


def create_test_file_batches(repo_path: Path, test_files: list, language: str, encoding: tiktoken.Encoding, max_batch_tokens: int = 25000) -> list:
    """Create test file batches that fit within token limits"""
    batches = []
    current_batch = []
    current_batch_tokens = 0
    max_single_file_tokens = 20000  # Single file shouldn't be too large
    
    for file_path in test_files:
        full_path = repo_path / file_path
        
        try:
            with open(full_path, 'r', encoding='utf-8') as f:
                file_content = f.read()
        except Exception as e:
            # Skip files that can't be read
            continue
        
        if not file_content.strip():
            continue
        
        file_tokens = calculate_content_tokens(file_content, encoding)
        
        # If single file is too large, split it into chunks
        if file_tokens > max_single_file_tokens:
            chunks = split_file_into_chunks(full_path, file_content, max_single_file_tokens, encoding)
            
            for i, chunk in enumerate(chunks):
                chunk_tokens = calculate_content_tokens(chunk, encoding)
                chunk_info = {
                    'file_path': file_path,
                    'content': chunk,
                    'tokens': chunk_tokens,
                    'is_chunk': True,
                    'chunk_index': i + 1,
                    'total_chunks': len(chunks),
                    'language': language
                }
                
                # Check if this chunk fits in current batch
                if current_batch_tokens + chunk_tokens > max_batch_tokens and current_batch:
                    # Save current batch and start new one
                    batches.append(current_batch)
                    current_batch = [chunk_info]
                    current_batch_tokens = chunk_tokens
                else:
                    current_batch.append(chunk_info)
                    current_batch_tokens += chunk_tokens
        else:
            # Regular file that fits within limits
            file_info = {
                'file_path': file_path,
                'content': file_content,
                'tokens': file_tokens,
                'is_chunk': False,
                'chunk_index': 1,
                'total_chunks': 1,
                'language': language
            }
            
            # Check if this file fits in current batch
            if current_batch_tokens + file_tokens > max_batch_tokens and current_batch:
                # Save current batch and start new one
                batches.append(current_batch)
                current_batch = [file_info]
                current_batch_tokens = file_tokens
            else:
                current_batch.append(file_info)
                current_batch_tokens += file_tokens
    
    # Add the last batch if it has content
    if current_batch:
        batches.append(current_batch)
    
    return batches


def format_file_chunks_for_prompt(file_chunks: list, test_type: str = "Original") -> str:
    """Format file chunks for inclusion in prompt"""
    if not file_chunks:
        return f"No {test_type.lower()} test files in this batch"
    
    formatted_chunks = []
    
    for chunk_info in file_chunks:
        file_path = chunk_info['file_path']
        content = chunk_info['content']
        language = chunk_info['language']
        is_chunk = chunk_info['is_chunk']
        chunk_index = chunk_info['chunk_index']
        total_chunks = chunk_info['total_chunks']
        
        if is_chunk and total_chunks > 1:
            title = f"### {file_path} (Part {chunk_index}/{total_chunks})"
            if chunk_index > 1:
                content = "# ... (continued from previous part)\n" + content
            if chunk_index < total_chunks:
                content = content + "\n# ... (continues in next part)"
        else:
            title = f"### {file_path}"
        
        formatted_chunk = f"{title}\n```{language.lower()}\n{content}\n```\n"
        formatted_chunks.append(formatted_chunk)
    
    return '\n'.join(formatted_chunks)


def generate_initial_test_translation_prompt(repo_path: Path, source_language: str, target_language: str, encoding: tiktoken.Encoding = None) -> str:
    """Generate initial user prompt with first batch of test files based on token limits"""
    repo_path = Path(repo_path)
    
    # Get project structure (truncated to avoid too long)
    project_structure = get_project_structure(repo_path)
    project_structure = truncate_content(project_structure, 2000, encoding)
    
    # Read public_test_summary.json if available
    public_test_summary = ""
    summary_path = repo_path / "public_test_summary.json"
    if summary_path.exists():
        public_test_summary = read_file_content(summary_path, max_tokens=800, encoding=encoding)
    
    # Find ALL test files
    original_test_files, public_test_files = find_test_files(repo_path)
    
    # Read some source files for context (keep this small)
    source_files = find_source_files(repo_path, source_language)
    source_code_samples = read_source_samples(repo_path, source_files[:2], source_language, encoding, max_tokens_per_sample=600)
    
    # **IMPROVED: Create token-based batches with stricter limits**
    max_test_content_tokens = 25000  # Limit test content to 25k tokens
    
    # Create batches for both test types
    original_batches = create_test_file_batches(repo_path, original_test_files, source_language, encoding, max_test_content_tokens)
    public_batches = create_test_file_batches(repo_path, public_test_files, source_language, encoding, max_test_content_tokens)
    
    # Get first batch for each type
    first_original_batch = original_batches[0] if original_batches else []
    first_public_batch = public_batches[0] if public_batches else []
    
    # Calculate batch statistics
    first_original_tokens = sum(chunk['tokens'] for chunk in first_original_batch)
    first_public_tokens = sum(chunk['tokens'] for chunk in first_public_batch)
    total_test_tokens = first_original_tokens + first_public_tokens
    
    # Format file chunks for prompt
    first_original_formatted = format_file_chunks_for_prompt(first_original_batch, "Original")
    first_public_formatted = format_file_chunks_for_prompt(first_public_batch, "Public")
    
    # Calculate remaining files
    remaining_original_batches = len(original_batches) - 1 if original_batches else 0
    remaining_public_batches = len(public_batches) - 1 if public_batches else 0
    
    prompt = f"""## Test Case Translation Task - Batch Processing (Batch 1)
**Source Language**: {source_language}
**Target Language**: {target_language}
**Project**: {repo_path.name}

## Source Project Structure
```
{project_structure}
```

## Public Test Summary Reference
{f'```json{chr(10)}{public_test_summary}{chr(10)}```' if public_test_summary else "No public_test_summary.json found - please analyze file paths and naming patterns to identify public vs original tests."}

## Source Code Context (for understanding test logic)
{chr(10).join(source_code_samples) if source_code_samples else "No source samples available"}

## Test Files Overview
- **Total Original Test Files**: {len(original_test_files)} files (split into {len(original_batches)} batches)
- **Total Public Test Files**: {len(public_test_files)} files (split into {len(public_batches)} batches)
- **This batch test content**: ~{total_test_tokens:,} tokens ({first_original_tokens:,} original + {first_public_tokens:,} public)
- **Remaining batches**: {remaining_original_batches} original + {remaining_public_batches} public

## First Batch Test Files (Token-Limited to ~25k)

### Original Test Files (Batch 1/1)
{first_original_formatted}

### Public Test Files (Batch 1/1) 
{first_public_formatted}

## Your Tasks for This Session

### Task 1: Project Setup and Initial Translation
1. **Analyze the provided test files/chunks** - Understand testing patterns and structure
2. **Design proper {target_language} project structure** following best practices
3. **Create build/dependency configuration files** (pom.xml, package.json, Cargo.toml, etc.)
4. **Create project directory structure** with tests/ and public_tests/ directories
5. **Translate the provided test content** to {target_language} maintaining identical functionality

### Task 2: Handle File Chunks
1. **For chunked files**: Understand that some large files are split into parts
2. **Maintain context**: Ensure chunked files are properly reconstructed in target language
3. **Preserve functionality**: Each chunk should maintain the same logic as the source

### Task 3: Prepare for Remaining Batches
1. **Create batch progress tracking** in `test_translation_progress.json`
2. **Set up infrastructure** that can handle additional batches seamlessly
3. **Create extensible test execution script**

## Translation Strategy (Token-Based Batching)
This uses **token-based batch processing** to manage context length:
- **Batch 1**: Project setup + ~{total_test_tokens:,} tokens of test content
- **Subsequent batches**: Continue with remaining test files (max 25k tokens each)
- **Chunked files**: Large files automatically split with proper context preservation
- **Final batch**: Complete any remaining content and finalize project

## Required Deliverables for This Batch

### 1. Project Infrastructure
- **{target_language} project structure** with proper directories
- **Build/configuration files** with testing dependencies  
- **Basic run_tests.sh script** (extensible for more tests)
- **Project structure document** (`project_structure.txt`)

### 2. Translated Test Content (First Batch)
- **Translated original test content** → `tests/original/` or `tests/`
- **Translated public test content** → `public_tests/` or equivalent
- **Identical test logic** with {target_language} syntax
- **Proper handling of chunked files** (reconstruct properly)

### 3. Batch Progress Tracking
Create `test_translation_progress.json`:
```json
{{
    "source_original_tests": {original_test_files},
    "source_public_tests": {public_test_files},
    "original_batches": {{
        "total_batches": {len(original_batches)},
        "completed_batches": 1,
        "current_batch_files": {[chunk['file_path'] for chunk in first_original_batch]}
    }},
    "public_batches": {{
        "total_batches": {len(public_batches)},
        "completed_batches": 1,
        "current_batch_files": {[chunk['file_path'] for chunk in first_public_batch]}
    }},
    "batch_token_limit": {max_test_content_tokens},
    "current_batch_tokens": {total_test_tokens},
    "project_structure_created": false,
    "build_files_created": false
}}
```

## Success Criteria for This Batch
✅ Project structure and build files created
✅ Test content from this batch translated (~{total_test_tokens:,} tokens)
✅ Identical test functionality preserved (same inputs, outputs, edge cases)
✅ Proper {target_language} testing framework setup
✅ Batch progress tracking system established
✅ **Chunked files properly handled and reconstructed**
✅ **Extensible run_tests.sh script created**

## Notes
- This is **Batch 1** of {max(len(original_batches), len(public_batches))} total batches
- Test content is limited to ~25k tokens per batch to manage context length
- Large files are automatically split into chunks with context preservation  
- Focus on setting up robust infrastructure for handling multiple batches
- Token limits ensure optimal model performance and response quality

{"" if remaining_original_batches == 0 and remaining_public_batches == 0 else "After completing this batch, we'll continue with remaining test content in subsequent iterations."}

Please start by analyzing the provided test content and setting up the complete {target_language} project infrastructure.
"""
    
    return prompt.strip()


def generate_test_translation_followup_prompt(repo_path: Path, source_language: str, target_language: str,
                                            execution_results: str = None, 
                                            file_results: str = None,
                                            encoding: tiktoken.Encoding = None,
                                            original_repo_path: Path = None) -> str:
    """Generate follow-up prompt for next batch or finalization"""
    repo_path = Path(repo_path)
    
    # Use original_repo_path if provided, otherwise try to derive it
    if original_repo_path is None:
        try:
            original_repo_path = Path("public_test_results") / source_language / repo_path.name
            if not original_repo_path.exists():
                for base_path in ["verified_repos/verified_repos_plus", "public_test_results", "."]:
                    potential_path = Path(base_path) / source_language / repo_path.name
                    if potential_path.exists():
                        original_repo_path = potential_path
                        break
        except Exception:
            original_repo_path = repo_path
    
    # Get current structure (truncated)
    current_structure = get_project_structure(repo_path)
    current_structure = truncate_content(current_structure, 1500, encoding)
    
    prompt_parts = [
        f"## Translation Progress and Next Steps",
        f"**Source Language**: {source_language}",
        f"**Target Language**: {target_language}",
        "",
        f"## Current Project Structure",
        "```",
        current_structure,
        "```",
        ""
    ]
    
    # Add previous results if available (truncated)
    if file_results:
        file_results = truncate_content(file_results, 1500, encoding)
        prompt_parts.extend([
            "### Previous File Operations",
            "```",
            file_results,
            "```",
            ""
        ])
        
    if execution_results:
        execution_results = truncate_content(execution_results, 1500, encoding)
        prompt_parts.extend([
            "### Previous Command Results", 
            "```",
            execution_results,
            "```",
            ""
        ])
    
    # Check for progress tracking file
    progress_file = repo_path / "test_translation_progress.json"
    progress_data = None
    if progress_file.exists():
        try:
            import json
            with open(progress_file, 'r', encoding='utf-8') as f:
                progress_data = json.load(f)
        except Exception as e:
            pass
    
    prompt_parts.extend([
        "## Translation Progress Analysis",
        ""
    ])
    
    # **IMPROVED: Better batch processing logic**
    if progress_data:
        original_batches_info = progress_data.get('original_batches', {})
        public_batches_info = progress_data.get('public_batches', {})
        
        original_total = original_batches_info.get('total_batches', 0)
        original_completed = original_batches_info.get('completed_batches', 0)
        public_total = public_batches_info.get('total_batches', 0)
        public_completed = public_batches_info.get('completed_batches', 0)
        
        batch_token_limit = progress_data.get('batch_token_limit', 25000)
        
        # Check if there are more batches to process
        if original_completed < original_total or public_completed < public_total:
            # Get next batch of test files
            original_test_files, public_test_files = find_test_files(original_repo_path)
            
            # Get next batch content
            next_batch_content = ""
            next_batch_tokens = 0
            
            # Process original tests next batch
            if original_completed < original_total:
                original_batches = create_test_file_batches(original_repo_path, original_test_files, source_language, encoding, batch_token_limit)
                if original_completed < len(original_batches):
                    next_original_batch = original_batches[original_completed]
                    original_formatted = format_file_chunks_for_prompt(next_original_batch, "Original")
                    next_batch_content += f"### Original Test Files (Batch {original_completed + 1}/{original_total})\n{original_formatted}\n\n"
                    next_batch_tokens += sum(chunk['tokens'] for chunk in next_original_batch)
            
            # Process public tests next batch if we have token budget
            if public_completed < public_total and next_batch_tokens < batch_token_limit:
                public_batches = create_test_file_batches(original_repo_path, public_test_files, source_language, encoding, batch_token_limit - next_batch_tokens)
                if public_completed < len(public_batches):
                    next_public_batch = public_batches[public_completed]
                    public_formatted = format_file_chunks_for_prompt(next_public_batch, "Public")
                    next_batch_content += f"### Public Test Files (Batch {public_completed + 1}/{public_total})\n{public_formatted}\n\n"
                    next_batch_tokens += sum(chunk['tokens'] for chunk in next_public_batch)
            
            if next_batch_content:
                prompt_parts.extend([
                    f"🔄 **Next Batch Ready**",
                    f"   - Batch tokens: ~{next_batch_tokens:,} / {batch_token_limit:,}",
                    f"   - Original batches: {original_completed + 1}/{original_total}",
                    f"   - Public batches: {public_completed + 1}/{public_total}",
                    "",
                    f"## Next Batch Test Content (~{next_batch_tokens:,} tokens)",
                    "",
                    next_batch_content,
                    "## Tasks for This Batch",
                    "1. **Translate the above test content** maintaining identical functionality",
                    "2. **Handle any chunked files** properly (reconstruct complete files)",
                    "3. **Update test_translation_progress.json** with completed batch info",
                    "4. **Update run_tests.sh** to include new test files",
                    "5. **Verify all translated tests work correctly**",
                    ""
                ])
            else:
                # All batches completed
                prompt_parts.extend([
                    "✅ **All Test Batches Processed**",
                    "   - Ready for final verification and completion",
                    "",
                    "## Final Tasks",
                    "1. **Verify all tests are translated and working**",
                    "2. **Finalize run_tests.sh script**",
                    "3. **Create final project structure document**",
                    "4. **Mark translation as complete with `finished` command**",
                    ""
                ])
        else:
            # All batches completed
            prompt_parts.extend([
                "✅ **Translation Complete - Finalization Phase**",
                ""
            ])
    else:
        # No progress file - check current status
        translation_status = analyze_translation_progress(repo_path, source_language, target_language)
        
        if translation_status['has_translated_tests']:
            prompt_parts.extend([
                f"✅ **Translated Tests Found**: {translation_status['translated_count']} files",
                "   - Ready for final verification and completion",
                "",
                "## Final Tasks",
                "1. **Verify all tests are working correctly**",
                "2. **Finalize run_tests.sh script**", 
                "3. **Create project structure document**",
                "4. **Mark translation as complete**",
                ""
            ])
        else:
            prompt_parts.extend([
                "❌ **No Translated Tests Found**",
                "   - Need to continue with test translation",
                "   - Create batch progress tracking",
                ""
            ])
    
    prompt_parts.extend([
        "## Requirements Reminder",
        f"- All tests must maintain **identical functionality** to source {source_language} tests",
        f"- Use proper {target_language} testing framework syntax and conventions", 
        "- **Handle chunked files properly** - reconstruct complete files in target language",
        "- **Update batch progress tracking** with completed batch information",
        "- **Maintain ~25k token limit** per batch for optimal processing",
        "",
        "Please continue with the next steps in the translation process."
    ])
    
    return "\n".join(prompt_parts)


def read_source_samples(repo_path: Path, source_files: list, language: str, encoding, max_tokens_per_sample: int = 600) -> list:
    """Read sample source files for context with token limit"""
    samples = []
    for file_path in source_files:
        full_path = repo_path / file_path
        content = read_file_content(full_path, max_tokens=max_tokens_per_sample, encoding=encoding)
        if content.strip():
            samples.append(f"### {file_path}\n```{language.lower()}\n{content}\n```")
    return samples


# Keep all other helper functions from the previous version...
def find_test_files(repo_path: Path, check_source: bool = False) -> tuple:
    """Find original and public test files"""
    repo_path = Path(repo_path).resolve()
    
    if check_source:
        test_patterns = [
            'test_*.py', '*_test.py', '*Test.java', '*.test.js', '*.spec.js',
            '*_test.cpp', '*_test.c', '*Test.cs', 'test_*.m', '*_test.m',
            '*_test.go', 'test_*.go', '*_test.rs', '*_test.rb'
        ]
    else:
        test_patterns = [
            'test_*', '*_test*', '*Test*', '*.test.*', '*.spec.*',
            '*test*'
        ]
    
    public_patterns = [
        'public_test_*', '*_public_test*', '*PublicTest*', '*.public.test.*',
        'public_*test*', '*public*test*', 'public'
    ]
    
    original_tests = []
    public_tests = []
    
    try:
        tests_dir = repo_path / "tests"
        public_tests_dir = repo_path / "public_tests"
        
        if public_tests_dir.exists():
            for pattern in test_patterns:
                for file in public_tests_dir.glob(f'**/{pattern}'):
                    if file.is_file() and not file.name.startswith('.'):
                        try:
                            rel_path = str(file.relative_to(repo_path))
                            public_tests.append(rel_path)
                        except ValueError:
                            continue
        
        if tests_dir.exists():
            for pattern in test_patterns:
                for file in tests_dir.glob(f'**/{pattern}'):
                    if file.is_file() and not file.name.startswith('.'):
                        try:
                            rel_path = str(file.relative_to(repo_path))
                            if rel_path not in public_tests:
                                is_public = any(pub_pattern.replace('*', '') in rel_path.lower() 
                                              for pub_pattern in public_patterns)
                                if is_public:
                                    public_tests.append(rel_path)
                                else:
                                    original_tests.append(rel_path)
                        except ValueError:
                            continue
        
        for pattern in test_patterns:
            for file in repo_path.glob(f'**/{pattern}'):
                if any(excl in str(file) for excl in ['.git', 'node_modules', 'venv', '__pycache__', '.pytest_cache']):
                    continue
                
                if file.is_file() and not file.name.startswith('.'):
                    try:
                        rel_path = str(file.relative_to(repo_path))
                        if rel_path not in original_tests and rel_path not in public_tests:
                            is_public = any(pub_pattern.replace('*', '') in rel_path.lower() 
                                          for pub_pattern in public_patterns)
                            if is_public:
                                public_tests.append(rel_path)
                            else:
                                original_tests.append(rel_path)
                    except ValueError:
                        continue
    
    except Exception as e:
        return [], []
    
    return original_tests, public_tests


def find_source_files(repo_path: Path, source_language: str) -> list:
    """Find source files for given language"""
    source_extensions = {
        'Python': ['.py'],
        'JavaScript': ['.js', '.jsx', '.ts', '.tsx'],
        'Java': ['.java'],
        'C++': ['.cpp', '.cc', '.cxx', '.h', '.hpp'],
        'C#': ['.cs'],
        'C': ['.c', '.h'],
        'Rust': ['.rs'],
        'Go': ['.go'],
        'Ruby': ['.rb'],
        'Matlab': ['.m']
    }
    
    extensions = source_extensions.get(source_language, [])
    source_files = []
    
    for ext in extensions:
        for file in repo_path.glob(f'**/*{ext}'):
            if any(excl in str(file) for excl in ['.git', 'node_modules', 'venv', '__pycache__']):
                continue
            if any(test_term in str(file).lower() for test_term in ['test_', '_test', 'test.', '.test', '/test']):
                continue
            try:
                source_files.append(str(file.relative_to(repo_path)))
            except ValueError:
                continue
    
    return source_files


def analyze_translation_progress(repo_path: Path, source_language: str, target_language: str) -> dict:
    """Analyze current translation progress"""
    target_extensions = get_target_extensions(target_language)
    
    translated_files = []
    tests_dir = repo_path / "tests"
    public_tests_dir = repo_path / "public_tests"
    
    # Check for translated test files
    for directory in [tests_dir, public_tests_dir]:
        if directory.exists():
            for ext in target_extensions:
                for file in directory.glob(f'**/*{ext}'):
                    if file.is_file():
                        try:
                            rel_path = str(file.relative_to(repo_path))
                            translated_files.append(rel_path)
                        except ValueError:
                            continue
    
    # Also check root for any test files
    if not translated_files:
        for ext in target_extensions:
            for file in repo_path.glob(f'*{ext}'):
                if file.is_file() and 'test' in file.name.lower():
                    try:
                        rel_path = str(file.relative_to(repo_path))
                        translated_files.append(rel_path)
                    except ValueError:
                        continue
    
    return {
        'has_translated_tests': len(translated_files) > 0,
        'translated_count': len(translated_files),
        'translated_files': translated_files
    }


def get_target_extensions(target_language: str) -> list:
    """Get file extensions for target language"""
    extensions = {
        'Python': ['.py'],
        'Java': ['.java'],
        'C++': ['.cpp', '.hpp', '.cc', '.h'],
        'Rust': ['.rs'],
        'Go': ['.go'],
        'C#': ['.cs'],
        'C': ['.c', '.h'],
        'JavaScript': ['.js', '.jsx'],
        'Matlab': ['.m']
    }
    return extensions.get(target_language, [])


def check_basic_translation_issues(repo_path: Path, target_language: str, translation_status: dict, run_tests_script: Path) -> list:
    """Check for basic translation issues"""
    issues = []
    
    # Check if test files are properly organized
    tests_in_root = any('/' not in f for f in translation_status['translated_files'])
    if tests_in_root:
        issues.append("⚠️ **Issue**: Some test files are in project root - should be in tests/ directory")
    
    # Check for proper file extensions
    target_extensions = get_target_extensions(target_language)
    for file_path in translation_status['translated_files']:
        if not any(file_path.endswith(ext) for ext in target_extensions):
            issues.append(f"⚠️ **Issue**: File `{file_path}` may not have correct {target_language} extension")
    
    # Check run_tests.sh script
    if run_tests_script.exists():
        try:
            script_content = run_tests_script.read_text(encoding='utf-8')
            
            if not os.access(run_tests_script, os.X_OK):
                issues.append("⚠️ **Issue**: run_tests.sh is not executable - run `chmod +x run_tests.sh`")
            
            if not script_content.startswith('#!/bin/bash'):
                issues.append("⚠️ **Issue**: run_tests.sh missing proper shebang (#!/bin/bash)")
            
            language_commands = {
                'Python': ['pytest', 'python'],
                'Java': ['mvn', 'java'],
                'JavaScript': ['npm', 'jest', 'yarn'],
                'C++': ['cmake', 'make'],
                'C#': ['dotnet'],
                'Rust': ['cargo'],
                'Go': ['go'],
                'C': ['gcc', 'make'],
                'Matlab': ['matlab']
            }
            
            expected_commands = language_commands.get(target_language, [])
            if expected_commands and not any(cmd in script_content.lower() for cmd in expected_commands):
                issues.append(f"⚠️ **Issue**: run_tests.sh may not contain {target_language} test commands")
            
        except Exception as e:
            issues.append(f"⚠️ **Issue**: Cannot read run_tests.sh content: {e}")
    
    return issues


def check_build_files(repo_path: Path, target_language: str) -> dict:
    """Check for build configuration files specific to target language"""
    expected_files = {
        'Python': ['requirements.txt', 'setup.py', 'pyproject.toml'],
        'Java': ['pom.xml', 'build.gradle', 'build.xml'],
        'JavaScript': ['package.json', 'yarn.lock', 'package-lock.json'],
        'C++': ['CMakeLists.txt', 'Makefile', 'conanfile.txt'],
        'C#': ['*.csproj', '*.sln', 'Directory.Build.props'],
        'Rust': ['Cargo.toml', 'Cargo.lock'],
        'Go': ['go.mod', 'go.sum'],
        'C': ['Makefile', 'CMakeLists.txt'],
        'Matlab': ['*.prj', 'startup.m']
    }
    
    language_files = expected_files.get(target_language, [])
    found_files = []
    
    for file_pattern in language_files:
        if '*' in file_pattern:
            pattern = file_pattern.replace('*', '')
            for file in repo_path.iterdir():
                if file.is_file() and pattern in file.name:
                    found_files.append(file.name)
        else:
            file_path = repo_path / file_pattern
            if file_path.exists():
                found_files.append(file_pattern)
    
    return {
        'found_files': found_files,
        'expected_files': language_files,
        'has_build_files': len(found_files) > 0
    }


def extract_finished_data(text: str) -> dict:
    """Extract finished block data from model response"""
    finished_pattern = r'```finished\s*\n(.*?)\n```'
    matches = re.findall(finished_pattern, text, re.DOTALL)
    
    if not matches:
        return None
        
    try:
        import json
        json_str = matches[-1].strip()
        json_str = re.sub(r"'", '"', json_str)
        finished_data = json.loads(json_str)
        return finished_data
    except (json.JSONDecodeError, ValueError):
        return None