package demo01.constants;

/**
 * @Author: jiang qiang hua
 * @Description:<h1></h1>
 * @Date: Create in 23:32 2019/2/22
 * @Modified By:
 **/
public class UDPConstants {
    public static byte[] HEADER = new byte[]{7,7,7,7,7,7,7,7};
    // 服务器固定端口
    public static int PORT_SERVER = 30201 ;
    // 客户端监听的端口，服务器往这个端口回应数据
    public static int PORT_CLIENT_RESPONSE = 30202 ;
}
