/*
 * 线程池类
 */
package com.schctr.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author 周明
 */
public class CServerThreadPool {

    private int ThreadNum = 0;
    private ExecutorService exec = null;

    public void initInstance(int num) {
        ThreadNum = num;
        if (exec == null) {
            exec = Executors.newFixedThreadPool(ThreadNum);
        }
    }

    public void closeThreadPool() {
        if (exec != null) {
            exec.shutdown();
        }
    }

    public void putNewThread(Runnable run) {
        exec.execute(run);
    }
}
