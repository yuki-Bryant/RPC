package com.yuki.rpc.client;

import com.yuki.rpc.common.RpcRequest;
import com.yuki.rpc.common.RpcResponse;
import com.yuki.rpc.common.ServiceRegistry;
import com.yuki.rpc.common.serialize.Serialization;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/23
 * Time: 14:40
 */
public class RpcClient {
    private static Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private Bootstrap bootstrap;

    private ServiceRegistry serviceRegistry = new ServiceRegistry("192.168.149.133:2181");
    //注册zookeeper的监听器
    private Watcher providersWatcher = new ProvidersChangedWatcher();
    //轮询服务器列表的号码
    private AtomicInteger round = new AtomicInteger(0);

    //服务信息
    private final Map<String, List<String>> services = new ConcurrentHashMap<>();

    private final Map<String, Channel> serviceChannelMap = new ConcurrentHashMap<>();

    private final Map<Long, PromiseResponse> responseMap = new ConcurrentHashMap<>();

    private RpcContext rpcContext = RpcContext.getInstance();

    private Serialization serialization = Serialization.getDefaultSerialization();

    public RpcClient() {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup(1);
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new RequestEncoder());
                        pipeline.addFirst(new LengthFieldBasedFrameDecoder(
                                10000000,
                                0,
                                4,
                                0,
                                4));
                        pipeline.addLast("handler", new RpcInClientHandler());
                    }
                });
        this.bootstrap = bootstrap;
    }

    /**
     * 重要的一个方法，可以被多个线程调用，这里的多个线程应该由用户来定义
     * 这个方法是同步阻塞的，如果超过3秒则返回超时
     *
     * @param request
     * @return
     */
    public RpcResponse send(RpcRequest request) {
        Channel channel = null;
        String serviceName = request.getServiceName();

        try {
            //获得一个Channel
            if (serviceChannelMap.containsKey(serviceName)) {
                channel = serviceChannelMap.get(serviceName);
            }
            if (channel == null || !channel.isOpen()) {
                //从zookeeper拉取服务列表
                String providerAddress = getProviderAddress(serviceName);//提供者地址，由IP和端口号组成
                if (providerAddress == null) {
                    return RpcResponse.ProviderNotFound(serviceName, request.getRequestId());
                }
                String[] address = providerAddress.split(":");
                String host = address[0];
                int port = Integer.parseInt(address[1]);

                //新建一条链路
                channel = bootstrap.connect(host, port).sync().channel();
            }
            ChannelPromise promise = (ChannelPromise) channel.write(request);
            PromiseResponse promiseResponse = new PromiseResponse(promise);
            responseMap.put(request.getRequestId(), promiseResponse);

            //阻塞调用，如果返回null，说明服务器调用超时
            RpcResponse response = promiseResponse.getResponse();
            if (response == null) {
                return RpcResponse.TimeOut(request.getRequestId());
            }

            responseMap.remove(request.getRequestId());
            //如果服务器返回的状态是200则将服务名和通道放入serviceChannel
            if (response.getStatus() == 200) {
                serviceChannelMap.put(serviceName, channel);
            }

            return response;
        } catch (InterruptedException e) {
            logger.warn(e.getMessage());
            e.printStackTrace();
            if (channel != null) {
                channel.close();
            }
            return RpcResponse.ThreadException();
        } catch (KeeperException e) {
            logger.warn(e.getMessage());
            e.printStackTrace();
            return RpcResponse.ServiceNotFound(serviceName, request.getRequestId());
        } catch (UndeclaredThrowableException e) {
            logger.warn(e.getLocalizedMessage());
            e.printStackTrace();
            return RpcResponse.ProviderNotFound(serviceName, request.getRequestId());
        } catch (Exception e) {
            e.printStackTrace();
            return RpcResponse.ThreadException();
        }
    }

    /**
     * 异步调用，客户端如果想要获得结果则要通过RpcContext获取PromiseResponse
     *
     * @param request
     * @return
     */
    public RpcResponse sendAsync(RpcRequest request) {
        Channel channel = null;
        String serviceName = request.getServiceName();

        try {
            //获得一个Channel
            if (serviceChannelMap.containsKey(serviceName)) {
                channel = serviceChannelMap.get(serviceName);
            }
            if (channel == null || !channel.isOpen()) {
                //从zookeeper拉取服务列表
                String providerAddress = getProviderAddress(serviceName);//提供者地址，由IP和端口号组成
                if (providerAddress == null) {
                    return RpcResponse.ProviderNotFound(serviceName, request.getRequestId());
                }
                String[] address = providerAddress.split(":");
                String host = address[0];
                int port = Integer.parseInt(address[1]);

                //新建一条链路
                channel = bootstrap.connect(host, port).sync().channel();
            }
            ChannelPromise promise = (ChannelPromise) channel.write(request);
            PromiseResponse promiseResponse = new PromiseResponse(promise);
            Channel finalChannel = channel;
            promiseResponse.addListener(() -> serviceChannelMap.put(serviceName, finalChannel));
            responseMap.put(request.getRequestId(), promiseResponse);
            rpcContext.addPromise(promiseResponse);
            return null;
        } catch (InterruptedException e) {
            logger.warn(e.getMessage());
            e.printStackTrace();
            if (channel != null) {
                channel.close();
            }
            return RpcResponse.ThreadException();
        } catch (KeeperException e) {
            logger.warn(e.getMessage());
            e.printStackTrace();
            return RpcResponse.ServiceNotFound(serviceName, request.getRequestId());
        } catch (UndeclaredThrowableException e) {
            logger.warn(e.getLocalizedMessage());
            e.printStackTrace();
            return RpcResponse.ProviderNotFound(serviceName, request.getRequestId());
        } catch (Exception e) {
            e.printStackTrace();
            return RpcResponse.ThreadException();
        }
    }

    /**
     * 获得指定服务的一个服务器地址，这里使用了轮询法
     *
     * @param serviceName 服务的名称
     * @return 一个服务器地址，没有对应的服务提供者时返回null
     */
    private String getProviderAddress(String serviceName) throws Exception {

        List<String> providerList = services.get(serviceName);
        if (providerList == null) {
            providerList = serviceRegistry.getProviderAddress(serviceName, providersWatcher);
            synchronized (services) {
                if (services.get(serviceName) == null) {
                    services.put(serviceName, providerList);
                }
            }
        }
        if (providerList == null || providerList.size() == 0)
            return null;
        int serverIndex = (round.getAndIncrement() % providerList.size());
        return providerList.get(serverIndex);
    }

    public void close() {
        serviceRegistry.close();
    }

    /**
     * 监听器，一旦服务节点的子节点发生了变化，说明有机器上线或下线
     * 这个时候需要更新services和servicesChannelMap
     */
    class ProvidersChangedWatcher implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeChildrenChanged){
                String servicePath = event.getWrapper().getPath();
                String serviceName = StringUtil.getServiceName(servicePath);

                try {
                    List<String> providerAddresses = serviceRegistry.getProviderAddress(serviceName, this);
                    services.put(serviceName, providerAddresses);
                    serviceChannelMap.remove(serviceName);
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("服务下的子节点发生改变");
            }
        }
    }

    class RpcInClientHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);

            RpcResponse response = serialization.getResponse(bytes);
            long id = response.getRequestId();
            PromiseResponse promiseResponse = responseMap.get(id);

            if (promiseResponse != null) {
                promiseResponse.setResponse(response);
            }
            //释放buf中缓存的数据
            buf.release();
        }
    }
}
