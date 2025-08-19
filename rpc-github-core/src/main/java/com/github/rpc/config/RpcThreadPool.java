package com.github.rpc.config;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RpcThreadPool {
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 50;
    private static final int QUEUE_CAPACITY = 200;
    private static final long KEEP_ALIVE_TIME = 30L;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private static final ThreadFactory THREAD_FACTORY = new RpcThreadFactory();
    private static final RejectedExecutionHandler REJECTION_HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();

    //单例模式 饿汉
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TIME_UNIT,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
            THREAD_FACTORY,
            REJECTION_HANDLER
    );

    //自定义一个工厂策略
    private static class RpcThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        RpcThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "NoteExecutor-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    public static ExecutorService newExecutor() {
       return EXECUTOR_SERVICE;
    }
}