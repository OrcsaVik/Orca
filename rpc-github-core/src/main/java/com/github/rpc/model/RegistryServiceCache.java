package com.github.rpc.model;

import com.github.rpc.model.dto.ServiceMetaInfoDTO;

import java.util.List;

public class RegistryServiceCache {

    /**
     * 服务缓存
     */
    List<ServiceMetaInfoDTO> serviceCache;

    /**
     * 写缓存
     *
     * @param newServiceCache
     * @return
     */
    public void writeCache(List<ServiceMetaInfoDTO> newServiceCache) {
        this.serviceCache = newServiceCache;
    }

    /**
     * 读缓存
     *
     * @return
     */
    public List<ServiceMetaInfoDTO> readCache() {
        return this.serviceCache;
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        this.serviceCache = null;
    }
}
