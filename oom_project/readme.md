# 大日志文件内存崩溃实验

## 项目简介

这是一个用于演示不当日志查看行为导致内存崩溃的Java实验项目。通过对比两种不同的文件读取方式，展示在处理大文件时内存管理的重要性。

## 实验目标

- 模拟生产环境中使用 `cat` 命令查看大日志文件的危险性
- 对比一次性读取整个文件与流式读取的内存使用差异
- 理解JVM内存管理机制和OutOfMemoryError异常

## 项目结构

```
src/
└── com/waaar/oom/
    └── Main.java          # 主程序文件
large.log                  # 生成的大型日志文件（500MB+）
README.md                  # 项目说明文档
```

## 核心功能

### 1. 大文件生成 (`createLargeFile`)
- 创建一个大小约为500MB的日志文件
- 文件内容模拟真实的系统日志格式
- 支持断点续传，避免重复生成

### 2. 方法A：Cat风格读取 (`catByAllFile`)
- 使用 `Files.readAllLines()` 一次性将整个文件加载到内存
- 模拟 `cat large.log` 命令的行为
- 在低内存环境下容易触发OutOfMemoryError

### 3. 方法B：流式读取 (`flowReadFile`)
- 使用 `BufferedReader` 逐行读取文件
- 只读取指定行数（如1000行），控制内存使用
- 模拟 `head -n 1000 large.log` 的安全行为

## 运行环境要求

- JDK 8+
- 建议使用低内存参数运行：`-Xmx256m`

## 运行步骤

### 1. 编译项目
```bash
javac -d out src/com/waaar/oom/Main.java
```

### 2. 运行实验（推荐使用低内存参数）
```bash
java -Xmx256m -cp out com.waaar.oom.Main
```

### 3. 在IDEA中设置JVM参数
1. Run → Edit Configurations...
2. 选择对应的配置
3. 点击 "Modify options" → 勾选 "Add VM options"
4. 在VM options中输入：`-Xmx256m`

## 预期结果

### 方法A（Cat风格）
- **预期行为**：尝试读取整个500MB文件到内存
- **在256MB内存限制下**：很可能触发OutOfMemoryError
- **内存使用**：峰值接近或超过JVM最大内存

### 方法B（流式读取）
- **预期行为**：只读取前1000行，内存使用稳定
- **内存使用**：远低于方法A，通常在几十MB以内
- **执行时间**：明显快于方法A

## 完整读取输出示例

```

正在构造大型日志
已存在文件
Method A : 通过cat完全的读取到内存

[Method A] 统计结果:
[Method A] Total time: 16822 ms
[Method A] Start memory: 3MB
[Method A] After read memory: 0MB
[Method A] Exception: GC overhead limit exceeded

```

## 流式读取

```
正在构造大型日志
已存在文件
输出日志前十行内容
2025-01-01 00:00:00 INFO User login success, userId=123
2025-01-01 00:00:01 ERROR Failed to connect to database2025-01-01 00:00:00 INFO User login success, userId=123
2025-01-01 00:00:01 ERROR Failed to connect to database2025-01-01 00:00:00 INFO User login success, userId=123
2025-01-01 00:00:01 ERROR Failed to connect to database2025-01-01 00:00:00 INFO User login success, userId=123
2025-01-01 00:00:01 ERROR Failed to connect to database2025-01-01 00:00:00 INFO User login success, userId=123
2025-01-01 00:00:01 ERROR Failed to connect to database2025-01-01 00:00:00 INFO User login success, userId=123
2025-01-01 00:00:01 ERROR Failed to connect to database2025-01-01 00:00:00 INFO User login success, userId=123
2025-01-01 00:00:01 ERROR Failed to connect to database2025-01-01 00:00:00 INFO User login success, userId=123
2025-01-01 00:00:01 ERROR Failed to connect to database2025-01-01 00:00:00 INFO User login success, userId=123
2025-01-01 00:00:01 ERROR Failed to connect to database2025-01-01 00:00:00 INFO User login success, userId=123

[Method B] 统计结果:
[Method B] Total time: 2 ms
[Method B] Start memory: 3MB
[Method B] After read memory: 5MB
[Method B] Exception: None
```

