package com.yuki.rpc.client;

import com.yuki.rpc.common.RpcRequest;
import com.yuki.rpc.common.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 服务动态代理类，为服务代表的接口创建一个代理对象，这个代理对象的方法被调用时，交由远程的服务来完成
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/23
 * Time: 14:38
 */
public class ServiceProxy {
    private static Logger logger = LoggerFactory.getLogger(ServiceProxy.class);
    private RpcClient rpcClient = new RpcClient();

    //表示请求的id
    private AtomicLong requestId = new AtomicLong(0);

    private static volatile boolean sync = true;

    /**
     * 创建特定接口的代理
     *
     * @param clazz 必须是接口
     * @param <T>
     * @return 代理类实例
     */
    public <T> T createProxy(Class<T> clazz){
        Object o = Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{clazz},
                new ServiceProxyHandler(clazz));
        return (T) o;
    }

    class ServiceProxyHandler implements InvocationHandler{

        private Class interfaceClass;

        public ServiceProxyHandler(Class interfaceClass){
            this.interfaceClass = interfaceClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            String serviceName = interfaceClass.getName();
            RpcRequest request = new RpcRequest();
            request.setRequestId(requestId.getAndIncrement());
            request.setServiceName(serviceName);
            request.setMethodName(methodName);
            request.setArgs(args);

            RpcResponse response = null;
            if (sync){
                response = rpcClient.send(request);
            }else {
                response = rpcClient.sendAsync(request);
                if (response == null) {
                    return null;
                }
            }

            int status = response.getStatus();
            switch (status) {
                case RpcResponse.OK:
                    logger.debug("request " + request.getRequestId() +" invoke success");
                    return response.getResult();
                case RpcResponse.SERVICE_NOT_FOUND:
                    logger.warn("service not found");
                    return null;
                case RpcResponse.SERVICE_OFFLINE:
                    logger.warn("service offline");
                    return null;
                case RpcResponse.TIMEOUT:
                    logger.warn("response timeout");
                    return null;
                case RpcResponse.THREAD_EXCEPTION:
                    logger.warn("thread was interrupt");
                    return null;
                default:
                    return null;
            }
        }
    }

    public void close() {
        rpcClient.close();
    }

    public static void setSync() {
        sync = true;
    }

    public static void setAsync() {
        sync = false;
    }

}
