/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tools.bean;

import java.io.Serializable;
/**
 *
 * @author Administrator
 */
public class CBaseDataBean implements Serializable
{
    private int DataType;
    public CBaseDataBean()
    {
        this.DataType = CMsgTypeBean.MSG_TYPE_NULL;
    }

    public int getDataType() {
        return DataType;
    }

    public void setDataType(int DataType) {
        this.DataType = DataType;
    }
    
}