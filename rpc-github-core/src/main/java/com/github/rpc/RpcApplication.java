package com.github.rpc;

import com.github.rpc.common.registry.Registry;
import com.github.rpc.common.registry.RegistryFactory;
import com.github.rpc.common.utils.ConfigUtils;
import com.github.rpc.config.GlobalRpcConfig;
import com.github.rpc.config.RegistryConfig;
import com.github.rpc.constants.RpcLoadConstant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//todo handlerRPC
public class RpcApplication {

    private static volatile GlobalRpcConfig rpcConfig;

    //加载对应环境的配置文件 初始为DEV
    private static final String environment = "dev";



    public static void init() {
        GlobalRpcConfig newRpcConfig;
        try {
            //rpc作为前缀 加载配置文件
            newRpcConfig = ConfigUtils.loadConfig(GlobalRpcConfig.class, RpcLoadConstant.DEFAULT_CONFIG_PREFIX, environment);
        } catch (Exception e) {
            // 配置加载失败，使用默认值
            newRpcConfig = new GlobalRpcConfig();
        }
        init(newRpcConfig);
    }

    //开启注册
    public static void init(GlobalRpcConfig newRpcConfig) {

        //赋值给静态变量
        rpcConfig = newRpcConfig;
        log.info("rpc init, config = {}", newRpcConfig.toString());
        // 注册中心初始化
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("registry init, config = {}", registryConfig);
        // 创建并注册 Shutdown Hook，JVM 退出时执行操作
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }



    /**
     * 双从检查进行初始化 单例模式
     * @return
     */
    public static GlobalRpcConfig getRpcConfig() {
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }
}