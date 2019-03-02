package demo01.server;

import demo01.server.handler.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jiang qiang hua
 * @Description:<h1></h1>
 * @Date: Create in 19:44 2019/2/24
 * @Modified By:
 **/
public class TCPServer {

    private final int port ;

    private ClientListener mListener ;

    private List<ClientHandler> clientHandlerList = new ArrayList<>();

    public TCPServer(int port){
        this.port = port ;
    }

    public boolean start(){
        try {
            ClientListener listener = new ClientListener(port);
            mListener = listener;
            listener.start();
        }catch (IOException e){
            e.printStackTrace();;
            return false ;
        }
        return true ;
    }

    public void stop(){
        if(mListener != null){
            mListener.exit();
        }

        for(ClientHandler clientHandler:clientHandlerList){
            clientHandler.exit();
        }
        clientHandlerList.clear();
    }

    private  class ClientListener extends Thread{
        private ServerSocket server ;
        private boolean done = false ;

        private ClientListener(int port) throws  IOException{
                server = new ServerSocket(port);
                System.out.println("服务器信息："+server.getInetAddress() + " :"+port);
        }

        @Override
        public void run() {
            System.out.println("服务器准备就绪!");
            do{
                Socket client ;
                try {
                    client = server.accept();
                    ClientHandler clientHandler = new ClientHandler(client, new ClientHandler.CloseNotify() {
                        @Override
                        public void onSelfClosed(ClientHandler handler) {
                            clientHandlerList.remove(handler);
                        }
                    });
                    // 启动读线程
                    clientHandler.readToPrint();
                    clientHandlerList.add(clientHandler);
                }catch (IOException e){
                    System.out.println("客户端连接异常:"+e.getMessage());
                    continue;
                }

            }while (!done);
            System.out.println("服务器已经关闭");
        }

        void exit(){
            done = true ;
            try{
                server.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    public void broadcast(String str) throws Exception{
        for(ClientHandler clientHandler:clientHandlerList){
            clientHandler.send(str);
        }
    }
}
