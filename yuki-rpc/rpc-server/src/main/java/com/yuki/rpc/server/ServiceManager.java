package com.yuki.rpc.server;

import io.netty.channel.Channel;
import com.yuki.rpc.common.RpcRequest;
import com.yuki.rpc.common.RpcResponse;
import com.yuki.rpc.common.ServiceRegistry;
import org.apache.zookeeper.KeeperException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 服务实现端的服务管理器，对请求进行路由调用实际服务
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/22
 * Time: 19:18
 */
public class ServiceManager {

    private ServiceManager(){}
    private static ServiceManager serviceManager = new ServiceManager();
    public static ServiceManager getInstance() {
        return serviceManager;
    }

    private ServiceRegistry registry = new ServiceRegistry("192.168.149.133:2181");
    private static final int COUNT = 200000;

    //业务处理线程池
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10,
            10,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(COUNT));

    //将Service的全限定名作为服务名
    private final Map<String, Service> serviceMap = new HashMap<>();

    /**
     * 使用线程池处理请求，处理完的请求通过channel入socket中
     * @param request 请求对象
     * @param channel 对应的通道
     */
    public void addServiceTask(RpcRequest request, Channel channel){
        threadPoolExecutor.execute(new ServiceTask(request,channel));
    }

    class ServiceTask implements Runnable{
        private RpcRequest request;
        private Channel channel;

        public ServiceTask(RpcRequest request, Channel channel) {
            this.request = request;
            this.channel = channel;
        }

        @Override
        public void run() {
            String serviceName = request.getServiceName();
            String methodName = request.getMethodName();
            Object[] args = request.getArgs();
            Service service = serviceMap.get(serviceName);
            //判断服务是否存在
            if (service == null) {
                channel.write(RpcResponse.ServiceOffline(serviceName, request.getRequestId()));
                return;
            }
            //获取服务
            Object result = service.invoke(methodName, args);
            channel.write(RpcResponse.OK(result, request.getRequestId()));
        }
    }

    //添加服务定义
    public void addService(Service service) {
        String serviceName = service.getServiceName();
        if (!serviceMap.containsKey(serviceName)) {
            serviceMap.put(serviceName, service);
        }
    }

    public void addService(Class<?> interfaceClass, Class<?> implClassName) {
        Service service = new Service(implClassName, implClassName);
        String serviceName = interfaceClass.getName();
        if (!serviceMap.containsKey(serviceName)) {
            serviceMap.put(serviceName, service);
        }
    }

    public void registerAllServices(int port) {
        String hostAddress = getLocalAddress();
        for (String serviceName : serviceMap.keySet()) {
            try {
                registry.registerService(serviceName, hostAddress +":" +port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerService(Service service, int port) {
        String hostAddress = getLocalAddress();
        assert (hostAddress != null);
        try {
            registry.registerService(service.getServiceName(), hostAddress +":" +port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getLocalAddress() {
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
        return address.getHostAddress();
    }
}
