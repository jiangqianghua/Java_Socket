package com.jqh.udpboardcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

/**
 * @Author: jiang qiang hua
 * @Description:<h1>内容提供方，需要监听端口，回送消息,实现局域网搜索</h1>
 * @Date: Create in 20:23 2019/1/27
 * @Modified By:
 **/
public class UDPProvider {

    public static void main(String[] args) throws IOException {

        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        provider.start();;

        System.in.read();

        provider.exit();



    }

    private static class Provider extends Thread{
        private final String sn ;
        private boolean done = false ;
        private DatagramSocket ds = null ;

        public Provider(String sn){
            super();
            this.sn = sn ;
        }

        @Override
        public void run() {
            super.run();

            System.out.println("UDPProvider started...");
            try {
                // 设定监听端口
                ds = new DatagramSocket(2000);
                while (!done) {
                    // 构建接受实体
                    final byte[] buf = new byte[512];
                    DatagramPacket receivePack = new DatagramPacket(buf, buf.length);

                    // 开始接受
                    ds.receive(receivePack);

                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int dataLen = receivePack.getLength();
                    String data = new String(receivePack.getData(), 0, dataLen);
                    System.out.println("UDPProvider receiver from ip:" + ip + " port:" + port + " data:" + data);

                    int responsePort =  MessageCreator.parsePort(data);
                    if(responsePort != -1) {
                        // 构建回送数据
                        String responseData = MessageCreator.buildWithSn(sn);
                        byte[] responseDataBytes = responseData.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(responseDataBytes,
                                responseDataBytes.length,
                                receivePack.getAddress(),
                                responsePort);

                        // 开始发送数据
                        ds.send(responsePacket);
                    }
                }
            }catch (Exception e){

            }finally {
                close();
            }

            System.out.println("UDPProvider Finished");

        }

        private void close(){
            if(ds != null){
                ds.close();
                ds = null ;
            }
        }

        private void exit(){
           done = true ;
           close();
        }
    }
}
