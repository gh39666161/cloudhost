/*
 * 云服务器网络连接库
 */
package com.server.lib;

import com.server.main.CloudServer;
import com.tools.bean.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Thread.sleep;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 *
 * @author 周明
 */
public class CNetWork {

    private Selector selector = null;
    private ServerSocketChannel SSC = null;
    private SelectionKey key = null;
    private InetSocketAddress hostIP = null;
    private boolean isInit = false;
    private boolean isConnected = false;
    private final int MemCache = 1024;
    private final int IntLength = 4;

    /**
     * 初始化云服务器网络数据
     */
    public void initCloudServer() {

        try {
            selector = Selector.open();

            hostIP = new InetSocketAddress(8453);

            SSC = ServerSocketChannel.open();
            SSC.configureBlocking(false);
            SSC.register(selector, SelectionKey.OP_ACCEPT);
            SSC.socket().bind(hostIP);

            System.out.println("服务器初始化完成:主机-" + hostIP.getHostName() + " IP-" + hostIP.getAddress().getHostAddress() + " 端口-" + hostIP.getPort());
            isInit = true;
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
    }

    /**
     * 开启云服务器的监听
     */
    public void startListen() {
        if (!isInit) {
            initCloudServer();
        }
        Dispatcher();
    }

    /**
     * 开辟线程分发消息
     */
    private void Dispatcher() {
        Runnable run = new Runnable() {

            @Override
            public void run() {
                try {
                    while (true) {
                        selector.selectNow();
                        Thread.sleep(100);
                        Iterator<SelectionKey> itor = selector.selectedKeys().iterator();
                        while (itor.hasNext()) {
                            SelectionKey selKey = itor.next();
                            itor.remove();

                            if (selKey.isValid() && selKey.isAcceptable()) {
                                finshAccept(selKey);
                            }

                            if (selKey.isValid() && selKey.isReadable()) {
                                //消息分发
                                Processer();
                            }
                        }
                    }
                } catch (IOException | InterruptedException ex) {
                    System.out.println(ex.toString());
                }
            }
        };
        CloudServer.cstp.putNewThread(run);
    }

    /**
     * 消息处理器
     */
    private void Processer() {
        ByteBuffer bbInt = ByteBuffer.allocate(IntLength);    //读取INT头信息的缓存池
        ByteBuffer bbObj = ByteBuffer.allocate(MemCache);     //读取OBJ有效数据的缓存池
        SocketChannel channel = (SocketChannel)key.channel();
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

                bIn = new ByteArrayInputStream(bbObj.array());
                in = new ObjectInputStream(bIn);
                cbdb = (CBaseDataBean) in.readObject();
                switch (cbdb.getDataType()) {
                    case CMsgTypeBean.MSG_TYPE_COMMAND:
                        rcv_msg_command(cbdb);
                        break;
                    case CMsgTypeBean.MSG_TYPE_CUTDOWN:
                        rcv_msg_cutdown();
                        break;
                    case CMsgTypeBean.MSG_TYPE_VERIFYFILE:
                        rcv_msg_verifyfile(cbdb);
                        break;
                    case CMsgTypeBean.MSG_TYPE_SENDFILE:
                        rcv_msg_sendfile(cbdb);
                        break;
                    case CMsgTypeBean.MSG_TYPE_DISPATCHTASK:
                        rcv_msg_dispchtask(cbdb);
                        break;
                }
                in.close();
            
            }
        } catch (ClassNotFoundException | IOException ex) {
        }
    }

    /**
     * 接收控制器连接
     *
     * @param selKey 携带连接消息的key
     */
    private void finshAccept(SelectionKey selKey) {
        ServerSocketChannel sscServer = (ServerSocketChannel) selKey.channel();
        SocketChannel channel;
        try {
            channel = sscServer.accept();

            channel.configureBlocking(false);
            key = channel.register(selector, channel.validOps());
            isConnected = true;
            System.out.println("From Schr-" + channel.socket().getInetAddress().getHostAddress() + ":" + channel.socket().getPort()+"::GetAcceptMsg");

            //开始发送服务器状态
            sendServerRuntime();
            //开启队列->信道传输线程
            sendIntoChannel();
        } catch (IOException ex) {
        }
    }

    /**
     * 处理命令类型消息
     *
     * @param cbdb 命令消息数据
     */
    private void rcv_msg_command(CBaseDataBean msg) {
        CMsgCmdBean cmcb = (CMsgCmdBean) msg;
        String cmd = cmcb.getCmd();
    }

    /**
     * 处理校验文件消息
     *
     * @param cbdb 校验消息数据
     */
    private void rcv_msg_verifyfile(CBaseDataBean msg) {
        System.out.println("From Processer::GetVerifyFileMsg");
        CMsgVerifyFileBean cmvfb = (CMsgVerifyFileBean) msg;
        String path = cmvfb.getPath();
        String name = cmvfb.getName();
        long size = cmvfb.getSize();
        File procfile = new File(path + name);

        String rtmsg;
        if (procfile.exists() && procfile.length() == size) {
            rtmsg = "File_Verify_Correct";
        } else {
            rtmsg = "File_Verify_Fail";
        }
        this.send_msg_command(rtmsg);
    }

    /**
     * 处理断开连接消息
     */
    private void rcv_msg_cutdown() {
        System.out.println("From Processer::GetCutdownMsg");
        this.isConnected = false;
        try {
            CBaseDataBean cbdb = new CBaseDataBean();
            cbdb.setDataType(CMsgTypeBean.MSG_TYPE_CUTDOWN);
            CloudServer.cdsq.enter(cbdb);

            key.channel().close();
            key.cancel();
            System.out.println("From System::Schr Already Closed");
        } catch (IOException ex) {
        }
    }
    
    private void rcv_msg_dispchtask(CBaseDataBean cbdb){
        System.out.println("From Processer::GetDispatchTaskMsg");
        CMsgDispatchTask cmdt = (CMsgDispatchTask)cbdb;
        CloudServer.cddt.dotask(cmdt);
    }

    /**
     * 向控制器发送命令消息
     *
     * @param cmd 所发送的命令信息
     */
    public void send_msg_command(String cmd) {
        CMsgCmdBean cmcb = new CMsgCmdBean();
        cmcb.setCmd(cmd);
        CloudServer.cdsq.enter(cmcb);
        System.out.println("Send Command To Schr::" + cmd);
    }

    /**
     * 接收数据文件消息
     *
     * @param msg 包含文件数据的消息
     */
    private void rcv_msg_sendfile(CBaseDataBean msg) {
        CMsgSendFileBean cmsfb = (CMsgSendFileBean) msg;
        CloudServer.cafp.save(cmsfb);
    }

    /**
     * 获取服务器信息并发送到消息队列
     */
    private void sendServerRuntime() {

        Runnable run = new Runnable() {
            @Override
            public void run() {

                try {
                    CJvmService service = new CJvmService();
                    CServerRuntime csr;
                    while (true) {
                        if (!isConnected) {
                            break;
                        }
                        csr = service.getCServerRuntime();
                        CloudServer.cdsq.enter(csr);
                        sleep(1000);
                    }
                } catch (Exception ex) {
                }
            }
        };

        CloudServer.cstp.putNewThread(run);
    }

    /**
     * 循环检测消息队列，并把消息队列信息推送到NIO信道
     */
    private void sendIntoChannel() {

        Runnable run = new Runnable() {
            @Override
            public void run() {

                try {
                    ByteArrayOutputStream bOut;
                    ObjectOutputStream out;
                    CBaseDataBean cbdb;
                    ByteBuffer bb = ByteBuffer.allocate(MemCache);
                    while (true) {
                        cbdb = CloudServer.cdsq.read();//Blocking Method

                        //处理自我命令:断开连接 退出线程
                        if (cbdb.getDataType() == CMsgTypeBean.MSG_TYPE_CUTDOWN) {
                            break;
                        }

                        bOut = new ByteArrayOutputStream();
                        out = new ObjectOutputStream(bOut);
                        out.writeObject(cbdb);
                        out.flush();
                        
                        //构造发送数据:整型数据头+有效数据段
                        byte[] arr = bOut.toByteArray();
                        final int ObjLength = arr.length;   //获取有效数据段长度                      
                        bb.clear();
                        bb.limit(IntLength + ObjLength);    //调整缓存池大小
                        bb.putInt(ObjLength);
                        bb.put(arr);
                        bb.position(0);                     //调整重置读写指针

                        SocketChannel channel = (SocketChannel) key.channel();
                        channel.write(bb);

                        out.close();
                        bOut.close();
                    }
                } catch (IOException ex) {
                }
            }
        };
        CloudServer.cstp.putNewThread(run);
    }
}
