package demo01.server.handler;

import demo01.clink.net.qiujuer.clink.utils.CloseUtils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: jiang qiang hua
 * @Description:<h1></h1>
 * @Date: Create in 22:55 2019/2/25
 * @Modified By:
 **/
public class ClientHandler {

    private final Socket socket ;

    private final ClientReadHandler clientReadHandler ;
    private final ClientWriteHandler clientWriteHandler ;
    private final CloseNotify closeNotify ;

    public ClientHandler(Socket socket,CloseNotify closeNotify) throws IOException{
        this.socket = socket ;
        clientReadHandler = new ClientReadHandler(socket.getInputStream());
        clientWriteHandler = new ClientWriteHandler(socket.getOutputStream());
        this.closeNotify = closeNotify ;
        System.out.println("新客户端连接:"+socket.getInetAddress() + ":"+ socket.getPort());
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
        CloseUtils.close(socket);
        System.out.println("客户端已退出:" + socket.getInetAddress() + ":" + socket.getPort());
    }

    private void exitBySlef(){
        exit();
        closeNotify.onSelfClosed(this);
    }

    public void readToPrint(){
        clientReadHandler.start();
    }

    /**
     * 接受线程
     */
    private class ClientReadHandler extends Thread{
        private boolean done = false ;

        private final InputStream inputStream ;

        ClientReadHandler(InputStream inputStream){
            this.inputStream = inputStream ;
        }

        @Override
        public void run() {
            super.run();

            try {
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));
                do{
                    String str = socketInput.readLine();
                    if(str == null){
                        System.out.println("客户端无法接受到数据");
                        exitBySlef();
                        break;
                    }
                    System.out.println(str);
                }while(!done);
                socketInput.close();
            }catch (Exception e){
                e.printStackTrace();
                if(!done){
                    System.out.println("客户端异常断开");
                    exitBySlef();
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

    /**
     * 发送线程
     */
    class ClientWriteHandler{

        private boolean done = false ;

        private final PrintStream printStream ;

        private final ExecutorService executorService ;

        ClientWriteHandler(OutputStream outputStream){
            printStream = new PrintStream(outputStream);
            executorService = Executors.newSingleThreadExecutor();
            printStream.println("init socket msg");
        }


        void exit(){
            done = true ;
            CloseUtils.close(printStream);
            executorService.shutdownNow();
        }

        void send(String str){
            executorService.execute(new WriteRunnable(str));
        }

        class WriteRunnable implements Runnable{
            private final String msg ;
            WriteRunnable(String msg){
                this.msg = msg ;
            }

            @Override
            public void run() {
                if(done)
                    return ;
                try {
                    printStream.println(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

    }

    public interface CloseNotify{
        void onSelfClosed(ClientHandler handler);
    }
}
