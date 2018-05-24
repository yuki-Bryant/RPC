package com.yuki.rpc.server;

import com.yuki.rpc.common.caculate;
import com.yuki.rpc.server.impl.caculateImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/23
 * Time: 16:39
 */
public class RpcServer {
    private static Logger logger = LoggerFactory.getLogger(RpcServer.class);
    private ServiceManager serviceManager = ServiceManager.getInstance();

    /**
     * 启动服务提供者
     * @param port 服务器运行的端口号
     */
    public void startServer(int port) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup(2);
        EventLoopGroup worker = new NioEventLoopGroup();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.SO_BACKLOG, 8192)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ResponseEncoder());

                        pipeline.addFirst(new LengthFieldBasedFrameDecoder(
                                10000000,
                                0,
                                4,
                                0,
                                4));
                        pipeline.addLast(new RpcHandler());
                    }
                });
        ChannelFuture future = bootstrap.bind(port);
        Service service = new Service(caculate.class, caculateImpl.class);

        serviceManager.registerService(service, port);
        future.addListener((ChannelFutureListener) future1 -> System.out.println("服务器绑定到" + port + "端口"));
        try {
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
