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
public class CMsgTypeBean {

    public static final int MSG_TYPE_NULL = 1000;           //空类型消息
    public static final int MSG_TYPE_COMMAND = 1001;        //命令类型消息
    public static final int MSG_TYPE_FILESEND = 1002;       //文件数据类型消息
    public static final int MSG_TYPE_SERVERSTATE = 1003;    //服务器状态类型消息
    public static final int MSG_TYPE_CUTDOWN = 1004;        //断开控制类型消息
    public static final int MSG_TYPE_VERIFYFILE = 1005;     //文件校验类型消息
    public static final int MSG_TYPE_SENDFILE = 1006;       //文件数据类型消息
    public static final int MSG_TYPE_DISPATCHTASK = 1007;   //分发任务类型消息
}
