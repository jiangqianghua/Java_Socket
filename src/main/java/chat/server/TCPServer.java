package chat.server;

import chat.server.handler.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: jiang qiang hua
 * @Description:<h1></h1>
 * @Date: Create in 19:44 2019/2/24
 * @Modified By:
 **/
public class TCPServer implements ClientHandler.ClientHandlerCallBack {

    private final int port ;

    private ClientListener mListener ;

    // 保证内部操作的安全
   // private List<ClientHandler> clientHandlerList = Collections.synchronizedList(new ArrayList<>());
    private List<ClientHandler> clientHandlerList = new ArrayList<>();
    private final ExecutorService forwardingThreadPoolExecutor ;

    public TCPServer(int port){
        this.port = port ;
        forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor();
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
        synchronized (TCPServer.this) {
            for (ClientHandler clientHandler : clientHandlerList) {
                clientHandler.exit();
            }
            clientHandlerList.clear();
        }
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
                    ClientHandler clientHandler = new ClientHandler(client, TCPServer.this);
                    // 启动读线程
                    clientHandler.readToPrint();
                    synchronized (TCPServer.this) {
                        clientHandlerList.add(clientHandler);
                    }
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


    public synchronized void broadcast(String str) throws Exception{
        for(ClientHandler clientHandler:clientHandlerList){
            clientHandler.send(str);
        }
    }

    @Override
    public synchronized void onSelfClosed(ClientHandler handler) {
        clientHandlerList.remove(handler);
    }

    @Override
    public void onNewMessageArrived(ClientHandler handler, String msg) {
        System.out.println("Received:"+handler.getClientInfo()+":"+msg);
        forwardingThreadPoolExecutor.execute(()->{
            synchronized (TCPServer.this){
                for(ClientHandler clientHandler:clientHandlerList){
                    if(clientHandler.equals(handler))
                        continue;
                    clientHandler.send(msg);
                }
            }
        });
     //   forwardingThreadPoolExecutor.shutdownNow();
    }
}
