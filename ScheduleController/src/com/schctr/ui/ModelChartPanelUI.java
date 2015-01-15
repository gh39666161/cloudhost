/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.schctr.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author Administrator
 */
public class ModelChartPanelUI extends JPanel {

    /**
     * 边距
     *
     */
    private final int BODER = 12;

    /**
     * O点X边距
     *
     */
    private final int MIN_X = 24;

    /**
     * O点Y边距
     *
     */
    private final int MIN_Y = 16;

    /**
     * 红背景
     *
     */
    private final Color BG_R = new Color(255, 191, 191);

    /**
     * 黄背景
     *
     */
    private final Color BG_Y = new Color(255, 255, 191);

    /**
     * 绿背景
     *
     */
    private final Color BG_G = new Color(191, 255, 191);

    /**
     * 蓝背景
     *
     */
    private final Color BG_B = new Color(160, 191, 255);

    /**
     * 大点颜色
     */
    private final Color PT_ORG = new Color(255, 128, 0);
    /**
     * x,y的坐标刻度个数 不算0点
     */
    private final int N_XRule = 5;
    private final int N_YRule = 10;

    /**
     * x,y的最大坐标刻度
     */
    private final int XRuleMax = 50;
    private final int YRuleMax = 500;

    /**
     * x,y的刻度尺
     */
    private int XRule[];
    private int YRule[];

    /**
     * 运行时间间隔
     */
    private int time;

    /**
     * 时间间隔单位
     */
    private String unit;

    /**
     * 绘图区域
     */
    private Rectangle viewZone;

    /**
     * 绘制数据链表
     */
    private ArrayList<Point> alPointData;

    /**
     * 当前绘制点的索引
     */
    private int index;

    public ModelChartPanelUI() {
        //初始化刻度数据
        this.XRule = new int[this.N_XRule + 1];
        this.YRule = new int[this.N_YRule + 1];
        this.YRule[0] = 0;
        this.XRule[0] = 0;
        for (int i = 1; i <= this.N_XRule; i++) {
            this.XRule[i] = (this.XRuleMax / this.N_XRule) * i;
        }
        for (int j = 1; j <= this.N_YRule; j++) {
            this.YRule[j] = (this.YRuleMax / this.N_YRule) * j;
        }
        //初始化数据
        this.index = -1;
        this.unit = "";
        this.time = 1;
    }

    /**
     * 设置图像数据
     *
     * @param al 所运行的链表数据
     * @param time 所使用的时间间隔
     * @param unit 时间间隔的单位
     * @return 返回是否设置成功
     */
    public boolean setData(ArrayList al, int time, String unit) {
        if (al.size() != 50) {
            return false;
        }
        this.alPointData = al;
        this.time = time;
        this.unit = unit;
        this.index = -1;
        this.repaint();
        return true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            // 当前面板大小
            Dimension thisSize = this.getSize();
            // 当前图表区域
            viewZone = new Rectangle(MIN_X, BODER,
                    (int) thisSize.getWidth() - BODER - MIN_X,
                    (int) thisSize.getHeight() - BODER - MIN_Y);
            // 绘制BMI背景
            this.drawBackground(g);
            // 绘制X轴和Y轴
            g.setColor(Color.BLACK);
            this.drawDescartes(g);
            // 绘制Y轴刻度
            this.drawMarkY(g);
            // 绘制X轴刻度
            this.drawMarkX(g);
            //绘制曲线
            g.setColor(Color.BLUE);
            this.drawTaskLineBG(g);
        } catch (Exception e) {
        }
    }
    /**
     * 防止窗体重绘运行曲线消失
     * @param g 重绘的画刷
     */
    private void drawTaskLineBG(Graphics g) {
        if (index == 0) {                               //只有一个点的时候
            Point pt = new Point();
            pt.x = this.alPointData.get(index).x;
            pt.y = this.alPointData.get(index).y;
            int x = this.XRuleToScreenX(pt.x);
            int y = this.YRuleToScreenY(pt.y);
            this.drawBigPoint(g, new Point(x, y));
        } else {                                        //绘制多点连线的时候
            for (int i = 0; i < this.index - 1; i++) {
                //相邻坐标
                Point pPrex = this.alPointData.get(i);
                Point pNext = this.alPointData.get(i + 1);
                //屏幕前坐标
                Point pscPrex = new Point();
                pscPrex.x = this.XRuleToScreenX(pPrex.x);
                pscPrex.y = this.YRuleToScreenY(pPrex.y);
                //屏幕后坐标
                Point pscNext = new Point();
                pscNext.x = this.XRuleToScreenX(pNext.x);
                pscNext.y = this.YRuleToScreenY(pNext.y);

                if (this.index == 1) {
                    this.drawBigPoint(g, pPrex);
                }
                g.drawLine(pscPrex.x, pscPrex.y, pscNext.x, pscNext.y);
                this.drawBigPoint(g, pscNext);
            }
        }
    }

    /**
     * 绘制运行曲线
     * @return 绘制成功返回任务量，否则返回-1
     */
    public int drawTaskLine() {
        Graphics g = this.getGraphics();
        g.setColor(Color.BLUE);
        // 判断数据是否全部画完
        if (this.index == 49) {
            return -1;
        } else {
            this.index++;
            if (this.index == 0) {                       //如果当前绘制的是第一个点的情况
                Point pt = new Point();
                pt.x = this.alPointData.get(index).x;
                pt.y = this.alPointData.get(index).y;
                int x = this.XRuleToScreenX(pt.x);
                int y = this.YRuleToScreenY(pt.y);
                this.drawBigPoint(g, new Point(x, y));
                return pt.y;
            } else {                                     //如果当前绘制的是第二个及以后的点时
                //相邻坐标
                Point pPrex = this.alPointData.get(index - 1);
                Point pNext = this.alPointData.get(index);
                //屏幕前坐标
                Point pscPrex = new Point();
                pscPrex.x = this.XRuleToScreenX(pPrex.x);
                pscPrex.y = this.YRuleToScreenY(pPrex.y);
                //屏幕后坐标
                Point pscNext = new Point();
                pscNext.x = this.XRuleToScreenX(pNext.x);
                pscNext.y = this.YRuleToScreenY(pNext.y);

                g.drawLine(pscPrex.x, pscPrex.y, pscNext.x, pscNext.y);
                this.drawBigPoint(g, pscNext);
                return pNext.y;
            }
        }
    }

    /**
     * 实现绘制大点
     *
     * @param pt 要绘制的点中心
     */
    private void drawBigPoint(Graphics g, Point pt) {
        for (int i = 0; i <= 2; i++) {
            g.drawLine(pt.x - 1, pt.y - 1 + i, pt.x + 1, pt.y - 1 + i);
        }
    }

    /**
     * 绘制X轴和Y轴
     *
     * @param g
     *
     */
    private void drawDescartes(Graphics g) {
        // 绘制X轴
        g.drawLine(BODER, viewZone.height + BODER, viewZone.width + MIN_X,
                viewZone.height + BODER);
        // 绘制y轴
        g.drawLine(MIN_X, BODER, MIN_X, viewZone.height + MIN_Y);
    }

    /**
     * 绘制X轴刻度
     *
     * @param g
     * @throws ParseException
     *
     */
    private void drawMarkX(Graphics g) {
        int prX;
        for (int i = 0; i <= this.N_XRule; i++) {
            prX = this.XRuleToScreenX(this.XRule[i]);
            // 绘制刻度线
            g.drawLine(prX, viewZone.y + viewZone.height, prX, viewZone.y + viewZone.height + 4);
            // 绘制文字
            g.drawString(Integer.toString(this.XRule[i] * this.time) + this.unit, prX - 8, viewZone.height + MIN_Y + this.BODER - 2);
        }
    }

    /**
     * 绘制Y轴刻度
     *
     * @param g
     *
     */
    private void drawMarkY(Graphics g) {
        //获取Y刻度对应的Y屏幕坐标
        int prY[] = new int[this.N_YRule + 1];
        for (int i = 0; i <= this.N_YRule; i++) {
            prY[i] = YRuleToScreenY(this.YRule[i]);
        }
        // 绘制刻度
        for (int i = 0; i <= this.N_YRule; i++) {
            // 绘制刻度线
            g.drawLine(MIN_X - 8, prY[i], MIN_X, prY[i]);
            // 绘制刻度文本
            Font ft = this.getFont();
            Font strFnt = new Font(ft.getName(), ft.getStyle(), 9);
            g.setFont(strFnt);
            g.drawString(Integer.toString(this.YRule[i]), 2, prY[i]);
        }
    }

    /**
     * 绘制背景
     *
     * @param viewZone
     * @param g
     */
    private void drawBackground(Graphics g) {

        int prY[] = new int[this.N_YRule + 1];
        for (int i = 0; i <= this.N_YRule; i++) {
            prY[i] = YRuleToScreenY(this.YRule[i]);
        }
        // 大任务区域
        g.setColor(BG_R);
        g.fillRect(viewZone.x, viewZone.y, viewZone.width, prY[8] - viewZone.y);
        // 中任务区域
        g.setColor(BG_Y);
        g.fillRect(viewZone.x, prY[8], viewZone.width, prY[5] - prY[8]);
        // 正常区域
        g.setColor(BG_G);
        g.fillRect(viewZone.x, prY[5], viewZone.width, prY[2] - prY[5]);
        // 轻任务区域
        g.setColor(BG_B);
        g.fillRect(viewZone.x, prY[2], viewZone.width, prY[0] - prY[2]);
    }

    /**
     * y刻度尺转屏幕y坐标
     *
     * @param viewZone 绘制区域
     * @param yRule y刻度尺
     * @return 返回对应的屏幕y坐标
     */
    private int YRuleToScreenY(int yRule) {
        // 数学意义上的Y
        int mathY = (int) (yRule * viewZone.height / this.YRuleMax);
        // 屏幕意义上的Y
        return viewZone.y + viewZone.height - mathY;
    }

    /**
     * x刻度尺转屏幕x坐标
     *
     * @param viewZone 绘制区域
     * @param xRule x坐标
     * @return 返回对应的x屏幕坐标
     */
    private int XRuleToScreenX(int xRule) {
        int x = viewZone.x + (int) (xRule * viewZone.width / this.XRuleMax);
        return x;
    }

    public int getTime() {
        return time;
    }

    public String getUnit() {
        return unit;
    }

}
