func_level_java_file_sketch_prompt_template = '''Here is a file of a Python project and the Java project tree.
Your job is to translate the given file of the Python project into a file of the Java project and try to meet the following conditions as much as possible:
1. Ensure that the file directory adheres to the Java project tree.
2. You only need to provide the complete path of the code and the corresponding code, without needing to provide any additional content.
3. You only need to provide the files in src/main/java. Don't provide test files in src/test/java.

A simple example of the java project files can be:

### src/main/java/Class1.java
```java
...
```

---
Now, it's your turn:
## A File of the Python Project
{python_file_context}


## Java Project Tree
{java_project_tree}


## A File of the Java Project
'''


func_level_java_file_prompt_template = '''Here is a file of a Python project and the Java project tree.
Your job is to translate the given file of the Python project into a file of the Java project and try to meet the following conditions as much as possible:
1. Ensure that the file directory adheres to the Java project tree.
2. You only need to provide the complete path of the code and the corresponding code, without needing to provide any additional content.
3. You only need to provide the files in src/main/java. Don't provide test files in src/test/java.

A simple example of the java project files can be:

### src/main/java/Class1.java
```java
...
```

---
Now, it's your turn:
## A File of the Python Project
{python_file_context}


## Java Project Tree
{java_project_tree}


## A File of the Java Project
'''


func_level_xml_file_prompt_template = '''Here is a Java project and the Java project tree.
Your job is to generate the `pom.xml` file.
Don't provide other files.

A simple example of the `pom.xml` file can be:

### pom.xml
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
...
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.22.2</version>
            </plugin>
        </plugins>
    </build>
</project>
```

---
Now, it's your turn:
## Java Project
{java_project_context}


## Java Project Tree
{java_project_tree}


## `pom.xml` File
'''
