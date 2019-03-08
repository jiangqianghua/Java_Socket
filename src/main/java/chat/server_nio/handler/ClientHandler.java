package chat.server_nio.handler;

import chat.utils.CloseUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: jiang qiang hua
 * @Description:<h1></h1>
 * @Date: Create in 22:55 2019/2/25
 * @Modified By:
 **/
public class ClientHandler {

    private final SocketChannel socketChannel ;

    private final ClientReadHandler clientReadHandler ;
    private final ClientWriteHandler clientWriteHandler ;
    private final ClientHandlerCallBack clientHandlerCallBack;
    private final String clientInfo ;
    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallBack clientHandlerCallBack) throws IOException{
        this.socketChannel = socketChannel ;
        // 设置非阻塞模式
        this.socketChannel.configureBlocking(false);
        Selector readSelector = Selector.open();
        socketChannel.register(readSelector, SelectionKey.OP_READ);

        Selector writeSelector = Selector.open();
        socketChannel.register(writeSelector, SelectionKey.OP_WRITE);

        clientReadHandler = new ClientReadHandler(readSelector);
        clientWriteHandler = new ClientWriteHandler(writeSelector);

        this.clientHandlerCallBack = clientHandlerCallBack ;
        clientInfo =  socketChannel.getRemoteAddress().toString();// 获取客户端信息
        System.out.println("新客户端连接:"+clientInfo);
    }

    public String getClientInfo(){
        return this.clientInfo;
    }

    public void send(String str){
//        try {
//            BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//            bufferWriter.write(str);
//        }catch (IOException e){
//            e.printStackTrace();
//        }
        clientWriteHandler.send(str);
    }

    public void exit(){
        clientReadHandler.exit();
        clientWriteHandler.exit();
        CloseUtils.close(socketChannel);
        System.out.println("客户端已退出:" + clientInfo);
    }

    private void exitBySlef(){
        exit();
        clientHandlerCallBack.onSelfClosed(this);
    }

    public void readToPrint(){
        clientReadHandler.start();
    }

    /**
     * 接受线程
     */
    private class ClientReadHandler extends Thread{
        private boolean done = false ;

        private final Selector selector ;
        private final ByteBuffer byteBuffer ;

        ClientReadHandler(Selector selector){
            this.selector = selector ;
            byteBuffer = ByteBuffer.allocate(256);
        }

        @Override
        public void run() {
            super.run();

            try {
                do{

                    if(selector.select() == 0){
                        if(done)
                            break;
                        continue;
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while(iterator.hasNext()){
                        if(done)
                            break;

                        SelectionKey key = iterator.next() ;
                        iterator.remove();
                        if(key.isReadable()){
                            SocketChannel client = (SocketChannel)key.channel();
                            byteBuffer.clear();
                            int read = client.read(byteBuffer);
                            if(read > 0){
                                String str = new String(byteBuffer.array(),0,read-1); // 丢失掉换行
                                System.out.println(str);
                                clientHandlerCallBack.onNewMessageArrived(ClientHandler.this,str);
                            }else{
                                System.out.println("客户端无法接受到数据");
                                exitBySlef();
                                break;
                            }
                        }
                    }
                }while(!done);
            }catch (Exception e){
                e.printStackTrace();
                if(!done){
                    System.out.println("客户端异常断开");
                    exitBySlef();
                }
            }finally {
                CloseUtils.close(selector);
            }

        }

        void exit(){
            done = true;
            selector.wakeup();
            CloseUtils.close(selector);
        }
    }

    /**
     * 发送线程
     */
    class ClientWriteHandler{

        private boolean done = false ;

        private Selector selector;
        private ByteBuffer byteBuffer ;
        private final ExecutorService executorService ;

        ClientWriteHandler(Selector selector){
            this.selector = selector ;
            this.byteBuffer = ByteBuffer.allocate(256);
            executorService = Executors.newSingleThreadExecutor();
        }


        void exit(){
            done = true ;
            CloseUtils.close(selector);
            executorService.shutdownNow();
        }

        void send(String str){
            if(done)
                return ;
            executorService.execute(new WriteRunnable(str));
        }

        class WriteRunnable implements Runnable{
            private final String msg ;
            WriteRunnable(String msg){
                this.msg = msg + '\n';
            }

            @Override
            public void run() {
                if(done)
                    return ;
                byteBuffer.clear();
                byteBuffer.put(msg.getBytes());
                // 把指针归0
                byteBuffer.flip();
                // 判断buffer是否还有可以发送的数据
                while (!done&&byteBuffer.hasRemaining()){
                    try {
                        int len = socketChannel.write(byteBuffer);
                        // len == 0是合法的
                        if(len < 0){
                            System.out.println("客户端无法发送数据");
                            exitBySlef();
                            break;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
        }

    }

    public interface ClientHandlerCallBack {
        void onSelfClosed(ClientHandler handler);
        //收到消息通知
        void onNewMessageArrived(ClientHandler handler, String msg);
    }
}
