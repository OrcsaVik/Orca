package com.github.rpc.example;

import com.github.rpc.RpcApplication;
import com.github.rpc.config.GlobalRpcConfig;
import com.github.rpc.config.center.ConfigCenter;
import com.github.rpc.config.center.ConfigCenterFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * 配置中心使用示例
 */
@Slf4j
public class ConfigCenterExample {
    //TODO 作为测试 对应配置文件application-nacos.properties
    public static void main(String[] args) throws Exception {
        // 加载 application-nacos.properties 配置文件
        Properties properties = new Properties();
        try (InputStream input = ConfigCenterExample.class.getClassLoader().getResourceAsStream("application-nacos.properties")) {
            if (input == null) {
                log.error("无法找到配置文件 application-nacos.properties");
                return;
            }
            properties.load(input);
            log.info("加载的配置文件内容: {}", properties);
        } catch (IOException e) {
            log.error("加载配置文件失败: {}", e.getMessage(), e);
            return;
        }

        // 设置环境为 nacos
        RpcApplication.setEnvironment("nacos");

        // 初始化 RPC 应用
        RpcApplication.init();

        // 获取全局配置
        GlobalRpcConfig config = RpcApplication.getRpcConfig();
        log.info("初始配置: {}", config);

        // 获取配置中心实例
        ConfigCenter configCenter = RpcApplication.getConfigCenter();
        if (configCenter == null) {
            log.error("配置中心未初始化，请检查配置");
            return;
        }

        // 添加配置监听器
        configCenter.addListener("custom.config.key", value -> log.info("配置变更: custom.config.key = {}", value));

        // 手动创建配置中心实例
        manualConfigCenterExample();

        // 等待配置变更
        log.info("等待配置变更，请在 Nacos 控制台修改配置...");
        TimeUnit.MINUTES.sleep(5);
    }

    /**
     * 手动创建配置中心示例
     */
    private static void manualConfigCenterExample() {
        try {
            // 创建Nacos配置中心实例
            ConfigCenter nacosConfigCenter = ConfigCenterFactory.getInstance("nacos");

            // 准备初始化参数
            Map<String, String> params = new HashMap<>();
            params.put("address", "localhost:8848");
            params.put("namespace", "public");
            params.put("group", "DEFAULT_GROUP");
            params.put("dataId", "custom-config");

            // 初始化配置中心
            nacosConfigCenter.init(params);

            // 获取配置
            String value = nacosConfigCenter.getConfig("custom.key");
            log.info("从Nacos获取配置: custom.key = {}", value);

            // 添加配置监听器
            nacosConfigCenter.addListener("custom.key", newValue -> log.info("配置变更: custom.key = {}", newValue));

            // 获取配置并转换为指定类型
            Integer intValue = nacosConfigCenter.getConfig("custom.int.key", Integer.class);
            log.info("从Nacos获取配置并转换为Integer: custom.int.key = {}", intValue);

            // 使用完毕后关闭
            // nacosConfigCenter.shutdown();
        } catch (Exception e) {
            log.error("手动创建配置中心示例失败: {}", e.getMessage(), e);
        }


    }
}