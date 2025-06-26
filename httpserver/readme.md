# Java HTTP Server

一个基于Java Socket的轻量级HTTP服务器，支持多种内容类型的请求处理，包括JSON、表单数据、文件上传等。

## 项目概述

本项目是一个完全使用Java Socket API构建的HTTP服务器，无需依赖第三方框架。支持多线程并发处理，能够处理常见的HTTP请求类型和内容格式。

## 功能特性

- ✅ **多线程并发处理** - 使用线程池处理并发请求
- ✅ **支持HTTP/1.1协议** - 包括Keep-Alive连接
- ✅ **多种内容类型支持**：
    - JSON数据 (`application/json`)
    - 表单数据 (`application/x-www-form-urlencoded`)
    - 文件上传 (`multipart/form-data`)
    - 纯文本 (`text/plain`)
- ✅ **请求回显接口** - 便于调试和测试
- ✅ **CORS支持** - 跨域资源共享
- ✅ **请求日志记录** - 详细的请求信息记录
- ✅ **错误处理** - 完善的异常处理机制
- ✅ **Web测试界面** - 内置HTML测试页面

## 项目结构

```
src/main/java/com/waaar/httpserver/
├── Main.java           # 服务器启动入口
├── HttpHandler.java    # HTTP请求处理器
├── HttpRequest.java    # HTTP请求对象
├── HttpResponse.java   # HTTP响应对象
└── Logger.java         # 日志记录工具
```

## 快速开始

### 1. 编译项目

```bash
# 创建项目目录
mkdir java-http-server
cd java-http-server

# 创建包结构
mkdir -p src/main/java/com/waaar/httpserver

# 将源代码文件放入对应目录
# 编译Java文件
javac -d . src/main/java/com/waaar/httpserver/*.java
```

### 2. 启动服务器

```bash
java com.waaar.httpserver.Main
```

服务器将在端口 **8080** 上启动。

### 3. 访问测试

在浏览器中访问：`http://localhost:8080`

## API接口

### 1. 根路径 `/`
- **方法**: GET
- **功能**: 返回欢迎页面和测试表单
- **响应**: HTML页面

### 2. Hello接口 `/hello`
- **方法**: GET, POST
- **功能**: 简单的Hello World响应
- **GET响应**: "Hello, World! (GET请求)"
- **POST响应**: "Hello, World! (POST请求)" + 请求体内容

### 3. 回显接口 `/api/echo`
- **方法**: POST
- **功能**: 回显请求数据，支持多种内容类型
- **支持的Content-Type**:
    - `application/json`
    - `application/x-www-form-urlencoded`
    - `multipart/form-data`
    - `text/plain`

## 使用示例

### JSON请求示例

```bash
curl -X POST http://localhost:8080/api/echo \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","age":25,"city":"北京"}'
```

### 表单数据示例

```bash
curl -X POST http://localhost:8080/api/echo \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "name=张三&age=25&city=北京"
```

### 文件上传示例

```bash
curl -X POST http://localhost:8080/api/echo \
  -F "file=@example.txt" \
  -F "description=测试文件"
```

### 纯文本示例

```bash
curl -X POST http://localhost:8080/api/echo \
  -H "Content-Type: text/plain" \
  -d "这是一段纯文本消息"
```

## 测试界面

项目包含一个内置的HTML测试页面，提供了友好的界面来测试各种请求类型：

1. 启动服务器后访问 `http://localhost:8080`
2. 选择请求方式和内容类型
3. 填入相应的数据
4. 点击"发送请求"查看响应结果

测试界面支持：
- 动态切换不同的内容类型
- 多个表单字段的添加/删除
- 文件上传功能
- 实时响应结果显示

## 配置参数

### 服务器配置
- **端口**: 8080 (可在Main.java中修改)
- **线程池大小**: 10 (可在Main.java中调整)
- **Socket超时**: 30秒 (可在HttpHandler.java中调整)

### 支持的HTTP特性
- HTTP/1.1协议
- Keep-Alive连接
- 分块传输编码
- CORS跨域支持

## 开发说明

### 核心类说明

1. **Main.java**: 服务器入口，创建ServerSocket并管理线程池
2. **HttpHandler.java**: 核心请求处理逻辑，解析HTTP请求并生成响应
3. **HttpRequest.java**: HTTP请求对象，封装请求的所有信息
4. **HttpResponse.java**: HTTP响应对象，封装响应数据
5. **Logger.java**: 日志工具，记录请求详情

### 请求处理流程

1. 接收客户端连接
2. 解析HTTP请求行和请求头
3. 根据Content-Length读取请求体
4. 根据路径和方法路由到相应处理器
5. 生成HTTP响应
6. 发送响应给客户端
7. 根据Connection头决定是否保持连接

### multipart/form-data解析

项目实现了自定义的multipart数据解析器，使用正则表达式提取表单字段：

```java
Pattern pattern = Pattern.compile("name=\"([^\"]+)\"\\s*\\n\\s*\\n([^-]+?)(?=\\n*-{6})", Pattern.DOTALL);
```

该正则表达式能够准确解析multipart边界内的字段名和值。

## 扩展功能

### 添加新的路由

在`HttpHandler.java`的`handleHttpRequest`方法中添加新的路径处理：

```java
if ("/new-endpoint".equals(path)) {
    // 处理新的端点
    return new HttpResponse(200, "application/json", "{\"message\":\"success\"}");
}
```

### 支持新的Content-Type

在`parseRequestBody`方法中添加新的内容类型处理：

```java
else if (contentType.contains("application/xml")) {
    return "XML数据: " + rawBody;
}
```

## 注意事项

1. **安全性**: 这是一个演示项目，生产环境使用需要添加安全验证
2. **性能**: 未进行性能优化，大量并发请求可能需要调整线程池大小
3. **协议完整性**: 仅实现了HTTP协议的基本功能
4. **错误处理**: 已包含基本的错误处理，可根据需要扩展

## 系统要求

- Java 8 或更高版本
- 无需额外依赖

