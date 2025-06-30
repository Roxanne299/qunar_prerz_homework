package com.waaar.thread;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

/**
 * 自定义线程池任务调度模拟器
 * 模拟高并发任务调度，观测线程池运行过程
 */
public class ThreadPoolSimulator {

    // 统计相关的原子变量
    private static final AtomicInteger totalSubmittedTasks = new AtomicInteger(0);
    private static final AtomicInteger totalRejectedTasks = new AtomicInteger(0);
    private static final AtomicInteger totalCompletedTasks = new AtomicInteger(0);
    private static final AtomicLong totalExecutionTime = new AtomicLong(0);
    private static final AtomicInteger maxConcurrentThreads = new AtomicInteger(0);
    private static final AtomicInteger currentActiveThreads = new AtomicInteger(0);

    // 记录被拒绝的任务信息
    private static final List<String> rejectedTasksLog = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========== 线程池任务调度模拟器启动 ==========");

        // 1. 创建自定义线程池
        ThreadPoolExecutor executor = createCustomThreadPool();

        // 2. 启动监控线程
        startMonitorThread(executor);

        // 3. 启动任务提交线程
        startTaskSubmissionThread(executor);

        // 4. 等待所有任务完成
        Thread.sleep(35000); // 等待30秒任务提交完成 + 5秒缓冲

        // 5. 关闭线程池
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        // 6. 输出最终统计结果
        printFinalStatistics();
    }

    /**
     * 创建自定义线程池
     */
    private static ThreadPoolExecutor createCustomThreadPool() {
        return new ThreadPoolExecutor(
                4,                                    // corePoolSize: 核心线程数
                10,                                   // maximumPoolSize: 最大线程数
                60L,                                  // keepAliveTime: 线程存活时间
                TimeUnit.SECONDS,                     // 时间单位
                new LinkedBlockingQueue<>(50),        // workQueue: 队列容量50
                new CustomThreadFactory(),            // 自定义线程工厂
                new CustomRejectedExecutionHandler()  // 自定义拒绝策略
        );
    }

    /**
     * 自定义线程工厂
     */
    static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "my-thread-" + threadNumber.getAndIncrement());
            // 设置为非守护线程
            thread.setDaemon(false);
            return thread;
        }
    }

    /**
     * 自定义拒绝策略：记录被拒绝的任务
     */
    static class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (r instanceof SimulatedTask) {
                SimulatedTask task = (SimulatedTask) r;
                String rejectionInfo = String.format(
                        "Task-%d rejected at %d, Pool size: %d, Active: %d, Queue: %d",
                        task.getTaskId(),
                        System.currentTimeMillis(),
                        executor.getPoolSize(),
                        executor.getActiveCount(),
                        executor.getQueue().size()
                );

                synchronized (rejectedTasksLog) {
                    rejectedTasksLog.add(rejectionInfo);
                }

                totalRejectedTasks.incrementAndGet();
                System.out.println("[REJECTED] " + rejectionInfo);
            }
        }
    }

    /**
     * 模拟任务类
     */
    static class SimulatedTask implements Runnable {
        private final int taskId;
        private final Random random = new Random();

        public SimulatedTask(int taskId) {
            this.taskId = taskId;
        }

        public int getTaskId() {
            return taskId;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();

            try {
                // 增加活跃线程计数
                int activeCount = currentActiveThreads.incrementAndGet();

                // 更新最大并发数
                maxConcurrentThreads.updateAndGet(current -> Math.max(current, activeCount));

                // 模拟任务耗时：1-3秒随机
                int executionTime = 1000 + random.nextInt(2000);
                Thread.sleep(executionTime);

                // 记录任务完成信息
                long endTime = System.currentTimeMillis();
                long actualExecutionTime = endTime - startTime;

                totalExecutionTime.addAndGet(actualExecutionTime);
                totalCompletedTasks.incrementAndGet();

                System.out.println(String.format(
                        "[COMPLETED] Task-%d executed by %s, took %dms",
                        taskId, Thread.currentThread().getName(), actualExecutionTime
                ));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(String.format("[INTERRUPTED] Task-%d was interrupted", taskId));
            } finally {
                // 减少活跃线程计数
                currentActiveThreads.decrementAndGet();
            }
        }
    }

    /**
     * 启动监控线程，每秒打印线程池状态
     */
    private static void startMonitorThread(ThreadPoolExecutor executor) {
        Thread monitorThread = new Thread(() -> {
            try {
                for (int i = 0; i < 35; i++) { // 监控35秒
                    Thread.sleep(1000);

                    System.out.println(String.format(
                            "[Monitor] Pool size: %d, Active: %d, Completed: %d, Queue: %d, Submitted: %d, Rejected: %d",
                            executor.getPoolSize(),
                            executor.getActiveCount(),
                            executor.getCompletedTaskCount(),
                            executor.getQueue().size(),
                            totalSubmittedTasks.get(),
                            totalRejectedTasks.get()
                    ));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "monitor-thread");

        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    /**
     * 启动任务提交线程，每秒提交10个任务，持续30秒
     */
    private static void startTaskSubmissionThread(ThreadPoolExecutor executor) {
        Thread submissionThread = new Thread(() -> {
            try {
                for (int second = 1; second <= 30; second++) {
                    System.out.println(String.format("========== 第 %d 秒开始提交任务 ==========", second));

                    // 每秒提交10个任务
                    for (int i = 1; i <= 10; i++) {
                        int taskId = (second - 1) * 10 + i;
                        SimulatedTask task = new SimulatedTask(taskId);

                        try {
                            executor.execute(task);
                            totalSubmittedTasks.incrementAndGet();
                            System.out.println(String.format("[SUBMITTED] Task-%d submitted", taskId));
                        } catch (RejectedExecutionException e) {
                            // 这里不应该发生，因为我们使用自定义拒绝策略
                            System.out.println(String.format("[ERROR] Task-%d submission failed", taskId));
                        }

                        // 稍微延迟一下，模拟真实场景
                        Thread.sleep(10);
                    }

                    // 等待到下一秒
                    Thread.sleep(900); // 已经sleep了100ms，再sleep 900ms
                }

                System.out.println("========== 任务提交完成，等待执行完毕 ==========");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "task-submission-thread");

        submissionThread.start();
    }

    /**
     * 打印最终统计结果
     */
    private static void printFinalStatistics() {
        System.out.println("\n========== 最终统计结果 ==========");
        System.out.println("总共提交任务数: " + totalSubmittedTasks.get());
        System.out.println("总共完成任务数: " + totalCompletedTasks.get());
        System.out.println("总共拒绝任务数: " + totalRejectedTasks.get());

        if (totalCompletedTasks.get() > 0) {
            long avgExecutionTime = totalExecutionTime.get() / totalCompletedTasks.get();
            System.out.println("平均任务执行耗时: " + avgExecutionTime + "ms");
        }

        System.out.println("最大并发线程数: " + maxConcurrentThreads.get());

        // 打印被拒绝的任务详情
        if (!rejectedTasksLog.isEmpty()) {
            System.out.println("\n========== 被拒绝的任务详情 ==========");
            synchronized (rejectedTasksLog) {
                for (String log : rejectedTasksLog) {
                    System.out.println(log);
                }
            }
        }

        System.out.println("\n========== 程序执行完毕 ==========");
    }
}