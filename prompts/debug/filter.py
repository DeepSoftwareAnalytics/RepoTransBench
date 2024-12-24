debug_filter_prompt_template = '''Here is a Java project and corresponding execution result.
You should try to meet the following conditions as much as possible:
1. Your job is to find the source of bug and update the buggy file to fix the bug.
2. The error must be in either `src/main/java` or `pom.xml`, so make the necessary modifications to pass the test.
3. The files in `src/test/java` contain predefined tests that cannot be modified, you must modify files in `src/main/java` or `pom.xml` to meet the tests.

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

**Attention: The files in `src/test/java` contain predefined tests that cannot be modified.**

## Updated Buggy Files
'''
