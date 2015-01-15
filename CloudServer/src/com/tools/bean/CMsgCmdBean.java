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
public class CMsgCmdBean extends CBaseDataBean {
    private String cmd;
    public CMsgCmdBean(){
        this.setDataType(CMsgTypeBean.MSG_TYPE_COMMAND);
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
    
}
