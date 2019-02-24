package demo01.client;

import demo01.client.bean.ServerInfo;

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
    }
}
