package demo01.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author: jiang qiang hua
 * @Description:<h1></h1>
 * @Date: Create in 19:44 2019/2/24
 * @Modified By:
 **/
public class TCPServer {

    private final int port ;

    private ClientListener mListener ;

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
    }

    private static class ClientListener extends Thread{
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
                    ClientHandler clientHandler = new ClientHandler(client);
                    clientHandler.start();
                }catch (IOException e){
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

        private static class ClientHandler extends Thread{
            private Socket socket ;

            private boolean flag = true ;

            ClientHandler(Socket socket){
                this.socket = socket ;
            }

            @Override
            public void run() {
                super.run();

                System.out.println("新客户端连接:"+socket.getInetAddress() + ":"+ socket.getPort());
                try {
                    PrintStream socketOutPut = new PrintStream(socket.getOutputStream());
                    BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    do{
                        String str = socketInput.readLine();
                        if("bye".equals(str)){
                            flag = false ;
                            socketOutPut.println("bye");
                        }else{
                            System.out.print(str);
                            socketOutPut.println("回送:"+str.length());
                        }
                    }while(flag);
                    socketInput.close();
                    socketOutPut.close();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    try{
                        socket.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }

                System.out.println("客户端退出:" +socket.getInetAddress() + ":"+socket.getPort());
            }
        }
    }
}
