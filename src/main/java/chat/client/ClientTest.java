package chat.client;

import chat.client.bean.ServerInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jiang qiang hua
 * @Description:<h1>对服务器压力测试</h1>
 * @Date: Create in 23:35 2019/3/7
 * @Modified By:
 **/
public class ClientTest {

    private static boolean done = false ;
    public static void main(String[] args) throws IOException {
        ServerInfo serverInfo = ClientSearcher.searchServer(10000);
        System.out.println("Server:"+serverInfo);
        if(serverInfo == null)
            return ;
        int size = 0 ; //  当前连接的数量
        final List<TCPClient> tcpClientList = new ArrayList<>();
        for(int i = 0 ; i < 1000; i++){
            try{
                TCPClient tcpClient = TCPClient.startWith(serverInfo);
                if(tcpClient == null) {
                    System.out.println("连接异常");
                    continue;
                }
                tcpClientList.add(tcpClient);
                System.out.println("连接成功:"+(++size));
            }catch (IOException e){
                System.out.println("连接异常" + e.getMessage());
            }

            try {
                Thread.sleep(20); // 延迟20毫秒，防止连接被拒绝
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        System.in.read();

        Runnable runnable = () -> {
            while(!done){
                for(TCPClient tcpClient:tcpClientList){
                    tcpClient.send("Hello~");
                }
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();

        System.in.read();

        done = true;

        try{
            //  等待所有线程执行完
            thread.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        for(TCPClient tcpClient:tcpClientList){
            tcpClient.exit();
        }
    }
}
