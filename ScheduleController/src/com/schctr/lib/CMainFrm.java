/*
 * 界面校验数据类
 */
package com.schctr.lib;

import com.schctr.ui.ModelChartPanelUI;
import com.schctr.ui.ProgressBarRedrawUI;
import com.schctr.ui.MainFrm;
import com.tools.bean.CMsgDispatchTask;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Timer;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author 周明
 */
public class CMainFrm extends JFrame {

    /**
     * 主窗体句柄
     */
    private MainFrm main = null;

    /**
     * 运行图像的对象
     */
    private ModelChartPanelUI mcpui = null;

    /**
     * 是否校验过文件
     */
    private boolean isVerifyFile;

    /**
     * 主机程序文件校验记录,0代表没连接，1表示校验成功，2表示校验失败
     */
    private int isServerExistFile[];

    /**
     * 运行定时器
     */
    private Timer runner;

    /**
     * 总任务比率
     */
    private final int TOTAL_TASK_NUMBER = 100;

    /**
     * 初始化窗体句柄
     *
     * @param frame 要处理的窗体的句柄
     */
    public void initInstance(MainFrm frame) {
        this.main = frame;

        //初始化校验记录
        this.isVerifyFile = false;
        //初始化程序文件校验标记
        this.isServerExistFile = new int[3];
        for (int i = 0; i < 3; i++) {
            this.isServerExistFile[i] = 0;
        }
        //初始化任务定时器
        runner = new Timer();
    }

    /**
     * 初始化界面的运行图像
     */
    public void actionModelChart() {
        this.mcpui = new ModelChartPanelUI();

        this.main.getjPanelModelGraph().setLayout(new BorderLayout());
        Dimension ds = new Dimension();
        ds.setSize(274, 140);
        this.main.getjPanelModelGraph().setPreferredSize(ds);
        ds.height = ds.height - 2;
        ds.width = ds.width - 2;
        mcpui.setSize(ds);
        this.main.getjPanelModelGraph().add("Center", mcpui);
    }

    /**
     * 初始化系统配置文件
     */
    public void initAppConfig() {
        if (!MainFrm.cafp.loadAppConfig()) {
            this.actionLogMsg("系统信息:加载配置文件失败，请检查配置文件:AppConfig.xml");
        } else {
            this.actionLogMsg("系统信息:初始化系统配置文件AppConfig.xml成功");
        }
    }

    /**
     * 选择文件
     *
     * @param typeName 文件类型名
     * @param type 文件扩展名
     * @return 返回选定文件路径 未选定 返回 NotFound
     */
    public JFileChooser fileChose(String typeName, String type) {
        JFileChooser fileChooser = new JFileChooser("./");
        fileChooser.setMultiSelectionEnabled(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(typeName, type);
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showOpenDialog(fileChooser);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return fileChooser;
        } else {
            return null;
        }
    }

    /**
     * 选择要发送的应用程序
     */
    public void actionChoseProcFile() {
        JFileChooser procfile = fileChose("应用程序", "exe");
        if (procfile == null) {
            this.actionLogMsg("控制信息:未选择任务程序");
        } else {
            String filepath = procfile.getSelectedFile().getAbsolutePath();
            String filename = procfile.getSelectedFile().getName();
            long filesize = new File(filepath).length();
            String filetype = "exe";
            MainFrm.cafp.cfgProcFile(filepath, filename, filetype, filesize);
            this.main.getjTextFieldFileChose().setText(filepath);
            long kbsize = filesize / 1024;
            long mbsize = kbsize / 1024;
            String size;
            if (mbsize >= 1) {
                size = String.valueOf(mbsize) + "MB";
            } else {
                size = String.valueOf(kbsize) + "Kb";
            }
            //设置校验信息为false
            this.isVerifyFile = false;
            for (int i = 0; i < 3; i++) {
                this.isServerExistFile[i] = 0;
            }
            this.actionLogMsg("控制信息:已选择程序--" + filename + "   大小:" + size);
        }
    }

    /**
     * 选择配置文件
     */
    public void actionChoseXmlFile() {
        JFileChooser xmlfile = fileChose("配置文件", "xml");
        if (xmlfile == null) {
            this.actionLogMsg("控制信息:未选择配置文件");
        } else {
            String filepath = xmlfile.getSelectedFile().getAbsolutePath();
            String filename = xmlfile.getSelectedFile().getName();
            long filesize = new File(filepath).length();
            MainFrm.cafp.cfgXmlFile(filepath, filename, filesize);
            this.main.getjTextFieldModelFile().setText(filepath);
            this.actionLogMsg("控制信息:已加成功载配置文件--" + filename);

            this.LoadModelXml();
        }
    }

    /**
     * 解析XML文件数据
     */
    private void LoadModelXml() {
        ArrayList rst = MainFrm.cafp.getRunModelDataList();
        int time = MainFrm.cafp.getRunModelTime();
        String unit = MainFrm.cafp.getRunModelTimeUnit();
        if (rst == null) {
            this.actionLogMsg("系统信息:配置文件解析出错-Point节点错误");
            return;
        }
        if (time == -1) {
            this.actionLogMsg("系统信息:配置文件解析出错-TimeCell节点错误");
            return;
        }
        if ("error".equals(unit)) {
            this.actionLogMsg("系统信息:配置文件解析出错-TimeUnit节点错误");
            return;
        }
        this.mcpui.setData(rst, time, unit);
        this.actionLogMsg("系统信息:配置文件解析成功并重绘表格.");
        this.main.getjButtonRun().setEnabled(true);
    }

    /**
     * 调用云主机执行任务
     */
    public void actionRun() {
        if (!this.isVerifyFile) {
            this.actionLogMsg("系统信息:未校验文件,请先校验文件再执行");
            return;
        }
        CTaskTimerListener Listener = new CTaskTimerListener(this.mcpui);
        String timeUnit = this.mcpui.getUnit();
        int timeCell = this.mcpui.getTime();
        long period;
        switch (timeUnit) {
            case "s":
                period = timeCell * 1000;
                break;
            case "m":
                period = timeCell * 1000 * 60;
                break;
            default:
                period = timeCell * 1000 * 60 * 60;
        }
        this.runner.schedule(Listener, 0, period);
    }

    /**
     * 构造资源完整路径
     *
     * @param type 资源的类型
     * @param name 资源的文件名
     * @return 返回资源路径
     */
    public String getResourceUrl(String type, String name) {
        return "./src/com/resource/" + type + "/" + name + "." + type;
    }

    /**
     * 校验ip地址数据格式
     *
     * @param hostIndex ip地址输入框的对象
     * @return ip格式正确返回true
     */
    public boolean correctIpAddr(int hostIndex) {
        String ip;
        switch (hostIndex) {
            case 0:
                ip = main.getjTextFieldServerAddrOne().getText();
                break;
            case 1:
                ip = main.getjTextFieldServerAddrTwo().getText();
                break;
            default:
                ip = main.getjTextFieldServerAddrThr().getText();
        }
        String regEx = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";
        Pattern pat = Pattern.compile(regEx);
        Matcher mat = pat.matcher(ip);
        boolean isMat = mat.find(0);
        if (ip.equals("000.000.000.000")) {
            isMat = false;
        }
        return isMat;
    }

    /**
     * 校验任务分配填写是否正确
     *
     * @return 正确返回true,错误返回false
     */
    public boolean correctPower() {
        String power[] = new String[3];
        power[0] = this.main.getjTextFieldServerPowerOne().getText();
        power[1] = this.main.getjTextFieldServerPowerTwo().getText();
        power[2] = this.main.getjTextFieldServerPowerThr().getText();
        String regEx = "(([1-9]\\d?)|(100)|(0))";
        Pattern pat = Pattern.compile(regEx);
        Matcher mat;
        boolean isMat = true;
        for (int i = 0; i < 3; i++) {
            mat = pat.matcher(power[i]);
            isMat = isMat && mat.find(0);
        }
        if (isMat) {
            isMat = Integer.parseInt(power[0]) + Integer.parseInt(power[1]) + Integer.parseInt(power[2]) <= 100;
        }
        return isMat;
    }

    /**
     * 连接服务器
     *
     * @param hostIndex 主机索引
     */
    public void actionServerLink(int hostIndex) {

        if (!this.correctIpAddr(hostIndex)) {
            this.actionLogMsg("系统消息:IP地址填写有误!");
            return;
        }
        if (!this.correctPower()) {
            this.actionLogMsg("系统消息:任务分配校验有误!");
            return;
        }
        JTextField jtfAddr;
        JTextField jtfPort;
        JLabel jlbStatue;
        JButton jbtOpt;
        JTextField jtfPower;
        switch (hostIndex) {
            case 0: {
                jtfAddr = main.getjTextFieldServerAddrOne();
                jtfPort = main.getjTextFieldServerPortOne();
                jlbStatue = main.getjLabelServerStatueOne();
                jbtOpt = main.getjButtonServerOptOne();
                jtfPower = main.getjTextFieldServerPowerOne();
                break;
            }
            case 1: {
                jtfAddr = main.getjTextFieldServerAddrTwo();
                jtfPort = main.getjTextFieldServerPortTwo();
                jlbStatue = main.getjLabelServerStatueTwo();
                jbtOpt = main.getjButtonServerOptTwo();
                jtfPower = main.getjTextFieldServerPowerTwo();
                break;
            }
            default: {
                jtfAddr = main.getjTextFieldServerAddrThr();
                jtfPort = main.getjTextFieldServerPortThr();
                jlbStatue = main.getjLabelServerStatueThr();
                jbtOpt = main.getjButtonServerOptThr();
                jtfPower = main.getjTextFieldServerPowerThr();
            }
        }
        String hostIp = jtfAddr.getText();
        String hostPort = jtfPort.getText();
        if (MainFrm.cnw.cServer[hostIndex].isConnection() == false) {
            MainFrm.cnw.LinkToServer(hostIndex, hostIp, hostPort);
        } else {
            MainFrm.cnw.closeOne(hostIndex);

            jtfAddr.setEnabled(true);
            jtfPort.setEnabled(true);
            jtfPower.setEnabled(true);
            jlbStatue.setText("已断开");
            jbtOpt.setText("连接");
        }
    }

    /**
     * 更新界面所显示的连接状态
     *
     * @param isConnection 是否连接成功
     * @param hostIndex 所更新连接状态的主机索引
     */
    public void actionLinkStatue(boolean isConnection, int hostIndex) {
        JTextField jtfAddr;
        JTextField jtfPort;
        JLabel jlbStatue;
        JButton jbtOpt;
        JTextField jtfPower;
        switch (hostIndex) {
            case 0: {
                jtfAddr = main.getjTextFieldServerAddrOne();
                jtfPort = main.getjTextFieldServerPortOne();
                jlbStatue = main.getjLabelServerStatueOne();
                jbtOpt = main.getjButtonServerOptOne();
                jtfPower = main.getjTextFieldServerPowerOne();
                break;
            }
            case 1: {
                jtfAddr = main.getjTextFieldServerAddrTwo();
                jtfPort = main.getjTextFieldServerPortTwo();
                jlbStatue = main.getjLabelServerStatueTwo();
                jbtOpt = main.getjButtonServerOptTwo();
                jtfPower = main.getjTextFieldServerPowerTwo();
                break;
            }
            default: {
                jtfAddr = main.getjTextFieldServerAddrThr();
                jtfPort = main.getjTextFieldServerPortThr();
                jlbStatue = main.getjLabelServerStatueThr();
                jbtOpt = main.getjButtonServerOptThr();
                jtfPower = main.getjTextFieldServerPowerThr();
            }
        }
        if (isConnection == true) {
            jtfAddr.setEnabled(false);
            jtfPort.setEnabled(false);
            jtfPower.setEnabled(false);
            jlbStatue.setText("已连接");
            jbtOpt.setText("断开");
        } else {
            jlbStatue.setText("未连接");
        }
    }

    /**
     * 设置应用程序图标
     */
    public void setAppIcon() {
        String url = this.getResourceUrl("png", "server_1_32");
        Toolkit tool = Toolkit.getDefaultToolkit();
        Image img = tool.createImage(url);
        main.setIconImage(img);
        main.setLocationRelativeTo(null);
    }

    /**
     * 重绘界面上进度条外观
     */
    public void actionRedrawProgressBar() {
        final int MacCpu = 0;
        final int Thread = 1;
        final int JvmMem = 2;
        final int MacMem = 3;
        JProgressBar jpb[][] = new JProgressBar[4][3];
        JProgressBar jtemp;

        jpb[MacCpu][0] = this.main.getjProgressBarMacCpuOne();
        jpb[MacCpu][1] = this.main.getjProgressBarMacCpuTwo();
        jpb[MacCpu][2] = this.main.getjProgressBarMacCpuThr();

        jpb[Thread][0] = this.main.getjProgressBarThreadOne();
        jpb[Thread][1] = this.main.getjProgressBarThreadTwo();
        jpb[Thread][2] = this.main.getjProgressBarThreadThr();

        jpb[JvmMem][0] = this.main.getjProgressBarJvmMemOne();
        jpb[JvmMem][1] = this.main.getjProgressBarJvmMemTwo();
        jpb[JvmMem][2] = this.main.getjProgressBarJvmMemThr();

        jpb[MacMem][0] = this.main.getjProgressBarMacMemOne();
        jpb[MacMem][1] = this.main.getjProgressBarMacMemTwo();
        jpb[MacMem][2] = this.main.getjProgressBarMacMemThr();

        for (int i = 0; i < 4; i++) {
            for (int host = 0; host < 3; host++) {
                jtemp = jpb[i][host];
                jtemp.setUI(new ProgressBarRedrawUI(jtemp, host));
            }
        }
    }

    /**
     * 更新显示界面上主机cpu使用率
     *
     * @param hostIndex 所更新显示的主机的索引
     * @param used 主机cpu使用情况
     */
    public void actionMacCpuUsed(int hostIndex, int used) {
        JProgressBar jpb;
        switch (hostIndex) {
            case 0:
                jpb = this.main.getjProgressBarMacCpuOne();
                break;
            case 1:
                jpb = this.main.getjProgressBarMacCpuTwo();
                break;
            default:
                jpb = this.main.getjProgressBarMacCpuThr();
        }
        jpb.setValue(used);
        jpb.setString(String.valueOf(used));
    }

    /**
     * 更新界面显示的虚拟机内存使用情况
     *
     * @param hostIndex 所需更新显示的主机的索引
     * @param used 主机虚拟机使用内存量
     * @param total 主机虚拟机虚拟内存总量
     */
    public void actionJvmMemUsed(int hostIndex, int used, int total) {
        JProgressBar jpb;
        switch (hostIndex) {
            case 0:
                jpb = this.main.getjProgressBarJvmMemOne();
                break;
            case 1:
                jpb = this.main.getjProgressBarJvmMemTwo();
                break;
            default:
                jpb = this.main.getjProgressBarJvmMemThr();
        }
        int percent;
        if(total == 0){
            percent  = 0;
        }else{
            percent = used * 100 / total;
        }
        jpb.setValue(percent);
        String txt = String.valueOf(used) + "/" + String.valueOf(total) + "MB";
        jpb.setString(txt);
    }

    /**
     * 更新界面物理内存使用情况
     *
     * @param hostIndex 所需更新的主机索引
     * @param used 主机物理内存使用量
     * @param total 主机物理内存配置总量
     */
    public void actionMacMemUsed(int hostIndex, int used, int total) {
        JProgressBar jpb;
        switch (hostIndex) {
            case 0:
                jpb = this.main.getjProgressBarMacMemOne();
                break;
            case 1:
                jpb = this.main.getjProgressBarMacMemTwo();
                break;
            default:
                jpb = this.main.getjProgressBarMacMemThr();
        }
        int percent;
        if(total == 0){
            percent  = 0;
        }else{
            percent = used * 100 / total;
        }
        jpb.setValue(percent);
        String txt = String.valueOf(used) + "/" + String.valueOf(total) + "MB";
        jpb.setString(txt);
    }

    /**
     * 更新界面显示的主机进程总数量
     *
     * @param hostIndex 所需要更新的主机的索引
     * @param num 主机线程当前进程总数
     * @param total 进度条最大线程总数
     */
    public void actionThread(int hostIndex, int num, int total) {
        JProgressBar jpb;
        switch (hostIndex) {
            case 0:
                jpb = this.main.getjProgressBarThreadOne();
                break;
            case 1:
                jpb = this.main.getjProgressBarThreadTwo();
                break;
            default:
                jpb = this.main.getjProgressBarThreadThr();
        }
        int percent = num * 100 / total;
        jpb.setValue(percent);
        jpb.setString(String.valueOf(num));
    }

    /**
     * 清空界面上显示的主机信息
     *
     * @param hostIndex 所需清空主机信息的主机索引
     */
    public void actionStatueClear(int hostIndex) {
        actionMacCpuUsed(hostIndex, 0);
        actionJvmMemUsed(hostIndex, 0, 0);
        actionMacMemUsed(hostIndex, 0, 0);
        actionThread(hostIndex, 0, 300);
    }

    /**
     * 更新和显示执行日志信息
     *
     * @param newlogmsg 新的执行日志信息
     */
    public void actionLogMsg(String newlogmsg) {
        MainFrm.csol.info(newlogmsg);
        Iterator it = MainFrm.csol.getIterator();
        String msg = "";
        while (it.hasNext()) {
            msg += it.next() + "\n";
        }
        JTextArea jta = this.main.getjTextAreaRunLog();
        jta.setText(msg);
        jta.setCaretPosition(jta.getText().length());
    }

    /**
     * 校验目标主机的任务程序
     */
    public void actionVerifyFile() {
        if (!MainFrm.cafp.isProcFileExist()) {
            this.actionLogMsg("界面信息:没有选择有效的校验程序");
        } else {
            for (int i = 0; i < 3; i++) {
                if (MainFrm.cnw.cServer[i].isConnection()) {
                    MainFrm.cnw.send_msg_verifyFile(i);
                    this.actionLogMsg("控制信息:已发送校验数据到" + i + "号服务器");
                }
            }
            //确认校验过
            this.isVerifyFile = true;
        }
    }

    /**
     * 更新显示校验结果
     *
     * @param hostIndex 校验的目标主机索引
     * @param isCorrect 校验结果
     */
    public void actionVerifyFileResult(int hostIndex, boolean isCorrect) {
        String verifymsg;
        if (isCorrect) {
            verifymsg = "校验结果:校验" + hostIndex + "号主机程序文件---------成功";
            this.isServerExistFile[hostIndex] = 1;
        } else {
            verifymsg = "校验结果:校验" + hostIndex + "号主机程序文件---------失败";
            this.isServerExistFile[hostIndex] = 2;
        }
        this.actionLogMsg(verifymsg);
    }
    
    /**
     * 显示主机的执行任务返回结果
     * @param hostIndex 执行任务主机的编号
     * @param isCorrect 是否执行成功
     */
    public void actionRunFileResult(int hostIndex, boolean isCorrect) {
        String verifymsg;
        if (isCorrect) {
            verifymsg = "运行结果:" + hostIndex + "号主机执行任务程序文件---------成功";
        } else {
            verifymsg = "运行错误:" + hostIndex + "号主机执行任务程序文件---------出错";
        }
        this.actionLogMsg(verifymsg);
    }

    /**
     * 更新界面中文件发送进度
     *
     * @param value 发送进度值
     */
    public void actionSendFileValue(int value) {
        this.main.getjProgressBarSendFileProc().setValue(value);
    }

    /**
     * 发送程序文件到主机
     */
    public void actionSendFileProc() {
        if (!MainFrm.cafp.isProcFileExist()) {
            this.actionLogMsg("界面信息:没有选择有效的发送程序");
        } else {
            if (!this.isVerifyFile) {
                this.actionLogMsg("系统信息:没有校验文件,请先校验文件.");
                return;
            }
            for (int i = 0; i < 3; i++) {
                if (this.isServerExistFile[i] == 2) {
                    MainFrm.cnw.send_msg_sendFileData(i);
                    this.isServerExistFile[i] = 1;
                }
            }
        }
    }

    /**
     * 向主机发送调度任务的信息
     *
     * @param task 当前总任务量
     */
    public void actionDoTask(int task) {
        ArrayList lst = this.getTaskSchedual();
        //结合任务总量,完善并发送任务调度信息
        for (int i = 0; i < lst.size(); i++) {
            CMsgDispatchTask cmdt = (CMsgDispatchTask) lst.get(i);
            int mytask = task * cmdt.getTaskPercent() / cmdt.getTaskTotal();
            cmdt.setTask(mytask);
            MainFrm.cnw.send_msg_DispatchTask(cmdt);
            this.actionLogMsg("系统消息:本次任务" + Integer.toString(task) + "个,已向"
                    + Integer.toString(cmdt.getHostIndex()) + "号主机发送"
                    + Integer.toString(mytask) + "个任务.");
        }
    }

    /**
     * 获取并构造任务调度信息
     *
     * @return 返回该次调度信息的列表
     */
    private ArrayList getTaskSchedual() {
        ArrayList<CMsgDispatchTask> task = new ArrayList();
        for (int i = 0; i < 3; i++) {
            if (MainFrm.cnw.cServer[i].isConnection()) {
                CMsgDispatchTask cmdt = new CMsgDispatchTask();
                cmdt.setHostIndex(i);
                cmdt.setTaskTotal(this.TOTAL_TASK_NUMBER);
                JTextField jtf;
                switch (i) {
                    case 0:
                        jtf = this.main.getjTextFieldServerPowerOne();
                        break;
                    case 1:
                        jtf = this.main.getjTextFieldServerPowerTwo();
                        break;
                    default:
                        jtf = this.main.getjTextFieldServerPowerThr();
                }
                cmdt.setTaskPercent(Integer.parseInt(jtf.getText()));
                cmdt.setTaskFileName(MainFrm.cafp.getProFile().getFileName());
                cmdt.setTaskRunParams(this.main.getjTextFieldFileParam().getText());
                cmdt.setTaskPath(MainFrm.cafp.getServerSavePath());
                task.add(cmdt);
            }
        }
        return task;
    }

    /**
     * 任务执行完毕后续处理
     */
    public void actionRunOver() {
        this.main.getjButtonRun().setEnabled(false);
        this.runner.cancel();
    }

}
