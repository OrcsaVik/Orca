package com.github.rpc.spring.bootstrap;

import com.github.rpc.RpcApplication;
import com.github.rpc.config.GlobalRpcConfig;
import com.github.rpc.service.web.VertxHttpServer;
import com.github.rpc.spring.annotation.EnableRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

@Slf4j
//Spring初始化之后进行处理
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {

    /**
     * Spring 初始化时执行，初始化 RPC 框架
     * 主要加载逻辑
     * @param importingClassMetadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获取 EnableRpc 注解的属性值
        boolean needServer = (boolean) importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName())
                .get("needServer");

        // RPC 框架初始化（配置和注册中心）
        RpcApplication.init();

        // 全局配置
        final GlobalRpcConfig rpcConfig = RpcApplication.getRpcConfig();

        // 启动服务器
        if (needServer) {
            //VertxHttpServer
            VertxHttpServer server = new VertxHttpServer();
            server.doStart(rpcConfig.getServerPort());
            log.info("VertxHttpServer started on port {}", rpcConfig.getServerPort());
        } else {
            log.warn("默认不启动Server服务");
        }

    }
}
