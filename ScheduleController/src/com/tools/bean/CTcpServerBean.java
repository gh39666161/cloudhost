/*
 * 服务器对象类
 */
package com.tools.bean;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 *
 * @author Administrator
 */
public class CTcpServerBean {

    private SelectionKey key;

    /**
     * 服务器的连接标记
     */
    private boolean connection;
    
    public CTcpServerBean() {
        this.key = null;
        this.connection = false;
    }

    public SelectionKey getKey() {
        return key;
    }

    public void setKey(SelectionKey key) {
        this.key = key;
    }

    public boolean isConnection() {
        return connection;
    }

    public void setConnection(boolean connection) {
        this.connection = connection;
    }
    
    public SocketChannel getChannel(){
        SocketChannel channel = (SocketChannel)key.channel();
        return channel;
    }
    public void createChannel(Selector selector, String hostIp, String hostPort) {
        try {
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            this.key = channel.register(selector, channel.validOps());
            channel.connect(new InetSocketAddress(hostIp, Integer.parseInt(hostPort)));
        } catch (IOException ex) {
        }
    }
}
