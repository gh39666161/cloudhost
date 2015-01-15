/*
 * 控制器网络操作库
 */
package com.schctr.lib;

import com.schctr.ui.MainFrm;
import com.tools.bean.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 *
 * @author 周明
 */
public class CNetWork {

    private Selector selector = null;
    private final int MemCache = 1024;
    private final int IntLength = 4;
    public CTcpServerBean cServer[] = new CTcpServerBean[3];

    /**
     * 初始化控制器网络所需的基本数据
     */
    public void initScheduleNet() {
        for (int i = 0; i < 3; i++) {
            cServer[i] = new CTcpServerBean();
        }

        try {
            selector = Selector.open();
        } catch (IOException ex) {
        }

        Dispatcher();
    }

    /**
     * 关闭一个服务器连接
     *
     * @param index 服务器连接的索引号
     */
    public void closeOne(int index) {

        if (cServer[index].isConnection() == true) {
            cServer[index].setConnection(false);
            this.send_msg_cutdown(index);
        }
    }

    /**
     * 关闭所有服务器的连接
     */
    public void closeAll() {
        for (int i = 0; i < 3; i++) {
            closeOne(i);
        }
    }

    /**
     * 连接一个服务器
     *
     * @param hostIndex 服务器索引号
     * @param hostIp 服务器的IP地址
     * @param hostPort 服务器的端口
     */
    public void LinkToServer(int hostIndex, String hostIp, String hostPort) {
        cServer[hostIndex].createChannel(selector, hostIp, hostPort);
    }

    /**
     * 为开辟线程处理服务器消息
     *
     * @param hostIndex 要获取状态的服务器编号
     */
    private void Dispatcher() {

        Runnable run = new Runnable() {
            @Override
            public void run() {
                // 等待事件的循环
                while (true) {
                    try {
                        // 等待
                        selector.selectNow();
                        Thread.sleep(100);
                    } catch (InterruptedException | IOException e) {
                        break;
                    }

                    // 所有事件列表
                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    // 处理每一个事件
                    while (it.hasNext()) {
                        // 得到关键字
                        SelectionKey selKey = it.next();
                        // 删除已经处理的关键字
                        it.remove();
                        // 处理事件
                        for (int index = 0; index <= 2; index++) {
                            if (cServer[index].getKey() == selKey) {
                                // 确认连接正常
                                if (selKey.isValid() && selKey.isConnectable()) {
                                    finishChannel(index);
                                }
                                //读取信道信息
                                if (selKey.isValid() && selKey.isReadable()) {
                                    Processer(index);
                                }
                            }
                        }
                    }
                }
            }
        };
        MainFrm.cstp.putNewThread(run);
    }

    /**
     * 消息处理器
     *
     * @param hostIndex 服务器索引
     */
    private void Processer(int hostIndex) {

        ByteBuffer bbInt = ByteBuffer.allocate(IntLength);    //读取INT头信息的缓存池
        ByteBuffer bbObj = ByteBuffer.allocate(MemCache);     //读取OBJ有效数据的缓存池
        SocketChannel channel = cServer[hostIndex].getChannel();
        ByteArrayInputStream bIn;
        ObjectInputStream in;
        CBaseDataBean cbdb;
        //有效数据长度
        int ObjLength;
        //从NIO信道中读出的数据长度
        int readObj;
        try {
            //读出INT数据头
            while (channel.read(bbInt) == IntLength) {
                //获取INT头中标示的有效数据长度信息并清空INT缓存池
                ObjLength = bbInt.getInt(0);
                bbInt.clear();

                //清空有效数据缓存池设置有效缓存池的大小
                bbObj.clear();
                bbObj.limit(ObjLength);

                //循环读满缓存池以保证数据完整性
                readObj = channel.read(bbObj);
                while (readObj != ObjLength) {
                    readObj += channel.read(bbObj);
                }

                //把有效数据缓存池中的数据转成对象数据
                bIn = new ByteArrayInputStream(bbObj.array());
                in = new ObjectInputStream(bIn);
                cbdb = (CBaseDataBean) in.readObject();
                switch (cbdb.getDataType()) {
                    case CMsgTypeBean.MSG_TYPE_SERVERSTATE:
                        rcv_msg_serverstatue(hostIndex, cbdb);
                        break;
                    case CMsgTypeBean.MSG_TYPE_COMMAND:
                        rcv_msg_command(hostIndex, cbdb);
                        break;
                }
                in.close();

            }
        } catch (ClassNotFoundException | IOException ex) {
        }
    }

    /**
     * 完成NIO信道连接
     *
     * @param hostIndex 连接的主机索引
     */
    private void finishChannel(int hostIndex) {
        SelectionKey selKey = cServer[hostIndex].getKey();
        SocketChannel channel = cServer[hostIndex].getChannel();
        try {
            // 是否连接完毕？
            cServer[hostIndex].setConnection(channel.finishConnect());
            if (!cServer[hostIndex].isConnection()) {
                // 异常
                selKey.cancel();
                MainFrm.cmf.actionLogMsg("控制信息:与" + hostIndex + "号主机建立NIO信道出现异常");
            } else {
                //开启队列信道传递线程
                sendIntoChannel(hostIndex);
                MainFrm.cmf.actionLogMsg("控制信息:与" + hostIndex + "号主机成功建立NIO信道");
            }
        } catch (IOException ex) {
            selKey.cancel();
            cServer[hostIndex].setConnection(false);
            MainFrm.cmf.actionLogMsg("控制信息:与" + hostIndex + "号主机建立NIO信道出现异常");
        }
        //更新界面连接显示信息
        MainFrm.cmf.actionLinkStatue(true, hostIndex);
    }

    /**
     * 循环检测消息队列，并把消息队列信息推送到NIO信道
     *
     * @param hostIndex 检测线程所服务的主机索引
     */
    private void sendIntoChannel(final int hostIndex) {

        Runnable run = new Runnable() {
            @Override
            public void run() {

                try {
                    ByteArrayOutputStream bOut;
                    ObjectOutputStream out;
                    CBaseDataBean cbdb;
                    ByteBuffer bb = ByteBuffer.allocate(MemCache);
                    while (true) {
                        cbdb = MainFrm.cdsq.read(hostIndex);//Blocking Method

                        bOut = new ByteArrayOutputStream();
                        out = new ObjectOutputStream(bOut);
                        out.writeObject(cbdb);
                        out.flush();

                        //构造发送数据:整型数据头+有效数据段
                        byte[] arr = bOut.toByteArray();
                        final int ObjLength = arr.length; //获取有效数据段长度                       
                        bb.clear();
                        bb.limit(IntLength + ObjLength);  //调整缓存池大小
                        bb.putInt(ObjLength);
                        bb.put(arr);
                        bb.position(0);                   //调整重置读写指针

                        SocketChannel channel = (SocketChannel) cServer[hostIndex].getChannel();
                        channel.write(bb);

                        out.close();
                        bOut.close();

                        //如果推送的是断开连接信息，进行特殊处理
                        if (cbdb.getDataType() == CMsgTypeBean.MSG_TYPE_CUTDOWN) {
                            channel.close();
                            cServer[hostIndex].getKey().cancel();
                            cServer[hostIndex].setKey(null);
                            MainFrm.cmf.actionStatueClear(hostIndex);
                            MainFrm.cmf.actionLogMsg("控制信息:已与" + hostIndex + "号主机的连接已经断开");
                            break;
                        }
                    }
                } catch (IOException ex) {
                }
            }
        };
        MainFrm.cstp.putNewThread(run);
    }

    /**
     * 获取服务器状态信息
     *
     * @param hostIndex 服务器索引
     * @param msg 状态消息
     */
    private void rcv_msg_serverstatue(int hostIndex, CBaseDataBean msg) {
        CServerRuntime statue = (CServerRuntime) msg;
        int cpu = (int) statue.getCpuRatio();
        int JvmMemUsed = (int) (statue.getTotalMemory() / 1024);
        int JvmMemTotal = (int) (statue.getMaxMemory() / 1024);
        int MacMemUsed = (int) (statue.getUsedMemory() / 1024);
        int MacMemTotal = (int) (statue.getTotalMemorySize() / 1024);
        int Thread = statue.getTotalThread();
        MainFrm.cmf.actionMacCpuUsed(hostIndex, cpu);
        MainFrm.cmf.actionJvmMemUsed(hostIndex, JvmMemUsed, JvmMemTotal);
        MainFrm.cmf.actionMacMemUsed(hostIndex, MacMemUsed, MacMemTotal);
        MainFrm.cmf.actionThread(hostIndex, Thread, 300);
    }

    /**
     * 处理命令消息信息
     *
     * @param hostIndex 接收命令的主机索引
     * @param msg 命令消息的数据
     */
    private void rcv_msg_command(int hostIndex, CBaseDataBean msg) {
        CMsgCmdBean cmcb = (CMsgCmdBean) msg;
        if (cmcb.getCmd().equals("File_Verify_Correct")) {
            MainFrm.cmf.actionVerifyFileResult(hostIndex, true);
        }
        if (cmcb.getCmd().equals("File_Verify_Fail")) {
            MainFrm.cmf.actionVerifyFileResult(hostIndex, false);
        }
        if(cmcb.getCmd().equals("Run_File_Success")){
            MainFrm.cmf.actionRunFileResult(hostIndex, true);
        }
        if(cmcb.getCmd().equals("Run_File_Fail")){
            MainFrm.cmf.actionRunFileResult(hostIndex, false);
        }
    }

    /**
     * 发送消息到消息队列
     *
     * @param hostIndex 发送的目标主机的索引
     * @param cmd 发送的命令
     */
    public void send_msg_command(int hostIndex, String cmd) {
        CMsgCmdBean cmcb = new CMsgCmdBean();
        cmcb.setCmd(cmd);
        MainFrm.cdsq.enter(hostIndex, cmcb);
    }

    /**
     * 发送校验信息到消息队列
     *
     * @param hostIndex 目标服务器的索引
     */
    public void send_msg_verifyFile(int hostIndex) {
        CMsgVerifyFileBean cmvfb = new CMsgVerifyFileBean();
        cmvfb.setName(MainFrm.cafp.getProFile().getFileName());
        cmvfb.setPath(MainFrm.cafp.getServerSavePath());
        cmvfb.setSize(MainFrm.cafp.getProFile().getFileSize());
        MainFrm.cdsq.enter(hostIndex, cmvfb);
    }
    
    /**
     * 向服务器发送执行任务的消息
     * @param cmdt 执行的任务量和相关信息数据包,内包含着执行主机的index
     */
    public void send_msg_DispatchTask(CMsgDispatchTask cmdt) {
        int hostIndex = cmdt.getHostIndex();
        MainFrm.cdsq.enter(hostIndex, cmdt);
    }

    /**
     * 开辟新线程向消息队列发送任务程序数据消息
     *
     * @param hostIndex 发送的目标主机的索引
     */
    public void send_msg_sendFileData(final int hostIndex) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                String basefilepath = MainFrm.cafp.getProFile().getFilePath();
                String filepath = MainFrm.cafp.getServerSavePath();
                String filename = MainFrm.cafp.getProFile().getFileName();
                long filesize = MainFrm.cafp.getProFile().getFileSize();
                String filetype = MainFrm.cafp.getProFile().getFileType();

                //已发送文件长度
                long send = 0;
                //单次发送文件长度
                int read;
                //发送进度百分比
                int percent = 0;
                MainFrm.cmf.actionSendFileValue(percent);
                File sendfile = new File(basefilepath);
                if (!sendfile.exists()) {
                    MainFrm.cmf.actionLogMsg("控制信息:要发送的文件不存在--" + filename);
                    return;
                }
                try {
                    FileInputStream fis = new FileInputStream(sendfile);
                    CMsgSendFileBean cmsfb;
                    MainFrm.cmf.actionLogMsg("控制信息:开始发送程序文件--" + filename);

                    while (true) {
                        //拆分构造文件数据包
                        cmsfb = new CMsgSendFileBean();
                        cmsfb.setName(filename);
                        cmsfb.setPath(filepath);
                        cmsfb.setType(filetype);
                        cmsfb.setSize(filesize);
                        byte bt[] = new byte[cmsfb.getDataLength()];
                        read = fis.read(bt);
                        cmsfb.setData(bt);
                        send += read;

                        //设置文件结束数据包标志
                        if (send == filesize) {
                            cmsfb.setEndReadSize(read);
                            cmsfb.setIsEnd(true);
                        }

                        //更新发送进度
                        percent = (int) (send * 100 / filesize);
                        MainFrm.cmf.actionSendFileValue(percent);
                        MainFrm.cdsq.enter(hostIndex, cmsfb);

                        //写入发送结束日志
                        if (send == filesize) {
                            MainFrm.cmf.actionLogMsg("控制信息:发送文件完成--" + filename);
                            break;
                        } else {
                            Thread.sleep(10);
                        }
                    }
                    fis.close();
                } catch (IOException | InterruptedException ex) {
                    MainFrm.cmf.actionLogMsg("系统信息:发送文件过程出现异常,进程被迫中断");
                }
            }
        };
        MainFrm.cstp.putNewThread(run);
    }

    /**
     * 发送断开连接消息
     *
     * @param hostIndex 所需断开连接的主机索引
     */
    public void send_msg_cutdown(int hostIndex) {
        CBaseDataBean cbdb = new CBaseDataBean();
        cbdb.setDataType(CMsgTypeBean.MSG_TYPE_CUTDOWN);
        MainFrm.cdsq.enter(hostIndex, cbdb);
    }
}
