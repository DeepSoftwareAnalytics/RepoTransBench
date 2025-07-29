# file path: RepoTransAgent/actions.py

import re
from dataclasses import dataclass, field
from typing import Optional, Any
from abc import ABC


def remove_quote(text: str) -> str:
    """Remove surrounding quotes from text"""
    for quote in ['"', "'", "`"]:
        if text.startswith(quote) and text.endswith(quote):
            text = text[1:-1]
            text = text.replace(f"\\{quote}", quote)
            break
    return text.strip()


@dataclass
class Action(ABC):
    action_type: str = field(
        repr=False,
        metadata={"help": 'type of action'}
    )

    @classmethod
    def get_action_description(cls) -> str:
        return "Base action class"

    @classmethod
    def parse_action_from_text(cls, text: str) -> Optional[Any]:
        raise NotImplementedError


@dataclass
class CreateFile(Action):
    action_type: str = field(
        default="create_file",
        init=False,
        repr=False,
        metadata={"help": 'create file action'}
    )

    filepath: str = field(
        metadata={"help": 'path to the file to create'}
    )

    content: str = field(
        metadata={"help": 'content of the file'}
    )

    @classmethod
    def get_action_description(cls) -> str:
        return """
## CreateFile Action
* Signature: CreateFile(filepath="path/to/file.ext"):
```
file_content_here
```
* Description: Create complete translated implementations. This is your PRIMARY action with full context available.
* Critical: Implement FULL functionality based on source code analysis, not empty stubs.
* Context Usage: Use the provided source code to implement exact functionality in target language.
* Example:
CreateFile(filepath="src/lunar.py"):
```python
class Lunar:
    def __init__(self, year, month, day):
        # Complete implementation based on source code
        self.year = year
        self.month = month  
        self.day = day
        self._validate_date()
        
    def to_solar(self):
        # Full algorithm translated from source
        return self._lunar_to_solar_conversion()
        
    def _validate_date(self):
        # Complete validation logic from source
        if not (1 <= self.month <= 12):
            raise ValueError("Invalid month")
        # ... full implementation
```
"""

    @classmethod
    def parse_action_from_text(cls, text: str) -> Optional['CreateFile']:
        # Match CreateFile(filepath="..."):
        # followed by ``` content ```
        pattern = r'CreateFile\(filepath=(.*?)\):\s*```[^\n]*\n(.*?)\n```'
        match = re.search(pattern, text, re.DOTALL)
        
        if match:
            filepath = remove_quote(match.group(1).strip())
            content = match.group(2)
            return cls(filepath=filepath, content=content)
        return None

    def __repr__(self) -> str:
        return f'CreateFile(filepath="{self.filepath}")'


@dataclass
class ReadFile(Action):
    action_type: str = field(
        default="read_file",
        init=False,
        repr=False,
        metadata={"help": 'read file action'}
    )

    filepath: str = field(
        metadata={"help": 'path to the file to read'}
    )

    @classmethod
    def get_action_description(cls) -> str:
        return """
## ReadFile Action
* Signature: ReadFile(filepath="path/to/file.ext")
* Description: Read current implementation files to verify or debug. Source code is already provided in context.
* Use When: Need to check current implementation state or verify generated files.
* Not Needed For: Source code analysis (already provided in initial context).
* Example: ReadFile(filepath="src/generated_class.py")
"""

    @classmethod
    def parse_action_from_text(cls, text: str) -> Optional['ReadFile']:
        pattern = r'ReadFile\(filepath=(.*?)\)'
        match = re.search(pattern, text)
        
        if match:
            filepath = remove_quote(match.group(1).strip())
            return cls(filepath=filepath)
        return None

    def __repr__(self) -> str:
        return f'ReadFile(filepath="{self.filepath}")'


@dataclass
class ExecuteCommand(Action):
    action_type: str = field(
        default="execute_command",
        init=False,
        repr=False,
        metadata={"help": 'execute shell command action'}
    )

    command: str = field(
        metadata={"help": 'shell command to execute'}
    )

    @classmethod
    def get_action_description(cls) -> str:
        return """
## ExecuteCommand Action
* Signature: ExecuteCommand(command="shell_command")
* Description: Execute commands for testing and verification. Project structure already provided in context.
* Primary Use: Testing implementations to verify correctness.
* Key Commands:
  - pytest tests/ -v       # Run tests to verify implementations
  - python -m pytest       # Alternative test command
  - npm test               # For JavaScript projects
  - mvn test               # For Java projects
  - python src/main.py     # Run main application
* Not Needed: tree, ls commands (structure already in context)
* Example: ExecuteCommand(command="pytest tests/ -v")
"""

    @classmethod
    def parse_action_from_text(cls, text: str) -> Optional['ExecuteCommand']:
        pattern = r'ExecuteCommand\(command=(.*?)\)'
        match = re.search(pattern, text)
        
        if match:
            command = remove_quote(match.group(1).strip())
            return cls(command=command)
        return None

    def __repr__(self) -> str:
        return f'ExecuteCommand(command="{self.command}")'


@dataclass
class SearchContent(Action):
    action_type: str = field(
        default="search_content",
        init=False,
        repr=False,
        metadata={"help": 'search content action'}
    )

    keyword: str = field(
        metadata={"help": 'keyword to search for'}
    )

    @classmethod
    def get_action_description(cls) -> str:
        return """
## SearchContent Action
* Signature: SearchContent(keyword="search_term")
* Description: Search within current working directory files. Source context already provided.
* Use When: Need to find specific patterns in your generated implementations.
* Limited Use: Source code analysis already provided in initial context.
* Example: SearchContent(keyword="class MyClass")
"""

    @classmethod
    def parse_action_from_text(cls, text: str) -> Optional['SearchContent']:
        pattern = r'SearchContent\(keyword=(.*?)\)'
        match = re.search(pattern, text)
        
        if match:
            keyword = remove_quote(match.group(1).strip())
            return cls(keyword=keyword)
        return None

    def __repr__(self) -> str:
        return f'SearchContent(keyword="{self.keyword}")'


@dataclass
class Finished(Action):
    action_type: str = field(
        default="finished",
        init=False,
        repr=False,
        metadata={"help": 'task completion action'}
    )

    status: str = field(
        metadata={"help": 'completion status: success or failed'}
    )

    @classmethod
    def get_action_description(cls) -> str:
        return """
## Finished Action
* Signature: Finished(status="success|failed")
* Description: Mark translation task complete. Use "success" ONLY when ALL tests pass.
* Critical: Verify all tests pass before declaring success.
* Context: You have complete source code context to ensure full implementation.
* Example: Finished(status="success")
"""

    @classmethod
    def parse_action_from_text(cls, text: str) -> Optional['Finished']:
        pattern = r'Finished\(status=(.*?)\)'
        match = re.search(pattern, text)
        
        if match:
            status = remove_quote(match.group(1).strip())
            return cls(status=status)
        return None

    def __repr__(self) -> str:
        return f'Finished(status="{self.status}")'


# All available actions for the enhanced context-aware translation agent
AVAILABLE_ACTIONS = [
    CreateFile,
    ReadFile, 
    ExecuteCommand,
    SearchContent,
    Finished
]