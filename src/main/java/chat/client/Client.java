package chat.client;

import chat.client.bean.ServerInfo;

import java.io.*;

/**
 * @Author: jiang qiang hua
 * @Description:<h1></h1>
 * @Date: Create in 12:53 2019/2/23
 * @Modified By:
 **/
public class Client {

    public static void main(String[] args) {
        ServerInfo serverInfo = ClientSearcher.searchServer(10000);
        System.out.println(serverInfo.getSn() + " " + serverInfo.getAddress() + ":" + serverInfo.getPort());

        if(serverInfo != null){
            TCPClient tcpClient = null ;
            try{
                tcpClient = TCPClient.startWith(serverInfo);
                if(tcpClient == null)
                    return ;
                write(tcpClient);
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                if(tcpClient != null)
                    tcpClient.exit();
            }
        }
    }

    private static void write(TCPClient tcpClient) throws IOException{
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        do{
            String str = input.readLine() ;
            tcpClient.send(str);

            if("00bye00".equals(str)){
                break;
            }
        }while (true);
    }
}
