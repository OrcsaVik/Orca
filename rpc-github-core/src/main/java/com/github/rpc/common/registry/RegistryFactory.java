package com.github.rpc.common.registry;

import com.github.rpc.common.spi.SpiLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.common.StringUtils;

import java.util.Objects;

@Slf4j
public class RegistryFactory {

    // SPI 动态加载
    static {
        SpiLoader.load(Registry.class);
    }

    /**
     * 默认注册中心
     */
    private static final Registry DEFAULT_REGISTRY = new ZooKeeperRegistry();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static Registry getInstance(String key) {

        if (Objects.nonNull(key) && !StringUtils.isBlank(key)) {
            Registry registry = SpiLoader.getInstance(Registry.class, key);
            if (registry != null) {
                return registry;
            }
        }
        log.info("注册中心类型未配置，将使用默认实现: {}", DEFAULT_REGISTRY.getClass().getSimpleName());
        // SPI 加载失败或 key 无效，返回默认实现
        return DEFAULT_REGISTRY;
    }

}

