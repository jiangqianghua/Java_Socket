package demo01.client;

import demo01.client.bean.ServerInfo;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @Author: jiang qiang hua
 * @Description:<h1></h1>
 * @Date: Create in 20:05 2019/2/24
 * @Modified By:
 **/
public class TCPClient {

    public static void linkWith(ServerInfo info)throws IOException{
        Socket socket = new Socket();
        socket.setSoTimeout(3000);
        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()),info.getPort()));

        System.out.println("已发起服务器连接，并进入后的流程");

        System.out.println("客户端信息:"+socket.getLocalAddress() + ":"+socket.getLocalPort());
        System.out.println("服务器端信息:"+socket.getInetAddress() +":"+socket.getPort());

        try{
            todo(socket);
        }catch (Exception e){
            System.out.println("异常关闭:"+e.getMessage());
        }
        socket.close();
        System.out.println("客户端退出");
    }

    private static void todo(Socket client) throws IOException{
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        OutputStream outputStream = client.getOutputStream();
        PrintStream socketPrintStream = new PrintStream(outputStream);

        InputStream inputStream = client.getInputStream();
        BufferedReader socketBufferReader = new BufferedReader(new InputStreamReader(inputStream));

        boolean flag = true ;

        do{
            String str = input.readLine() ;
            socketPrintStream.println(str);

            String echo = socketBufferReader.readLine();
            if("bye".equals(echo)){
                flag = true ;
            }else{
                System.out.println("收到服务器消息:"+echo);
            }
        }while (flag);

        socketBufferReader.close();
        socketPrintStream.close();
    }
}
