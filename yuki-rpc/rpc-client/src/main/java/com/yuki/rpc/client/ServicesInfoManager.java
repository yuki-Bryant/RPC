package com.yuki.rpc.client;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/23
 * Time: 15:17
 */
public class ServicesInfoManager {
    private static ServicesInfoManager servicesInfoManager = new ServicesInfoManager();

    public static ServicesInfoManager getInstance() {
        return servicesInfoManager;
    }

    private Map<String, ServiceInfo> servicesInfo = new ConcurrentHashMap<>();

    public static class ServiceInfo{
        private List<String> providerList = new ArrayList<>();
        private List<Channel> channelList = new ArrayList<>();
        private AtomicInteger round = new AtomicInteger(0);

        private Channel getChannel() {
            if (channelList.size() < 0) {
                return null;
            }
            return channelList.get(round.getAndIncrement() % channelList.size());
        }

        synchronized void addChannel(Channel channel) {
            channelList.add(channel);
        }
    }

    public Channel getChannel(String serviceName) {
        ServiceInfo serviceInfo = servicesInfo.get(serviceName);
        return serviceInfo.getChannel();
    }

    public void addServiceChannel(String serviceName, Channel channel) {
        servicesInfo.get(serviceName).addChannel(channel);
    }
}
