package com.jqh.udpboardcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: jiang qiang hua
 * @Description:<h1>udp 内容搜索框，实现局域网搜索</h1>
 * @Date: Create in 20:37 2019/1/27
 * @Modified By:
 **/
public class UDPSearcher {

    private static final int LISTEN_PORT = 3001;

    public static void main(String[] args) throws IOException,InterruptedException {

        System.out.println("UDPSearcher started...");
        Listener listener = listen();
        sendBroadcast();

        System.in.read();
        System.out.println("UDPSearcher Finished...");

        List<Device> devices = listener.getDeviceAndClose();

        for(Device device:devices){
            System.out.println("Device : " +device.toString());
        }

    }

    private static Listener listen() throws InterruptedException{
        System.out.println("UDPSearcher started listen...");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT,countDownLatch);
        listener.start();

        countDownLatch.await();
        return listener ;
    }

    private static void sendBroadcast() throws IOException{
        System.out.println("UDPSearcher sendBroadcast started...");
        DatagramSocket ds = new DatagramSocket();

        // 构建发送数据
        // 构建回送数据
        String requestData = MessageCreator.buildWithPort(LISTEN_PORT);
        byte[] requestDataBytes = requestData.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(requestDataBytes,
                requestDataBytes.length);
        // 发送广播
        responsePacket.setAddress(InetAddress.getByName("255.255.255.255"));
        responsePacket.setPort(2000);

        // 开始发送数据
        ds.send(responsePacket);
        ds.close();
        System.out.println("UDPSearcher sendBroadcast finish...");


    }

    private static class Device{
        final int port ;
        final String ip ;
        final String sn ;

        public Device(int port, String ip, String sn) {
            this.port = port;
            this.ip = ip;
            this.sn = sn;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "port=" + port +
                    ", ip='" + ip + '\'' +
                    ", sn='" + sn + '\'' +
                    '}';
        }
    }

    private static class Listener extends Thread{

        private final int listenerPort ;

        private final CountDownLatch countDownLatch ;

        private final List<Device> devices = new ArrayList<>();

        private boolean done = false ;

        private DatagramSocket ds = null ;

        public Listener(int listenerPort,CountDownLatch countDownLatch) {
            super();
            this.listenerPort = listenerPort ;
            this.countDownLatch = countDownLatch ;
        }

        @Override
        public void run() {
            super.run();
            countDownLatch.countDown();

            try{
                ds = new DatagramSocket(listenerPort);
                while (!done){
                    final byte[] buf = new byte[512];
                    DatagramPacket receivePack = new DatagramPacket(buf, buf.length);

                    // 开始接受
                    ds.receive(receivePack);

                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int dataLen = receivePack.getLength();
                    String data = new String(receivePack.getData(), 0, dataLen);
                    System.out.println("UDPProvider receiver from ip:" + ip + " port:" + port + " data:" + data);

                    String sn = MessageCreator.parseSn(data);
                    if(sn != null){
                        Device device = new Device(port,ip,sn);
                        devices.add(device);
                    }
                }
            }catch (Exception e){
               // e.printStackTrace();
            }finally {
                close();
            }
            System.out.println("UDPSearcher listener finish...");
        }

        private void close(){
            if(ds != null)
            {
                ds.close();
                ds = null ;
            }
        }

        List<Device> getDeviceAndClose(){
            done = true ;
            close();
            return devices ;
        }
    }
}
