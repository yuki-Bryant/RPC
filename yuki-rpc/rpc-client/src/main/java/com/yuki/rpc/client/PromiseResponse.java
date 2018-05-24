package com.yuki.rpc.client;

import com.yuki.rpc.common.RpcResponse;
import io.netty.channel.ChannelPromise;

/**
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/22
 * Time: 21:55
 */
public class PromiseResponse {
    private ChannelPromise promise;
    private RpcResponse response;

    private Listener listener;

    public PromiseResponse(ChannelPromise promise) {
        this.promise = promise;
    }

    /**
     * 同步获得调用结果
     * @return
     * @throws InterruptedException
     */
    public RpcResponse getResponse() throws InterruptedException {
        promise.await(3000);
        return response;
    }

    public void setResponse(RpcResponse response) {
        this.response = response;
        promise.setSuccess();
    }

    public void addListener(Listener listener) {
        this.listener = listener;
    }

    public Object get(int timeout) {
        if (promise.isSuccess()) {
            return response.getResult();
        } else {
            try {
                boolean success = promise.await(timeout);
                if (success) {
                    listener.update();
                    return response.getResult();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
