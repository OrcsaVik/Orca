package com.github.rpc.common.loadblanace;

import com.github.rpc.model.dto.ServiceMetaInfoDTO;

import java.util.List;
import java.util.Map;

public interface LoadBalancer {

    /**
     * 选择服务调用
     *
     * @param requestParams       请求参数
     * @param serviceMetaInfoList 可用服务列表
     * @return
     */
    ServiceMetaInfoDTO select(Map<String, Object> requestParams, List<ServiceMetaInfoDTO> serviceMetaInfoList);
}
