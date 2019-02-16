package com.jqh.tcp;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

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
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        //socket输出流。并转成打印流
        OutputStream outputStream = client.getOutputStream();
        PrintStream socketPrintStream = new PrintStream(outputStream);

        // socket输入流
        InputStream inputStream = client.getInputStream() ;
        BufferedReader socketBufferReader = new BufferedReader(new InputStreamReader(inputStream));
        boolean flag = true ;
        do {
            // 读取键盘
            String str = input.readLine();
            // 发送到服务器
            socketPrintStream.println(str);

            //  从服务器读取一行
            String echo = socketBufferReader.readLine();
            if ("bye".equals(echo)) {
                flag = false ;
            }else{
                System.out.println(echo);
            }
        }while (flag);

        socketBufferReader.close();
        socketPrintStream.close();
    }
}
