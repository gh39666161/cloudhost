/*
 * 云服务器主类
 */

package com.server.main;

import com.server.lib.CAppFileProc;
import com.server.lib.CDataSendQueue;
import com.server.lib.CDoDispatchTask;
import com.server.lib.CNetWork;
import com.server.lib.CServerThreadPool;

/**
 *
 * @author 周明
 */
public class CloudServer {

    /**
     * 消息队列池
     */
    public static CDataSendQueue cdsq = null;
    /**
     * 网络连接池
     */
    public static CNetWork cnw = null;
    /**
     * 多线程池
     */
    public static CServerThreadPool cstp = null;
    /**
     * 文件接收器
     */
    public static CAppFileProc cafp = null;
    /**
     * 任务执行器
     */
    public static CDoDispatchTask cddt = null;
    
    public static void main(String[] args) {
        // TODO code application logic here
        cdsq = new CDataSendQueue();
        cnw = new CNetWork();
        cstp = new CServerThreadPool();
        cafp = new CAppFileProc();
        cddt = new CDoDispatchTask();
        
        cstp.initInstance(10);
        cnw.initCloudServer();
        cnw.startListen();
        
    }
}
