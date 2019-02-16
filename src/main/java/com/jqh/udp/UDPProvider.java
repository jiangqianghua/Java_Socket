package com.jqh.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @Author: jiang qiang hua
 * @Description:<h1>内容提供方，需要监听端口，回送消息</h1>
 * @Date: Create in 20:23 2019/1/27
 * @Modified By:
 **/
public class UDPProvider {

    public static void main(String[] args) throws IOException {

        System.out.println("UDPProvider started...");

        // 设定监听端口
        DatagramSocket ds = new DatagramSocket(2000);

        // 构建接受实体
        final byte[] buf = new byte[512];
        DatagramPacket receivePack = new DatagramPacket(buf,buf.length);

        // 开始接受
        ds.receive(receivePack);

        String ip = receivePack.getAddress().getHostAddress();
        int port = receivePack.getPort();
        int dataLen = receivePack.getLength();
        String data = new String(receivePack.getData(),0,dataLen);
        System.out.println("UDPProvider receiver from ip:"+ ip+ " port:"+port+" data:"+data);

        // 构建回送数据
        String responseData = "Recevie data with len:"+dataLen;
        byte[] responseDataBytes = responseData.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(responseDataBytes,
                                                            responseDataBytes.length,
                receivePack.getAddress(),
                receivePack.getPort());

        // 开始发送数据
        ds.send(responsePacket);

        System.out.println("UDPProvider Finished");
        ds.close();



    }
}
