package com.yuki.rpc.server.impl;

import com.yuki.rpc.common.caculate;

/**
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/22
 * Time: 14:32
 */
public class caculateImpl implements caculate {
    @Override
    public Integer add(int a, int b) {
        return a+b;
    }

    @Override
    public Integer sub(int a, int b) {
        return a-b;
    }
}
