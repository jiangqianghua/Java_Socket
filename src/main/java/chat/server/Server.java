package chat.server;

import chat.constants.TCPConstants;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @Author: jiang qiang hua
 * @Description:<h1>利用udp局域网搜索到所有服务器port，然后利用tcp连接这些端口</h1>
 * @Date: Create in 9:44 2019/2/23
 * @Modified By:
 **/
public class Server {

    public static void main(String[] args) {

        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSuccess = tcpServer.start();
        if(!isSuccess){
            System.out.println("Start TCP server failed");
            return ;
        }
        ServerProvider.start(TCPConstants.PORT_SERVER);
        try{
//            System.in.read();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String str = "";
            do{
                str = bufferedReader.readLine() ;
                tcpServer.broadcast(str);
            }while (!"00bye00".equals(str));
        }catch (Exception e){
            e.printStackTrace();
        }


        ServerProvider.stop();
        tcpServer.stop();
    }
}
