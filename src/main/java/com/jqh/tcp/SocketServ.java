package com.jqh.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author: jiang qiang hua
 * @Description:<h1></h1>
 * @Date: Create in 23:36 2019/1/26
 * @Modified By:
 **/
public class SocketServ {

    private static final int PORT = 20000;
    public static void main(String[] args) throws IOException {

        ServerSocket server = createServerSocket();
        initServerSocket(server);

        System.out.println("服务器准备就绪...");
        System.out.println("服务器信息："+server.getInetAddress() + ":"+server.getLocalPort());

        for(;;){
            //接受客户端
            Socket client = server.accept();
            System.out.println("新客户端接入:"+client.getInetAddress() + ":" + client.getPort());
            //构建异步线程
            ClientHandler clientHandler = new ClientHandler(client);
            //启动线程
            clientHandler.start();
        }

    }

    private static ServerSocket createServerSocket() throws IOException{
        ServerSocket server = new ServerSocket();
        server.bind(new InetSocketAddress(Inet4Address.getLocalHost(),PORT),50);
        return server ;
    }

    private static void initServerSocket(ServerSocket server) throws IOException{
        server.setReuseAddress(true);
        server.setReceiveBufferSize(64*1024*1024);
       // server.setSoTimeout(2000);  // accept 等待超时时间
        server.setPerformancePreferences(1,1,1);
    }


    public static class ClientHandler extends Thread{
        private Socket socket ;

        ClientHandler(Socket socket){
            this.socket = socket ;
        }

        @Override
        public void run() {
            super.run();
            try {
                // 获取打印的输出流
                PrintStream socketOutput = new PrintStream(socket.getOutputStream());
                // 获取输入流
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                boolean flag = true ;
                do{
                    String str = socketInput.readLine();
                    if("bye".equals(str)){
                        flag = false ;
                        socketOutput.println("bye");
                    }else{
                        System.out.println("接受客户端请求数据："+str);
                        socketOutput.println("回送:"+str.length());
                    }
                }while (flag);

                socketInput.close();
                socketOutput.close();
            }catch (Exception e){
                System.out.println("连接异常断开...");
            }finally {
                try {
                    socket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            System.out.println("客户端已退出:"+socket.getInetAddress() + ":" + socket.getPort());
        }
    }
}
