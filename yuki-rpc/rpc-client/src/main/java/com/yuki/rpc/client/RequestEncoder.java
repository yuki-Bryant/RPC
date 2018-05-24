package com.yuki.rpc.client;

import com.yuki.rpc.common.RpcRequest;
import com.yuki.rpc.common.serialize.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/22
 * Time: 21:53
 */
public class RequestEncoder extends ChannelOutboundHandlerAdapter {
    private static Serialization serialization = Serialization.getDefaultSerialization();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        RpcRequest request = (RpcRequest) msg;
        //将请求序列化为byte数组
        byte[] bytes = serialization.getBytes(request);
        ByteBuf buf = Unpooled.buffer(bytes.length + 4);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        ctx.writeAndFlush(buf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        super.exceptionCaught(ctx,cause);
        Channel channel = ctx.channel();
        if (channel.isActive())
            ctx.close();
    }
}
