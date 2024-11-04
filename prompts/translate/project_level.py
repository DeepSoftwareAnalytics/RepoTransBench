project_level_prompt_template = '''Here is a Python project and Java project tree.
Your job is to translate the given Python project into a Java project and try to meet the following conditions as much as possible:
1. Ensure that the file path matches the path in Java project tree.
2. You only need to provide the full path of the file and the corresponding code, without needing to provide any additional content.
3. You only need to provide the `pom.xml` file and files in src/main/java. Don't provide test files in src/test/java.

A simple example of the java project files can be:

### src/main/java/Class1.java
```java
...
```

### src/main/java/Class2.java
```java
...
```

### pom.xml
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>fake-java-project</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.7.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>5.7.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

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
## Python Project
{python_project_context}


## Java Project Tree
{java_project_tree}


## Java Project
'''
