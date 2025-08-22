package com.github.rpc.spring.bootstrap;

import com.github.rpc.RpcApplication;
import com.github.rpc.common.registry.Registry;
import com.github.rpc.common.registry.RegistryFactory;
import com.github.rpc.common.registry.localcache.LocalRegistry;
import com.github.rpc.config.GlobalRpcConfig;
import com.github.rpc.model.dto.ServiceMetaInfoDTO;
import com.github.rpc.spring.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

@Slf4j
//bean初始化进行处理
public class RpcProviderBootstrap implements BeanPostProcessor {

    /**
     * Bean 初始化后执行，注册服务 每个bean处理逻辑
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);

        if (rpcService != null) {
            log.info("发现 RPC 服务: {} -> {}", beanName, beanClass.getSimpleName());

            // 1. 获取服务接口类型
            Class<?> interfaceClass = rpcService.interfaceClass();

            // 如果未指定 interfaceClass，默认取第一个接口
            if (interfaceClass == void.class) {
                Class<?>[] interfaces = beanClass.getInterfaces();
                if (interfaces.length == 0) {
                    log.error("【RPC 服务注册失败】服务 {} 没有实现任何接口，无法导出 RPC 服务", beanName);
                    throw new IllegalArgumentException("RPC 服务必须实现至少一个接口");
                }
                interfaceClass = interfaces[0];
                log.warn("服务 {} 未指定 interfaceClass，自动使用第一个接口: {}", beanName, interfaceClass.getName());
            }

            // 强制要求接口存在（您原来的逻辑）
            if (interfaceClass == null) {
                log.error("【RPC 服务注册失败】interfaceClass 为 null，服务: {}", beanName);
                throw new IllegalArgumentException("interfaceClass 不能为空");
            }

            // 2. 获取服务元信息（版本、分组、超时等）
            String serviceVersion = rpcService.serviceVersion();
            String serviceGroup = rpcService.serviceGroup();

            String serviceKey = ServiceMetaInfoDTO.buildKey(interfaceClass.getName(), serviceGroup, serviceVersion);


            try {
                // 3. 注册到本地注册表（用于本地调用）
                LocalRegistry.register(serviceKey, beanClass);
                log.info("✅ 本地注册 RPC 服务: {} -> {}", serviceKey, beanClass.getName());

                // 4. 注册到注册中心（如 Nacos/ZooKeeper）
                GlobalRpcConfig rpcConfig = RpcApplication.getRpcConfig();
                Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
                //
                ServiceMetaInfoDTO serviceMetaInfo = new ServiceMetaInfoDTO();
                serviceMetaInfo.setServiceName(interfaceClass.getName());
                serviceMetaInfo.setServiceVersion(serviceVersion);
                serviceMetaInfo.setServiceGroup(serviceGroup);
                serviceMetaInfo.setServiceHost(rpcConfig.getServerHost()); // 本地 IP
                serviceMetaInfo.setServicePort(rpcConfig.getServerPort()); // 服务端口
                serviceMetaInfo.setWeight(rpcService.weight());
                //不设置序列化器 因为传输信息 包含序列化器信息 自动处理
                registry.register(serviceMetaInfo);
                log.info("🌐 已注册服务到注册中心: {}", serviceMetaInfo);

            } catch (Exception e) {
                log.error("【RPC 服务注册失败】服务: {}", serviceKey, e);
                throw new RuntimeException("Failed to register RPC service: " + serviceKey, e);
            }
        }

        // 返回原始 bean（Spring 容器继续使用）
        return bean;
    }
}
