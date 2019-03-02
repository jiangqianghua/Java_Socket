package demo01.client;

import demo01.client.bean.ServerInfo;
import demo01.clink.net.qiujuer.clink.utils.CloseUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * @Author: jiang qiang hua
 * @Description:<h1></h1>
 * @Date: Create in 20:05 2019/2/24
 * @Modified By:
 **/
public class TCPClient {

    public static void linkWith(ServerInfo info)throws IOException{
        Socket socket = new Socket();
        socket.setSoTimeout(30000000);
        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()),info.getPort()));

        System.out.println("已发起服务器连接，并进入后的流程");

        System.out.println("客户端信息:"+socket.getLocalAddress() + ":"+socket.getLocalPort());
        System.out.println("服务器端信息:"+socket.getInetAddress() +":"+socket.getPort());

        try{
            ReadHandler readHandler = new ReadHandler(socket.getInputStream());
            readHandler.start();
            write(socket);

            readHandler.exit();
        }catch (Exception e){
            System.out.println("异常关闭:"+e.getMessage());
        }
        socket.close();
        System.out.println("客户端退出");
    }

    private static void write(Socket client) throws IOException{
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        OutputStream outputStream = client.getOutputStream();
        PrintStream socketPrintStream = new PrintStream(outputStream);

        do{
            String str = input.readLine() ;
            socketPrintStream.println(str);

            if("00bye00".equals(str)){
                break;
            }
        }while (true);
        socketPrintStream.close();
    }



    /**
     * 接受线程
     */
    private static class ReadHandler extends Thread{
        private boolean done = false ;

        private final InputStream inputStream ;

        ReadHandler(InputStream inputStream){
            this.inputStream = inputStream ;
        }

        @Override
        public void run() {
            super.run();

            try {
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));
                do{
                    String str = null;
                    try {
                        str = socketInput.readLine();
                    }catch (SocketTimeoutException e){
                        continue;
                    }
                    if (str == null) {
                        System.out.println("客户端无法接受到数据");
                        break;
                    }
                    System.out.println(str);
                }while(!done);
                socketInput.close();
            }catch (Exception e){
                e.printStackTrace();
                if(!done){
                    System.out.println("客户端异常断开" + e.getMessage());
                }
            }finally {
                CloseUtils.close(inputStream);
            }

        }

        void exit(){
            done = true;
            CloseUtils.close(inputStream);
        }
    }
}
