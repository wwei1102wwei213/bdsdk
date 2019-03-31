package com.wei.wlib.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WLibThreadPoolManager {

    //核心线程数
    private static final int CORE_POOL_SIZE = 16;
    //最大线程数
    private static final int MAX_POOL_SIZE = 64;
    //线程池中超过corePoolSize数目的空闲线程最大存活时间；可以allowCoreThreadTimeOut(true)使得核心线程有效时间
    private static final int KEEP_ALIVE_TIME = 1;
    //任务队列
    private static BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(64);
    private static ThreadPoolExecutor mPool;

    /**
     * 执行任务，当线程池处于关闭，将会重新创建新的线程池
     */
    public synchronized static void execute(Runnable run) {
        if (run == null) {
            return;
        }
        if (mPool == null || mPool.isShutdown()) {
            //参数说明
            //当线程池中的线程小于mCorePoolSize，直接创建新的线程加入线程池执行任务
            //当线程池中的线程数目等于mCorePoolSize，将会把任务放入任务队列BlockingQueue中
            //当BlockingQueue中的任务放满了，将会创建新的线程去执行，
            //但是当总线程数大于mMaximumPoolSize时，将会抛出异常，交给RejectedExecutionHandler处理
            //mKeepAliveTime是线程执行完任务后，且队列中没有可以执行的任务，存活的时间，后面的参数是时间单位
            //ThreadFactory是每次创建新的线程工厂
            mPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue);
            mPool.allowCoreThreadTimeOut(true);
        }
        mPool.execute(run);
    }

    /**
     * 取消线程池中某个还未执行的任务
     */
    public synchronized static boolean cancel(Runnable run) {
        if (mPool != null && (!mPool.isShutdown() || mPool.isTerminating())) {
            return mPool.getQueue().remove(run);
        } else {
            return false;
        }
    }

    /**
     * 查看线程池中是否还有某个还未执行的任务
     */
    public synchronized static boolean contains(Runnable run) {
        if (mPool != null && (!mPool.isShutdown() || mPool.isTerminating())) {
            return mPool.getQueue().contains(run);
        } else {
            return false;
        }
    }

    /**
     * 立刻关闭线程池，并且正在执行的任务也将会被中断
     */
    public static void stop() {
        if (mPool != null && (!mPool.isShutdown() || mPool.isTerminating())) {
            mPool.shutdownNow();
        }
    }

    /**
     * 平缓关闭单任务线程池，但是会确保所有已经加入的任务都将会被执行完毕才关闭
     */
    public synchronized static void shutdown() {
        if (mPool != null && (!mPool.isShutdown() || mPool.isTerminating())) {
            mPool.shutdownNow();
        }
    }

}
