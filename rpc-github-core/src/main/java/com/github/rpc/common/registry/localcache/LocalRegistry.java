package com.github.rpc.common.registry.localcache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalRegistry {

    /**
     * 注册信息存储 key MetaDTO
     */
    private static final Map<String, Class<?>> map = new ConcurrentHashMap<>();

    /**
     * 注册服务
     *
     * @param serviceName
     * @param implClass
     */
    public static void register(String serviceName, Class<?> implClass) {
        map.put(serviceName, implClass);
    }

    /**
     * 获取服务
     *
     * @param serviceKey
     * @return
     */
    public static Class<?> get(String serviceKey) {
        return map.get(serviceKey);
    }

    /**
     * 删除服务
     *
     * @param serviceKey
     */
    public static void remove(String serviceKey) {
        map.remove(serviceKey);
    }
}