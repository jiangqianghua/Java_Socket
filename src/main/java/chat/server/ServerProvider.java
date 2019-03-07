package chat.server;

import chat.constants.UDPConstants;
import chat.utils.ByteUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @Author: jiang qiang hua
 * @Description:<h1>服务器提供者，提供tcp的端口</h1>
 * @Date: Create in 9:44 2019/2/23
 * @Modified By:
 **/
public class ServerProvider {

    private static Provider PROVIDER_INSTANCE ;

    static void start(int port){
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn,port);
        provider.start();
        PROVIDER_INSTANCE = provider ;
    }

    static void stop(){
        if(PROVIDER_INSTANCE != null){
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null ;
        }
    }

    private static class Provider extends Thread{
        private final byte[] sn ;
        private final int port ;
        private boolean done = false ;
        private DatagramSocket ds = null ;

        final byte[] buffer = new byte[128];

        Provider(String sn , int port){
            super();
            this.sn = sn.getBytes();
            this.port = port ;
        }
        @Override
        public void run() {
            super.run();

            System.out.println("UDPProvider started.");
            try{

                ds = new DatagramSocket(UDPConstants.PORT_SERVER);
                DatagramPacket receivePack = new DatagramPacket(buffer,buffer.length);

                while (!done){
                    // 接受
                    ds.receive(receivePack);
                    String  clientIp = receivePack.getAddress().getHostAddress();
                    int clientPort = receivePack.getPort();
                    int clientDataLen = receivePack.getLength();
                    byte[] clientData = receivePack.getData();
                    // 2  表示口令长度，short
                    // 4  表示回调端口的长度， int
                    boolean isValid = clientDataLen > (UDPConstants.HEADER.length + 2 + 4)
                            && ByteUtils.startWith(clientData, UDPConstants.HEADER);
                    System.out.println("ServerProvider receive from ip:"+clientIp + " \tport:"+clientPort + " \tdataValid:"+isValid);

                    int index = UDPConstants.HEADER.length ;
                    short cmd = (short)(clientData[index++]<<8 | (clientData[index++] & 0xff));
                    int responsePort = (((clientData[index++]) << 24) |
                            ((clientData[index++] & 0xff) << 16) |
                            ((clientData[index++] & 0xff) << 8) |
                            ((clientData[index] & 0xff)));

                    if(cmd == 1 && responsePort > 0){
                        // 构建回送数据
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putShort((short)2);
                        // 回送tcp的端口号
                        byteBuffer.putInt(port);
                        byteBuffer.put(sn);
                        int len = byteBuffer.position();

                        DatagramPacket responsePacket = new DatagramPacket(buffer,
                                len,
                                receivePack.getAddress(),
                                responsePort);

                        ds.send(responsePacket);
                        System.out.println("ServerProvider response to:"+clientIp + " \tport:"+responsePort+"\tdataLen:"+len);
                    }else{
                        System.out.println("ServerProvider receive cmd nonsupport; cmd:"+cmd+"\t port:"+responsePort);
                    }

                }
            }catch (Exception e){

            }finally {
                close();
            }
        }

        private void close(){
            if(ds != null){
                ds.close();
                ds = null ;
            }
        }

        public void exit(){
            done = false ;
            close();
        }
    }
}
