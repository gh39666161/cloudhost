/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.server.lib;

import com.tools.bean.CMsgSendFileBean;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Administrator
 */
public class CAppFileProc {

    private File taskFile;
    private String name;
    private String path;
    private String type;
    private long size;
    private boolean isFirstSave;
    private FileOutputStream fos;

    public CAppFileProc() {
        isFirstSave = true;
    }

    public void save(CMsgSendFileBean cmsfb) {
        try {
            if (isFirstSave) {

                this.name = cmsfb.getName();
                this.path = cmsfb.getPath();
                this.type = cmsfb.getType();
                this.size = cmsfb.getSize();
                this.taskFile = new File(this.path + this.name);
                this.fos = new FileOutputStream(taskFile);
                isFirstSave = false;
                System.out.println("From System::Create File " + this.name + " success");
            }
            fos.write(cmsfb.getData());
            if (cmsfb.isIsEnd()) {
                fos.write(cmsfb.getData(), 0, cmsfb.getEndReadSize());
                closefile();
                System.out.println("From System::Recieve File " + this.name + " End");
            }
        } catch (IOException ex) {
        }
    }

    private void closefile() throws IOException {
        isFirstSave = true;
        this.fos.close();
    }
}
