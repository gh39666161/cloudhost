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
public class CMsgSendFileBean extends CBaseDataBean {

    private String name;
    private String path;
    private String type;
    private long size;
    private int DataLength;
    private byte data[];
    private boolean isEnd;
    private int endReadSize;

    public CMsgSendFileBean() {
        this.setDataType(CMsgTypeBean.MSG_TYPE_SENDFILE);
        this.DataLength = 512;
        this.isEnd = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getDataLength() {
        return DataLength;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isIsEnd() {
        return isEnd;
    }

    public void setIsEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getEndReadSize() {
        return endReadSize;
    }

    public void setEndReadSize(int endReadSize) {
        this.endReadSize = endReadSize;
    }
    
}
