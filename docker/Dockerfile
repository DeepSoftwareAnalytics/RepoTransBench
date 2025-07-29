# 多语言开发环境 Docker 镜像
FROM ubuntu:22.04

# 设置非交互式安装
ENV DEBIAN_FRONTEND=noninteractive

# 更新系统并安装基础工具
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    git \
    vim \
    build-essential \
    software-properties-common \
    apt-transport-https \
    ca-certificates \
    gnupg \
    lsb-release \
    && rm -rf /var/lib/apt/lists/*

# 安装 Python 3.11
RUN apt-get update && apt-get install -y \
    python3.11 \
    python3.11-dev \
    python3-pip \
    python3.11-venv \
    && ln -sf /usr/bin/python3.11 /usr/bin/python \
    && ln -sf /usr/bin/python3.11 /usr/bin/python3

# 安装 C/C++ 编译器和工具
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    make \
    cmake \
    gdb \
    valgrind

# 安装 Java 17 (OpenJDK) 和 Maven
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    openjdk-17-jre \
    maven

# 安装 .NET 7.0
RUN wget https://packages.microsoft.com/config/ubuntu/22.04/packages-microsoft-prod.deb -O packages-microsoft-prod.deb \
    && dpkg -i packages-microsoft-prod.deb \
    && rm packages-microsoft-prod.deb \
    && apt-get update \
    && apt-get install -y dotnet-sdk-7.0

# 安装 Node.js 18 (JavaScript)
RUN curl -fsSL https://deb.nodesource.com/setup_18.x | bash - \
    && apt-get install -y nodejs

# 安装 Go 1.21
RUN wget https://go.dev/dl/go1.21.0.linux-amd64.tar.gz \
    && tar -C /usr/local -xzf go1.21.0.linux-amd64.tar.gz \
    && rm go1.21.0.linux-amd64.tar.gz

# 安装 Rust
RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y \
    && /root/.cargo/bin/rustup default stable

# 设置环境变量
ENV PATH="/usr/local/go/bin:/root/.cargo/bin:${PATH}"
ENV JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
ENV DOTNET_ROOT="/usr/share/dotnet"

# 创建工作目录
WORKDIR /workspace

# 验证安装
RUN echo "=== 验证安装 ===" && \
    echo "Python: $(python --version)" && \
    echo "GCC: $(gcc --version | head -n1)" && \
    echo "G++: $(g++ --version | head -n1)" && \
    echo "Java: $(java -version 2>&1 | head -n1)" && \
    echo "Maven: $(mvn --version | head -n1)" && \
    echo "C#/.NET: $(dotnet --version)" && \
    echo "Node.js: $(node --version)" && \
    echo "npm: $(npm --version)" && \
    echo "Go: $(go version)" && \
    echo "Rust: $(rustc --version)"

# 设置默认命令
CMD ["/bin/bash"]


# 在Dockerfile最后添加
ARG USER_ID=1000
ARG GROUP_ID=1000
RUN groupadd -g $GROUP_ID appgroup && \
    useradd -u $USER_ID -g $GROUP_ID -m -s /bin/bash appuser

# 设置工作目录权限
RUN chown -R appuser:appgroup /workspace

# 切换到非root用户
USER appuser

# 更新PATH环境变量
ENV PATH="/home/appuser/.cargo/bin:/usr/local/go/bin:${PATH}"