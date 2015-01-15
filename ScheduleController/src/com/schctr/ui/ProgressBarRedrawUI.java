/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.schctr.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicProgressBarUI;

/**
 *
 * @author Administrator
 */
public class ProgressBarRedrawUI extends BasicProgressBarUI {

    private JProgressBar jProgressBar;
    private int hostIndex;

    public ProgressBarRedrawUI(JProgressBar jProgressBar,int hostIndex) {
        this.jProgressBar = jProgressBar;
        this.hostIndex = hostIndex;
    }

    @Override
    protected void paintDeterminate(Graphics g, JComponent c) {
        super.paintDeterminate(g, c);
    }

    @Override
    protected void paintString(Graphics g, int x, int y, int width, int height, int amountFull, Insets b) {
        g.setColor(new Color(0,0,0));
        super.paintString(g, x, y, width, height, amountFull, b); //To change body of generated methods, choose Tools | Templates.
    }
}
