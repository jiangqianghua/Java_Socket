package com.jqh.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @Author: jiang qiang hua
 * @Description:<h1>udp 内容搜索框</h1>
 * @Date: Create in 20:37 2019/1/27
 * @Modified By:
 **/
public class UDPSearcher {

    public static void main(String[] args) throws IOException {
        System.out.println("UDPSearcher started...");

        DatagramSocket ds = new DatagramSocket();

        // 构建发送数据
        // 构建回送数据
        String requestData = "hello";
        byte[] requestDataBytes = requestData.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(requestDataBytes,
                requestDataBytes.length);
        responsePacket.setAddress(InetAddress.getLocalHost());
        responsePacket.setPort(2000);

        // 开始发送数据
        ds.send(responsePacket);



        // 构建接受实体
        final byte[] buf = new byte[512];
        DatagramPacket receivePack = new DatagramPacket(buf,buf.length);

        // 开始接受
        ds.receive(receivePack);

        String ip = receivePack.getAddress().getHostAddress();
        int port = receivePack.getPort();
        int dataLen = receivePack.getLength();
        String data = new String(receivePack.getData(),0,dataLen);
        System.out.println("UDPSearcher receiver from ip:"+ ip+ " port:"+port+" data:"+data);

    }
}
