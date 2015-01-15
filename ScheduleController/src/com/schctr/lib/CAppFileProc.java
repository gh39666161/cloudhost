/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.schctr.lib;

import com.tools.bean.CFileChosedBean;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author 周明
 */
public class CAppFileProc {

    /**
     * 选择的可执行文件
     */
    private CFileChosedBean ProFile;
    /**
     * 选择的XML文件
     */
    private CFileChosedBean XmlFile;
    /**
     * 服务器接收文件路径
     */
    private String ServerSavePath;
    /**
     * 系统配置文件
     */
    private String AppConfigFile;

    public CAppFileProc() {
        ProFile = new CFileChosedBean();
        XmlFile = new CFileChosedBean();
        XmlFile.setFileType("xml");
        AppConfigFile = "AppConfig.xml";
    }

    /**
     * 配置程序文件
     *
     * @param path 路径
     * @param name 文件名
     * @param type 文件类型 例如:exe sh等
     * @param size 文件大小
     */
    public void cfgProcFile(String path, String name, String type, long size) {
        this.ProFile.setFilePath(path);
        this.ProFile.setFileName(name);
        this.ProFile.setFileSize(size);
        this.ProFile.setFileType(type);
    }

    /**
     * 配置配置文件
     *
     * @param path 路径
     * @param name 文件名
     * @param size 文件大小
     */
    public void cfgXmlFile(String path, String name, long size) {
        this.XmlFile.setFilePath(path);
        this.XmlFile.setFileName(name);
        this.XmlFile.setFileSize(size);
    }

    /**
     * 获取xml中运行时间间隔
     * @return 返回时间间隔，错误时返回-1
     */
    public int getRunModelTime() {
        int time = -1;
        File f = new File(this.XmlFile.getFilePath());
        if (!f.exists()) {
            return -1;
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = (Document) builder.parse(f);
            Element root = doc.getDocumentElement();
            if (root == null) {
                return -1;
            }
            //获取一级配置------WorkLoad
            NodeList nl = root.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node nd = nl.item(i);
                //获取二级配置--TimeCell
                if (nd.getNodeType() == Node.ELEMENT_NODE && nd.getNodeName().equals("TimeCell")) {
                    time = Integer.parseInt(nd.getFirstChild().getNodeValue());
                    if (time <= 0) {
                        time = -1;
                    }
                    break;
                }
            }
            return time;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            return -1;
        }
    }
    
    /**
     * 获取xml中运行时间单位
     * @return 返回时间单位,否则返回error
     */
    public String getRunModelTimeUnit(){
        String unit = "error";
        File f = new File(this.XmlFile.getFilePath());
        if (!f.exists()) {
            return "error";
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = (Document) builder.parse(f);
            Element root = doc.getDocumentElement();
            if (root == null) {
                return "error";
            }
            //获取一级配置------WorkLoad
            NodeList nl = root.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node nd = nl.item(i);
                //获取二级配置--TimeUnit
                if (nd.getNodeType() == Node.ELEMENT_NODE && nd.getNodeName().equals("TimeUnit")) {
                    unit = nd.getFirstChild().getNodeValue();
                    switch (unit) {
                        case "hour":
                            unit="h";
                            break;
                        case "minute":
                            unit="m";
                            break;
                        case "second":
                            unit="s";
                            break;
                        default:
                            unit="error";
                    }
                    break;
                }
            }
            return unit;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            return "error";
        }
    }

    /**
     * 获取xml中的运行节点数据
     *
     * @return 返回数据链表
     */
    public ArrayList getRunModelDataList() {
        //创建返回数据链表
        ArrayList<Point> rst = new ArrayList();
        int index = 0;
        File f = new File(this.XmlFile.getFilePath());
        if (!f.exists()) {
            return null;
        }
        /**
         * 创建读取xml所需对象
         */
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = (Document) builder.parse(f);
            Element root = doc.getDocumentElement();
            if (root == null) {
                return null;
            }
            //获取一级配置-------------WorkLoad
            NodeList nl = root.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node nd = nl.item(i);
                //获取二级配置---------DataPoint
                if (nd.getNodeType() == Node.ELEMENT_NODE && nd.getNodeName().equals("DataPoint")) {
                    //获取三级配置-----PointIndex
                    NodeList data = nd.getChildNodes();
                    for (int j = 0; j < data.getLength(); j++) {
                        Node dtItem = data.item(j);
                        if (dtItem.getNodeType() == Node.ELEMENT_NODE && dtItem.getNodeName().equals("Point" + Integer.toString(index))) {
                            //获取三级配置中的数据并转换Int
                            int task = Integer.parseInt(dtItem.getFirstChild().getNodeValue());
                            //添加到链表
                            rst.add(new Point(index, task));
                            index++;
                        }
                    }
                    break;
                }
            }
            return rst;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            return null;
        }
    }

    /**
     * 加载应用程序系统配置
     *
     * @return 返回加载是否成功
     */
    public boolean loadAppConfig() {
        File f = new File(AppConfigFile);
        if (!f.exists()) {
            return false;
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = (Document) builder.parse(f);
            Element root = doc.getDocumentElement();
            if (root == null) {
                return false;
            }
            NodeList nl = root.getChildNodes();
            int len = nl.getLength();
            for (int i = 0; i < len; i++) {
                Node nd = nl.item(i);
                if (nd.getNodeType() == Node.ELEMENT_NODE && nd.getNodeName().equals("ProcSavePath")) {
                    ServerSavePath = nd.getFirstChild().getNodeValue();
                    break;
                }
            }
            return true;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            return false;
        }
    }

    public String getServerSavePath() {
        return ServerSavePath;
    }

    public void setServerSavePath(String ServerSavePath) {
        this.ServerSavePath = ServerSavePath;
    }

    public CFileChosedBean getProFile() {
        return ProFile;
    }

    public void setProFile(CFileChosedBean ProFile) {
        this.ProFile = ProFile;
    }

    public CFileChosedBean getXmlFile() {
        return XmlFile;
    }

    public void setXmlFile(CFileChosedBean XmlFile) {
        this.XmlFile = XmlFile;
    }

    public boolean isProcFileExist() {
        return ProFile.getFileName() != null && ServerSavePath != null;
    }

}
