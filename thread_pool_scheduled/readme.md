# 线程池任务调度模拟器

## 项目简介

基于 Java ThreadPoolExecutor 实现的高并发任务调度模拟器，用于学习和观测线程池工作机制。

## 运行方式

```bash
javac ThreadPoolSimulator.java
java ThreadPoolSimulator
```

## 功能实现

### 核心功能
- 自定义线程池：核心线程4个，最大线程10个，队列容量50
- 任务提交：每秒提交10个任务，持续30秒，共约300个任务
- 实时监控：每秒输出线程池状态信息
- 统计分析：输出最终执行结果和性能数据

### 线程池配置
```java
ThreadPoolExecutor(
    4,                                    // 核心线程数
    10,                                   // 最大线程数
    60L, TimeUnit.SECONDS,               // 线程存活时间
    new LinkedBlockingQueue<>(50),        // 工作队列容量
    new CustomThreadFactory(),            // 自定义线程工厂
    new CustomRejectedExecutionHandler()  // 自定义拒绝策略
);
```

## 主要类说明

### ThreadPoolSimulator（主类）
- **功能**：程序入口，控制整个模拟流程
- **主要方法**：
    - `main()`：启动模拟器
    - `createCustomThreadPool()`：创建线程池
    - `startMonitorThread()`：启动监控线程
    - `startTaskSubmissionThread()`：启动任务提交线程
    - `printFinalStatistics()`：输出最终统计

### CustomThreadFactory（自定义线程工厂）
- **功能**：创建统一命名的线程
- **线程命名格式**：`my-thread-1`, `my-thread-2`, ...
- **实现**：使用 AtomicInteger 保证线程编号唯一

### CustomRejectedExecutionHandler（自定义拒绝策略）
- **功能**：记录被拒绝的任务信息
- **记录内容**：任务ID、拒绝时间、线程池状态
- **输出**：控制台日志 + 内存集合保存

### SimulatedTask（模拟任务）
- **功能**：模拟业务任务执行
- **执行时间**：1-3秒随机耗时
- **统计功能**：记录任务执行时间和线程信息

## 监控指标

### 实时监控（每秒输出）
- Pool size：当前线程池大小
- Active：活跃线程数量
- Completed：已完成任务数
- Queue：队列等待任务数
- Submitted：已提交任务总数
- Rejected：已拒绝任务总数

### 最终统计
- 总共提交/完成/拒绝任务数
- 平均任务执行耗时
- 最大并发线程数
- 被拒绝任务详细日志

## 输出示例

```
[Monitor] Pool size: 6, Active: 4, Completed: 128, Queue: 46, Submitted: 150, Rejected: 0
[COMPLETED] Task-15 executed by my-thread-3, took 1843ms
[REJECTED] Task-245 rejected at 1640995200000, Pool size: 10, Active: 10, Queue: 50

========== 最终统计结果 ==========
总共提交任务数: 300
总共完成任务数: 245
总共拒绝任务数: 55
平均任务执行耗时: 1987ms
最大并发线程数: 10
```

- **线程安全**：使用 AtomicInteger、AtomicLong 保证统计数据准确性
- **守护线程**：监控线程设置为守护线程，不影响程序正常退出
- **拒绝策略**：自定义拒绝策略，详细记录拒绝原因和上下文
- **并发控制**：通过线程池参数控制并发度和资源使用