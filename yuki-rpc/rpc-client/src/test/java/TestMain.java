
import com.yuki.rpc.client.PromiseResponse;
import com.yuki.rpc.client.RpcContext;
import com.yuki.rpc.client.ServiceProxy;
import com.yuki.rpc.common.caculate;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/23
 * Time: 16:52
 */
public class TestMain {
    private RpcContext rpcContext = RpcContext.getInstance();
    private int count = 100;

    @Test
    //测试同步
    public void testSysc(){
        //测试同步调用
        ServiceProxy proxy = new ServiceProxy();
        ServiceProxy.setSync();

        caculate caculate = proxy.createProxy(caculate.class);
        int[] results = new int[count];

        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            results[i] = caculate.add(1, 2*i);
        }

        long end = System.currentTimeMillis();
        System.out.println((end - start)+"  ms");

        for (int result : results) {
            System.out.println("得到结果为："+result);
        }
        proxy.close();
    }

    @Test
    //测试异步
    public void testAsysc(){
        ServiceProxy proxy = new ServiceProxy();
        ServiceProxy.setAsync();

        caculate caculate = proxy.createProxy(caculate.class);
        Integer[] results = new Integer[count];

        long start = System.currentTimeMillis();
        PromiseResponse[] promiseResponses = new PromiseResponse[count];

        for (int i = 0; i < count; i++) {
            results[i] = caculate.add(0, 2*i);
            promiseResponses[i] = rpcContext.getPromise();
        }

        //要从promiseResponse里面取
        for (int i = 0; i < count; i++) {
            results[i] = (Integer) promiseResponses[i].get(3000);
        }

        long end = System.currentTimeMillis();
        System.out.println((end - start)+"  ms");

        for (int result : results) {
            System.out.println("得到结果为："+result);
        }
        proxy.close();
    }
}
