package demo01.client;

import com.jqh.udpboardcast.MessageCreator;
import com.jqh.udpboardcast.UDPSearcher;
import demo01.client.bean.ServerInfo;
import demo01.clink.net.qiujuer.clink.utils.ByteUtils;
import demo01.constants.UDPConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author: jiang qiang hua
 * @Description:<h1></h1>
 * @Date: Create in 12:54 2019/2/23
 * @Modified By:
 **/
public class ClientSearcher {

    private static final int LISTEN_PORT = UDPConstants.PORT_CLIENT_RESPONSE ;

    public static ServerInfo searchServer(int timeout){
        System.out.println("UDPSearcher started.");

        CountDownLatch receiveLatch = new CountDownLatch(1);
        Listener listener = null ;
        try{
            listener = listen(receiveLatch);
            sendBroadcast();
            // 会阻塞timeout秒
            receiveLatch.await(timeout, TimeUnit.MILLISECONDS);
        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("UDPSearcher Finished.");
        if(listener == null)
            return null ;

        List<ServerInfo> devices = listener.getDeviceAndClose();
        if(devices.size() > 0){
            return devices.get(0);
        }
        return null;
    }

    private static Listener listen(CountDownLatch receiveLatch) throws  InterruptedException{
        System.out.println("UDPSearcher start listen.");
        CountDownLatch startDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, startDownLatch, receiveLatch);
        listener.start();
        // 堵塞
        startDownLatch.await();
        return listener ;

    }

    /**
     * 开始发送广播
     * @throws IOException
     */
    private static void sendBroadcast() throws IOException {
        System.out.println("UDPSearcher sendBroadcast started...");
        DatagramSocket ds = new DatagramSocket();
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        byteBuffer.put(UDPConstants.HEADER);
        byteBuffer.putShort((short)1);
        byteBuffer.putInt(LISTEN_PORT);


        DatagramPacket responsePacket = new DatagramPacket(byteBuffer.array(),
                byteBuffer.position() + 1);
        // 发送广播
        responsePacket.setAddress(InetAddress.getByName("255.255.255.255"));
        responsePacket.setPort(UDPConstants.PORT_SERVER);

        // 开始发送数据
        ds.send(responsePacket);
        ds.close();
        System.out.println("UDPSearcher sendBroadcast finish...");


    }


    private static class Listener extends Thread{
        private final int listenerPort ;
        private final CountDownLatch startDownLatch ;
        private final CountDownLatch recevieDownLatch ;
        private final List<ServerInfo> serverInfoList = new ArrayList<>();
        private final byte[] buffer = new byte[128];
        private final int minLen = UDPConstants.HEADER.length + 2 + 4 ;
        private boolean done = false ;
        private DatagramSocket ds = null ;

        public Listener(int listenerPort,CountDownLatch startDownLatch,CountDownLatch recevieDownLatch ) {
            super();
            this.listenerPort = listenerPort ;
            this.startDownLatch = startDownLatch ;
            this.recevieDownLatch = recevieDownLatch ;
        }

        @Override
        public void run() {
            super.run();
            // 调用这句，会解决listen方法里面的startDownLatch.await();堵塞
            startDownLatch.countDown();

            try{
                ds = new DatagramSocket(listenerPort);
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);
                while (!done){
                    // 开始接受
                    ds.receive(receivePack);

                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int dataLen = receivePack.getLength();
                    byte[] data = receivePack.getData() ;
                    boolean isValid = dataLen >= minLen
                            && ByteUtils.startWith(data,UDPConstants.HEADER);
                    System.out.println("UDPProvider receiver from ip:" + ip + " port:" + port + " dataValid:" + isValid);

                    if(!isValid){
                        continue;
                    }

                    // 跳过前面的UDPConstants.HEADER.length字节开始包裹buffer
                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer,UDPConstants.HEADER.length,dataLen);
                    final short cmd = byteBuffer.getShort();
                    final int serverPort = byteBuffer.getInt();
                    if(cmd != 2 || serverPort <= 0){
                        System.out.println("UDPSearch receive cmd:"+cmd + "\tserverPort:"+serverPort);
                        continue;
                    }
                    String sn = new String(buffer,minLen,dataLen-minLen);
                    ServerInfo info = new ServerInfo(serverPort,ip, sn);
                    serverInfoList.add(info);
                    recevieDownLatch.countDown();
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

        List<ServerInfo> getDeviceAndClose(){
            done = true ;
            close();
            return serverInfoList ;
        }
    }
}
