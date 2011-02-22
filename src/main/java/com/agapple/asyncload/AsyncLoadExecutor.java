package com.agapple.asyncload;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * �첽���صľ���ִ��������, ֧��Runable��Callable����
 * 
 * @author jianghang 2011-1-21 ����11:32:31
 */
public class AsyncLoadExecutor {

    public static final int        DEFAULT_POOL_SIZE    = 20;
    public static final int        DEFAULT_ACCEPT_COUNT = 100;
    public static final HandleMode DEFAULT_MODE         = HandleMode.REJECT;
    private int                    poolSize;
    private int                    acceptCount;                            // �ȴ����г��ȣ������������ύ����
    private HandleMode             mode;                                   // Ĭ��Ϊ�ܾ��������ڿ���accept���������Ժ�Ĵ�����ʽ
    private ThreadPoolExecutor     pool;
    private volatile boolean       isInit               = false;

    enum HandleMode {
        REJECT, BLOCK;
    }

    public AsyncLoadExecutor(){
        this(DEFAULT_POOL_SIZE, DEFAULT_ACCEPT_COUNT, DEFAULT_MODE);
    }

    public AsyncLoadExecutor(int poolSize){
        this(poolSize, DEFAULT_ACCEPT_COUNT, DEFAULT_MODE);
    }

    public AsyncLoadExecutor(int poolSize, int acceptCount){
        this(poolSize, acceptCount, DEFAULT_MODE);
    }

    public AsyncLoadExecutor(int poolSize, int acceptCount, HandleMode mode){
        this.poolSize = poolSize;
        this.acceptCount = acceptCount;
        this.mode = mode;
    }

    public void initital() {
        if (isInit == false) {
            RejectedExecutionHandler handler = getHandler(mode);
            BlockingQueue queue = getBlockingQueue(acceptCount, mode);
            // ����pool��
            this.pool = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, queue, handler);

            isInit = true;
        }
    }

    public void destory() {
        if (isInit && pool != null) {
            pool.shutdown();
            pool = null;

            isInit = false;
        }
    }

    public <T> Future<T> submit(Callable<T> task) {
        return pool.submit(task);
    }

    public Future<?> submit(Runnable task) {
        return pool.submit(task);
    }

    // ==================== help method ===========================

    private BlockingQueue<?> getBlockingQueue(int acceptCount, HandleMode mode) {
        if (acceptCount < 0) {
            return new LinkedBlockingQueue();
        } else if (acceptCount == 0) {
            return HandleMode.REJECT == mode ? new ArrayBlockingQueue(1) : new SynchronousQueue();
        } else {
            return new ArrayBlockingQueue(acceptCount);
        }
    }

    private RejectedExecutionHandler getHandler(HandleMode mode) {
        return HandleMode.REJECT == mode ? new ThreadPoolExecutor.AbortPolicy() : new ThreadPoolExecutor.DiscardPolicy();
    }

    // ====================== setter / getter ==========================

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public void setAcceptCount(int acceptCount) {
        this.acceptCount = acceptCount;
    }

    public void setMode(HandleMode mode) {
        this.mode = mode;
    }

    public void setMode(String mode) {
        this.mode = HandleMode.valueOf(mode);
    }

    // ======================= help method ==========================

    @Override
    public String toString() {
        return "AsyncLoadExecutor [ poolSize=" + poolSize + ", acceptCount=" + acceptCount + ", mode=" + mode + "]";
    }
}