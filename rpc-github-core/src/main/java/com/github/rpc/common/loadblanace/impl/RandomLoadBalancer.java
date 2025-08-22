package com.github.rpc.common.loadblanace.impl;

import cn.hutool.json.JSONUtil;
import com.github.rpc.common.loadblanace.LoadBalancerStrategy;
import com.github.rpc.model.dto.ServiceMetaInfoDTO;

import java.util.List;
import java.util.Map;
import java.util.Random;

public  class RandomLoadBalancer implements LoadBalancerStrategy {

    private final Random random = new Random();

    @Override
    public ServiceMetaInfoDTO select(Map<String, Object> requestParams, List<ServiceMetaInfoDTO> serviceMetaInfoList) {
        int size = serviceMetaInfoList.size();
        System.out.println("size:"+size);
        if (size == 0) {
            return null;
        }
        // 只有 1 个服务，不用随机
        if (size == 1) {
            return serviceMetaInfoList.get(0);
        }

        System.out.println("随机选择对应的服务节点--~");
        ServiceMetaInfoDTO serviceMetaInfoDTO = serviceMetaInfoList.get(random.nextInt(size));
        System.out.println(JSONUtil.toJsonStr(serviceMetaInfoList));
        return serviceMetaInfoDTO;
    }
}