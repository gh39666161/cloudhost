/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.schctr.lib;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;


/**
 *
 * @author Administrator
 */
public class CSysOptLog {
    private List loglist;
    private SimpleDateFormat df;
    public CSysOptLog(){
        loglist = Collections.synchronizedList(new LinkedList());
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
    public void info(String msg){
        String time = df.format(new Date());
        msg = time+"\n"+msg;
        this.loglist.add(msg);
    }
    public void clearAll(){
        this.loglist.clear();
    }
    public Iterator getIterator(){
        return this.loglist.iterator();
    }
}
