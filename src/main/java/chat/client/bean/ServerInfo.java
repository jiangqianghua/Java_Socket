package chat.client.bean;

/**
 * @Author: jiang qiang hua
 * @Description:<h1></h1>
 * @Date: Create in 12:55 2019/2/23
 * @Modified By:
 **/
public class ServerInfo {

    private String sn ;

    private int port ;

    private String address ;


    public ServerInfo(int port, String address,String sn) {
        this.sn = sn;
        this.port = port;
        this.address = address;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "sn='" + sn + '\'' +
                ", port=" + port +
                ", address='" + address + '\'' +
                '}';
    }
}
