package com.yuki.rpc.server;

import com.yuki.rpc.common.RpcRequest;
import com.yuki.rpc.common.serialize.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/22
 * Time: 21:04
 */
public class RpcHandler extends ChannelInboundHandlerAdapter{

    private static Logger logger = LoggerFactory.getLogger(RpcHandler.class);
    private ServiceManager serviceManager = ServiceManager.getInstance();
    private Serialization serialization = Serialization.getDefaultSerialization();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        RpcRequest request = serialization.getRequest(bytes);

        //只处理ServiceRequest类型的数据
        if (request != null) {
            //开辟线程池另外处理业务逻辑，防止业务逻辑时间过长阻塞，而使I/O复用失去了意义
            serviceManager.addServiceTask(request, ctx.channel());
        }
        buf.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //logger.error("server caught exception", cause);
        ctx.close();
    }
}
