package com.github.rpc.common.registry;

import com.github.rpc.config.RegistryConfig;
import com.github.rpc.model.dto.ServiceMetaInfoDTO;

import java.util.List;

//TODO注册服务中心 接口
public interface Registry {

    /**
     * 初始化
     *
     * @param registryConfig
     */
    void init(RegistryConfig registryConfig);

    /**
     * 注册服务（服务端）
     *
     * @param ServiceMetaInfoDTO
     */
    void register(ServiceMetaInfoDTO ServiceMetaInfoDTO) throws Exception;

    /**
     * 注销服务（服务端）
     *
     * @param ServiceMetaInfoDTO
     */
    void unRegister(ServiceMetaInfoDTO ServiceMetaInfoDTO);

    /**
     * 服务发现（获取某服务的所有节点，消费端）
     *
     * @param serviceKey 服务键名
     * @return
     */
    List<ServiceMetaInfoDTO> serviceDiscovery(String serviceKey);

    /**
     * 心跳检测（服务端）
     */
    void heartBeat();

    /**
     * 监听（消费端）
     *
     * @param serviceNodeKey
     */
    void watch(String serviceNodeKey);

    /**
     * 服务销毁
     */
    void destroy();
}
