debug_direct_prompt_template = '''Here is a Java project and corresponding execution result.
Your job is to find the source of bug and update the buggy file to fix the bug.
You should try to meet the following conditions as much as possible:
1. Ensure that the file directory adheres to the Java project tree.
2. You only need to provide the complete path of the code and the corresponding code, without needing to provide any additional content.
3. You only need to provide the files in src/main/java or `pom.xml`. Don't provide test files in src/test/java.

A simple example of the updated buggy files can be:

### src/main/java/Class1.java
```java
...
```

### pom.xml
```xml
...
```

---
Now, it's your turn:
## Java Project
{java_project_context}


## Execution Result
{exec_result_text}


## Updated Buggy Files
'''
