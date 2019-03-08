package chat.server_nio;

import chat.server_nio.handler.ClientHandler;
import chat.utils.CloseUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
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

    private Selector selector ;
    private ServerSocketChannel server;
    public TCPServer(int port){
        this.port = port ;
        forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor();
    }

    public boolean start(){
        try {
            selector = Selector.open();
            server = ServerSocketChannel.open();
            // 设置非堵塞
            server.configureBlocking(false);
            // 绑定端口
            server.socket().bind(new InetSocketAddress(port));
            //注册客户端连接达到监听
            server.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("服务器信息："+server.getLocalAddress().toString());
            ClientListener listener = new ClientListener();
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
        CloseUtils.close(selector);
        CloseUtils.close(selector);
        synchronized (TCPServer.this) {
            for (ClientHandler clientHandler : clientHandlerList) {
                clientHandler.exit();
            }
            clientHandlerList.clear();
        }
    }

    private  class ClientListener extends Thread{
        private boolean done = false ;

        @Override
        public void run() {
            System.out.println("服务器准备就绪!");
            Selector selector = TCPServer.this.selector ;
            do{
                Socket client ;
                try {
                    if(selector.select() == 0){
                        if(done){
                            break;
                        }
                        continue;
                    }

                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()){
                        if(done)
                            break;

                        SelectionKey key = iterator.next();
                        iterator.remove();

                        // 判断key是否是我们要关注的状态
                        if(key.isAcceptable()){
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel() ;
                            // 非阻塞拿到客户端连接
                            SocketChannel socketChannel = serverSocketChannel.accept() ;
                            ClientHandler clientHandler = new ClientHandler(socketChannel, TCPServer.this);
                            // 启动读线程
                            clientHandler.readToPrint();
                            synchronized (TCPServer.this) {
                                clientHandlerList.add(clientHandler);
                            }
                        }
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
            // 唤醒当前的阻塞
           selector.wakeup();
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
