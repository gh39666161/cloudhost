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
public class CMsgDispatchTask extends CBaseDataBean {

    /**
     * 分配的任务执行次数
     */
    private int task;
    /**
     * 任务执行比例
     */
    private int taskPercent;
    /**
     * 任务总比数
     */
    private int taskTotal;
    /**
     * 任务程序的名字
     */
    private String taskFileName;
    /**
     * 任务路径
     */
    private String taskPath;
    /**
     * 任务执行参数
     */
    private String taskRunParams;
    /**
     * 要发给的服务器索引
     */
    private int hostIndex;

    public CMsgDispatchTask() {
        this.setDataType(CMsgTypeBean.MSG_TYPE_DISPATCHTASK);
        this.task = 0;
        this.taskPercent = 0;
        this.taskTotal = 0;
        this.taskFileName = null;
        this.taskRunParams = null;
        this.taskPath = null;
    }

    public int getTask() {
        return task;
    }

    public void setTask(int task) {
        this.task = task;
    }

    public String getTaskFileName() {
        return taskFileName;
    }

    public void setTaskFileName(String taskFileName) {
        this.taskFileName = taskFileName;
    }

    public String getTaskRunParams() {
        return taskRunParams;
    }

    public void setTaskRunParams(String taskRunParams) {
        this.taskRunParams = taskRunParams;
    }

    public int getHostIndex() {
        return hostIndex;
    }

    public void setHostIndex(int hostIndex) {
        this.hostIndex = hostIndex;
    }

    public int getTaskPercent() {
        return taskPercent;
    }

    public void setTaskPercent(int taskPercent) {
        this.taskPercent = taskPercent;
    }

    public int getTaskTotal() {
        return taskTotal;
    }

    public void setTaskTotal(int taskTotal) {
        this.taskTotal = taskTotal;
    }

    public String getTaskPath() {
        return taskPath;
    }

    public void setTaskPath(String taskPath) {
        this.taskPath = taskPath;
    }
    
}
