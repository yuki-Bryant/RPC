package com.yuki.rpc.server;

import com.yuki.rpc.common.RpcResponse;
import com.yuki.rpc.common.serialize.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledDirectByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/22
 * Time: 21:17
 */
public class ResponseEncoder extends ChannelOutboundHandlerAdapter {
    private static Serialization serialization = Serialization.getDefaultSerialization();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception{
        RpcResponse result = (RpcResponse) msg;
        byte[] response = serialization.getBytes(result);
        //疑问??
        ByteBuf resp = Unpooled.buffer(response.length+4);

        resp.writeInt(response.length);
        resp.writeBytes(response);
        ctx.writeAndFlush(resp);
    }

}
