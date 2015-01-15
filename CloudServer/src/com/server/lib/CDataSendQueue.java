/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.server.lib;

import com.tools.bean.CBaseDataBean;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 *
 * @author Administrator
 */
public class CDataSendQueue {

    BlockingQueue<CBaseDataBean> QDataFileds;
    
    public CDataSendQueue(){
        QDataFileds = new SynchronousQueue();
    }

    public void enter(CBaseDataBean cbdb) {
        try {
            QDataFileds.put(cbdb);
        } catch (InterruptedException ex) {
        }
    }

    public CBaseDataBean read() {
        CBaseDataBean cbdb = null;
        try {
            cbdb = QDataFileds.take();
        } catch (InterruptedException ex) {
        }
        return cbdb;
    }
}
