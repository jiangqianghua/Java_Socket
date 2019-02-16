package com.jqh.tcp1;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * @Author: jiang qiang hua
 * @Description:<h1></h1>
 * @Date: Create in 23:36 2019/1/26
 * @Modified By:
 **/
public class SocketClient {

    private static final int PORT = 20000;
    private static final int LOCAL_PORT = 20001 ;

    public static void main(String[] args)throws Exception {
        Socket socket = createSocket();
        initSocket(socket);


        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(),PORT),3000);

        System.out.println("客户端准备就绪");
        System.out.println("客户端信息："+socket.getLocalAddress() + ":"+socket.getLocalPort());
        System.out.println("服务器端信息:" + socket.getInetAddress() + ":"+socket.getPort());

        try {
            todo(socket);
        }catch (Exception e){
            System.out.println("异常断开:"+ e.getMessage());
        }
        // 关闭socket
        socket.close();
    }

    private static Socket createSocket() throws Exception{
        Socket socket =  new Socket();
        socket.bind(new InetSocketAddress(Inet4Address.getLocalHost(),LOCAL_PORT));// 本地绑定指定的端口
        return socket ;
    }

    private static void initSocket(Socket socket) throws SocketException {
        // 设置读取超时时间
        socket.setSoTimeout(3000);
        // 设置是否复用未完全关闭的socket地址，对于指定bind操作后的套接字有效
        socket.setReuseAddress(true);

        //是否开启Nagle算法
        socket.setTcpNoDelay(true);

        //是否需要在长时间无数据响应发送确认数据，类似心跳包，时间大约两小时
        socket.setKeepAlive(true);

        socket.setSoLinger(true,20);

        socket.setOOBInline(true);

        socket.setReceiveBufferSize(64*1024*1024);
        socket.setSendBufferSize(64*1024*1024);
        socket.setPerformancePreferences(1,1,1);

    }

    private static void todo(Socket client) throws IOException{

        // 键盘输入流

        OutputStream outputStream = client.getOutputStream();

        InputStream inputStream = client.getInputStream() ;

        byte[] buffer = new byte[256];

       // ByteBuffer byteBuffer = ByteBuffer.allocate(256);
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

        byte be = 1;
        byteBuffer.put(be);

        char c = 'a';
        byteBuffer.putChar(c);

        int i = 123;
        byteBuffer.putInt(i);

        boolean b = true ;
        byteBuffer.put(b?(byte)1:(byte)0);

        long l = 234566778;
        byteBuffer.putLong(l);

        float f = 2.4566f;
        byteBuffer.putFloat(f);

        double d = 3.4567894343334545555566;
        byteBuffer.putDouble(d);


        String s = "hello江强华";
        byteBuffer.put(s.getBytes());

        // 发送到服务器
        outputStream.write(buffer,0,byteBuffer.position()+1);

        int read = inputStream.read(buffer);
        if (read > 0) {
            System.out.println("收到数量:"+read );
        }else{
            System.out.println("收到数量:"+read);
        }

    }
}
