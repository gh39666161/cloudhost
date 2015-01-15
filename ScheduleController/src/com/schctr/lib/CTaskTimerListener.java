/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.schctr.lib;

import com.schctr.ui.MainFrm;
import com.schctr.ui.ModelChartPanelUI;
import java.util.TimerTask;

/**
 *
 * @author Administrator
 */
public class CTaskTimerListener extends TimerTask {

    private ModelChartPanelUI mcpui;

    public CTaskTimerListener(ModelChartPanelUI mcpui) {
        this.mcpui = mcpui;
    }

    @Override
    public void run() {
        int task = mcpui.drawTaskLine();
        if (task == -1) {
            MainFrm.cmf.actionRunOver();
            MainFrm.cmf.actionLogMsg("系统消息:任务全部执行结束");
        } else{
            MainFrm.cmf.actionDoTask(task);
        }
    }
}
