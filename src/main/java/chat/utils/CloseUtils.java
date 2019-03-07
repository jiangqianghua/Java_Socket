package chat.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * @Author: jiang qiang hua
 * @Description:<h1></h1>
 * @Date: Create in 23:24 2019/2/25
 * @Modified By:
 **/
public class CloseUtils {
    public static void close(Closeable ...closeables){
        if(closeables == null)
            return ;
        for(Closeable closeable:closeables){
            try{
                closeable.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
