/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tools.bean;

/**
 *
 * @author Administrator
 */
public class CMsgVerifyFileBean extends CBaseDataBean {
    private String name;
    private String path;
    private long size;
    public CMsgVerifyFileBean(){
        this.setDataType(CMsgTypeBean.MSG_TYPE_VERIFYFILE);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
    
}
