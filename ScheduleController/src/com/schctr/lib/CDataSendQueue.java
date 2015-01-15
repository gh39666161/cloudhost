/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.schctr.lib;

import com.tools.bean.CBaseDataBean;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 *
 * @author Administrator
 */
public class CDataSendQueue {

    BlockingQueue<CBaseDataBean> QDataFileds[]=new SynchronousQueue[3];

    public CDataSendQueue() {
        for (int i = 0; i < 3; i++) {
            QDataFileds[i] = new SynchronousQueue();
        }
    }

    public void enter(int hostIndex ,CBaseDataBean cbdb) {
        try {
            QDataFileds[hostIndex].put(cbdb);
        } catch (InterruptedException ex) {
        }
    }

    public CBaseDataBean read(int hostIndex) {
        CBaseDataBean cbdb = null;
        try {
            cbdb = QDataFileds[hostIndex].take();
        } catch (InterruptedException ex) {
        }
        return cbdb;
    }
}
