package com.jqh.socketsimple;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @Author: jiang qiang hua
 * @Description:<h1></h1>
 * @Date: Create in 23:36 2019/1/26
 * @Modified By:
 **/
public class SocketClient {

    public static void main(String[] args)throws IOException {
        Socket socket = new Socket();
        // 设置io超时时间
        socket.setSoTimeout(3000);

        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(),2000),3000);

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
