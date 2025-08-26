package com.github.rpc;

import com.github.rpc.common.registry.Registry;
import com.github.rpc.common.registry.RegistryFactory;
import com.github.rpc.common.utils.ConfigUtils;
import com.github.rpc.config.ConfigCenterConfig;
import com.github.rpc.config.GlobalRpcConfig;
import com.github.rpc.config.RegistryConfig;
import com.github.rpc.config.center.ConfigCenter;
import com.github.rpc.config.center.ConfigCenterFactory;
import com.github.rpc.constants.RpcLoadConstant;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RpcApplication {

    private static volatile GlobalRpcConfig rpcConfig;

    // 配置中心实例
    private static volatile ConfigCenter configCenter;

    // 加载对应环境的配置文件 初始为DEV
    private static String environment = "dev";
    
    // 配置文件前缀
    //    String DEFAULT_CONFIG_PREFIX = "rpc"; env
    private static final String CONFIG_PREFIX = RpcLoadConstant.DEFAULT_CONFIG_PREFIX;



    /**
     * 初始化RPC应用
     * 支持从环境变量或系统属性中获取环境名称
     */
    public static void init() {
        // 从环境变量或系统属性中获取环境名称
        String envFromSystem = System.getProperty("rpc.env");
        if (envFromSystem != null && !envFromSystem.isEmpty()) {
            environment = envFromSystem;
            log.info("从系统属性加载环境配置: {}", environment);
        }
        
        GlobalRpcConfig newRpcConfig;
        try {
            // 加载配置文件
            newRpcConfig = ConfigUtils.loadConfig(GlobalRpcConfig.class, CONFIG_PREFIX, environment);
        } catch (Exception e) {
            log.warn("配置加载失败，使用默认值: {}", e.getMessage());
            // 配置加载失败，使用默认值
            newRpcConfig = GlobalRpcConfig.builder().build();
        }
        init(newRpcConfig);
    }

    /**
     * 初始化RPC应用
     * 
     * @param newRpcConfig RPC配置
     */
    public static void init(GlobalRpcConfig newRpcConfig) {
        // 赋值给静态变量
        rpcConfig = newRpcConfig;
        log.info("RPC初始化, 配置 = {}", newRpcConfig);
        
        // 初始化配置中心
        initConfigCenter(newRpcConfig.getConfigCenterConfig());
        
        // 注册中心初始化
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("注册中心初始化, 配置 = {}", registryConfig);
        
        // 创建并注册 Shutdown Hook，JVM 退出时执行操作
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // 关闭注册中心
            registry.destroy();
            
            // 关闭配置中心
            if (configCenter != null) {
                configCenter.shutdown();
            }
        }));
    }
    
    /**
     * 初始化配置中心
     * 
     * @param config 配置中心配置
     */
    private static void initConfigCenter(ConfigCenterConfig config) {
        // 如果配置中心未启用，则跳过
        if (!config.isEnabled()) {
            log.info("配置中心未启用，跳过初始化");
            return;
        }
        
        try {
            // 获取配置中心实例 本地 nacos
            configCenter = ConfigCenterFactory.getInstance(config.getType());
            
            // 准备配置中心初始化参数
            Map<String, String> params = new HashMap<>();
            params.put("address", config.getAddress());
            params.put("namespace", config.getNamespace());
            params.put("group", config.getGroup());
            params.put("dataId", config.getDataId());
            params.put("username", config.getUsername());
            params.put("password", config.getPassword());
            params.put("timeout", String.valueOf(config.getTimeout()));
            
            // 初始化配置中心
            configCenter.init(params);
            log.info("配置中心初始化成功: type={}, address={}", config.getType(), config.getAddress());
            
            // 添加配置监听器，实现配置热加载
            setupConfigChangeListeners();
        } catch (Exception e) {
            log.error("配置中心初始化失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 设置配置变更监听器
     */
    private static void setupConfigChangeListeners() {
        if (configCenter == null) {
            return;
        }
        
        // 监听配置变更 rpc
        configCenter.addListener(CONFIG_PREFIX, configStr -> {
            try {
                log.info("检测到配置变更，准备更新RPC配置");

                //configStr 查看该类型 如果json
                
                // 解析新配置
                GlobalRpcConfig newConfig = ConfigUtils.parseConfig(GlobalRpcConfig.class, (String) configStr);
                
                // 更新配置
                updateConfig(newConfig);
                
                log.info("RPC配置热更新成功");
            } catch (Exception e) {
                log.error("RPC配置热更新失败: {}", e.getMessage(), e);
            }
        });
    }
    
    /**
     * 更新RPC配置
     * 
     * @param newConfig 新配置
     */
    private static void updateConfig(GlobalRpcConfig newConfig) {
        // 更新全局配置
        rpcConfig = newConfig;
        
        // 这里可以添加更多的配置更新逻辑
        // 例如更新序列化器、负载均衡器等
        
        // 通知配置变更监听器
        notifyConfigChangeListeners();
    }
    
    /**
     * 通知配置变更监听器
     */
    private static void notifyConfigChangeListeners() {
        // 这里可以添加通知逻辑，例如发布事件等
    }
    



    /**
     * 双重检查锁进行初始化 单例模式
     * @return RPC配置
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
    
    /**
     * 获取配置中心实例
     * 
     * @return 配置中心实例
     */
    public static ConfigCenter getConfigCenter() {
        if (configCenter == null && rpcConfig != null && rpcConfig.getConfigCenterConfig().isEnabled()) {
            synchronized (RpcApplication.class) {
                if (configCenter == null) {
                    initConfigCenter(rpcConfig.getConfigCenterConfig());
                }
            }
        }
        return configCenter;
    }
    
    /**
     * 设置环境变量
     * @param env 环境名称
     */
    public static void setEnvironment(String env) {
        if (env != null && !env.isEmpty()) {
            environment = env;
            log.info("环境已设置为: {}", environment);
        } else {
            log.warn("无效的环境名称: {}", env);
        }
    }

}