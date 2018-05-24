import com.yuki.rpc.server.RpcServer;

/**
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/23
 * Time: 16:44
 */
public class ServerStart {
    public static void main(String[] args) {
        //注册服务
        new RpcServer().startServer(8080);
    }
}
