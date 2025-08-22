package com.github.rpc.common.proxy;

import com.github.rpc.RpcApplication;
import com.github.rpc.config.GlobalRpcConfig;
import com.github.rpc.config.RpcReferenceConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Proxy;

@Slf4j
public class ServiceProxyFactory {

    /**
     * 根据接口和注解配置生成代理对象
     */
    public static <T> T getProxy(Class<T> interfaceClass, RpcReference rpcReference) {
        // 1. 构建最终的引用配置（全局配置 + 注解覆盖）
        RpcReferenceConfig config = buildConfig(interfaceClass, rpcReference);

        // 2. 创建代理处理器
        ServiceProxy serviceProxy = new ServiceProxy();
        serviceProxy.setConfig(config); // 注入合并后的配置

        // 3. 创建动态代理
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                serviceProxy
        );
    }

    /**
     * 合并全局配置与注解配置
     */
    private static RpcReferenceConfig buildConfig(Class<?> interfaceClass, RpcReference rpcReference) {
        GlobalRpcConfig globalConfig = RpcApplication.getRpcConfig();

        return RpcReferenceConfig.builder()
                .serviceName(interfaceClass.getName())
                .serviceVersion(StringUtils.defaultIfBlank(rpcReference.serviceVersion(), globalConfig.getVersion()))
                .loadBalancer(StringUtils.defaultIfBlank(rpcReference.loadBalancer(), globalConfig.getLoadBalancer()))
                .retryStrategy(StringUtils.defaultIfBlank(rpcReference.retryStrategy(), globalConfig.getRetryStrategy()))
                .tolerantStrategy(StringUtils.defaultIfBlank(rpcReference.tolerantStrategy(), globalConfig.getTolerantStrategy()))
                .mock(rpcReference.mock() || globalConfig.isMock()) // mock 可来自全局或注解
                .build();
    }
}