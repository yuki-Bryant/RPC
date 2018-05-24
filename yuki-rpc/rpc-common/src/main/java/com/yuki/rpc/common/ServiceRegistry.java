package com.yuki.rpc.common;

import org.apache.zookeeper.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 服务注册中心
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/22
 * Time: 16:34
 */
public class ServiceRegistry {
    private final String serviceRooPath = "/services";
    private ZooKeeper zooKeeper;

    public ServiceRegistry(String hosts){
        try {
            Integer sessionTimeOut = 5000;//通讯的过期时长
            CountDownLatch latch = new CountDownLatch(1);//类似计数器
            zooKeeper = new ZooKeeper(hosts,sessionTimeOut,watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("与ZooKeeper服务器建立连接");
                    latch.countDown();
                }
            });
            latch.await();
            //没有就创建目录
            if (zooKeeper.exists(serviceRooPath, true) == null) {
                zooKeeper.create(serviceRooPath,
                        "".getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
    * 注册服务提供者
    * 在服务节点下建立子节点存储服务提供者的IP地址
    * @param serviceName     服务的名称
    * @param providerAddress 服务提供者的地址，包括IP地址和端口号
    */

    public void registerService(String serviceName,String providerAddress) throws Exception{
        String servicePath = serviceRooPath + "/" + serviceName;
        if (zooKeeper.exists(servicePath, true) == null) {
            zooKeeper.create(servicePath,
                    "".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
        }
        providerAddress = servicePath+ "/" + providerAddress;//具体的子节点
        if (zooKeeper.exists(providerAddress, true) == null) {
            zooKeeper.create(providerAddress,
                    "".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);
        }
    }

    public List<String> getProviderAddress(String serviceName, Watcher watcher) throws KeeperException, InterruptedException {
        String servicePath = serviceRooPath + "/" + serviceName;
        if (zooKeeper.exists(servicePath, true) != null) {
            return zooKeeper.getChildren(servicePath, watcher);
        }
        return null;
    }

    public void close() {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
