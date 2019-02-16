package com.jqh.udpboardcast;

/**
 * @Author: jiang qiang hua
 * @Description:<h1></h1>
 * @Date: Create in 21:49 2019/1/27
 * @Modified By:
 **/
public class MessageCreator {

    private static final String SN_HEADER = "收到暗号,我是(SN):";

    private static final String PORT_HEADER = "这是暗号，请回调端口(PORT):";

    public static String buildWithPort(int port){
        return PORT_HEADER + port;
    }

    public static int parsePort(String data){
        if(data.startsWith(PORT_HEADER)){
            return Integer.parseInt(data.substring(PORT_HEADER.length()));
        }
        return -1;
    }

    public static  String buildWithSn(String sn){
        return SN_HEADER + sn;
    }

    public static String parseSn(String data){
        if(data.startsWith(SN_HEADER)){
            return data.substring(SN_HEADER.length());
        }
        return null;
    }
}
