/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.server.lib;

import com.server.main.CloudServer;
import com.tools.bean.CMsgDispatchTask;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Administrator
 */
public class CDoDispatchTask {

    private final boolean Linux = false;
    private final boolean Windows = true;

    public void dotask(final CMsgDispatchTask cmdt) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                int mytask = cmdt.getTask();
                String name = cmdt.getTaskFileName();
                String path = cmdt.getTaskPath();
                String params = cmdt.getTaskRunParams();
                for (int i = 0; i < mytask; i++) {
                    excuteTask(path, name, params);
                    
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                    }
                }
                String msg = "Run_File_Success";
                CloudServer.cnw.send_msg_command(msg);
            }
        };
        CloudServer.cstp.putNewThread(task);
    }

    private void excuteTask(String path, String name, String params) {
        String filePath = path + name;
        Runtime app = Runtime.getRuntime();
        String cmd;
        boolean OperatingSystem = this.getOperateSysByFileName(name);
        if (OperatingSystem == Windows) {
            cmd = filePath + " " + params;
        }else{
            cmd = filePath + " " + params;
        }
        try {
            Process pro =  app.exec(cmd);
        } catch (IOException ex) {
            String msg = "Run_File_Fail";
            CloudServer.cnw.send_msg_command(msg);
        }
    }

    private boolean getOperateSysByFileName(String filename) {
        String regEx = "((.*\\.exe)|(.*\\.bat))";
        Pattern pt = Pattern.compile(regEx);
        Matcher mat = pt.matcher(regEx);
        boolean ismat = mat.find(0);
        return ismat;
    }
}
