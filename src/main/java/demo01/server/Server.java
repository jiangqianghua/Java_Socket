package demo01.server;

import demo01.constants.TCPConstants;

/**
 * @Author: jiang qiang hua
 * @Description:<h1>利用udp局域网搜索到所有服务器port，然后利用tcp连接这些端口</h1>
 * @Date: Create in 9:44 2019/2/23
 * @Modified By:
 **/
public class Server {

    public static void main(String[] args) {


        ServerProvider.start(TCPConstants.PORT_SERVER);
        try{
            System.in.read();
        }catch (Exception e){

        }


        ServerProvider.stop();

    }
}
