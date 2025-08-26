package com.github.rpc.config.center;

import com.github.rpc.common.spi.SpiLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * 配置中心工厂类
 * 负责创建和管理配置中心实例
 */
@Slf4j
public class ConfigCenterFactory {

    static {
        // 预加载 ConfigCenter 的 SPI 映射，避免首次获取时未加载导致异常
        SpiLoader.load(ConfigCenter.class);
    }

    private ConfigCenterFactory() {
    }

    /**
     * 获取配置中心实例
     *
     * @param type 配置中心类型
     * @return 配置中心实例
     */
    public static ConfigCenter getInstance(String type) {
        // 若未配置类型，默认使用本地文件配置中心
        if (type == null || type.trim().isEmpty()) {
            log.info("配置中心类型未配置，将使用本地文件配置中心");
            type = "local";
        }
        try {
            // 通过 SPI 加载配置中心实现
            return SpiLoader.getInstance(ConfigCenter.class, type);
        } catch (Exception e) {
            log.warn("加载配置中心失败: type={}, 将使用本地文件配置中心", type, e);
            // 默认使用本地文件配置中心
            return SpiLoader.getInstance(ConfigCenter.class, "local");
        }
    }
}