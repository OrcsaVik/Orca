package com.github.rpc.common.loadblanace;

import com.github.rpc.common.loadblanace.impl.RandomLoadBalancer;
import com.github.rpc.common.spi.SpiLoader;
import io.grpc.LoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.common.StringUtils;

import java.util.Objects;

@Slf4j
public class LoadBalancerFactory {

    static {
        SpiLoader.load(LoadBalancerFactory.class);
    }

    /**
     * 默认负载均衡器
     */
    private static final LoadBalancerStrategy DEFAULT_LOAD_BALANCER = new RandomLoadBalancer();

    /**
     * 默认加载+动态选择
     *
     * @param key
     * @return
     */
    public static LoadBalancerStrategy getInstance(String key) {
        // 1. 参数校验：使用 Objects 和 StringUtils
        if (Objects.nonNull(key) && !StringUtils.isBlank(key)) {
            LoadBalancerStrategy loadBalancer = SpiLoader.getInstance(LoadBalancer.class, key);
            if (loadBalancer != null) {
                log.debug("使用 SPI 加载负载均衡器: key='{}', 实现类={}", key, loadBalancer.getClass().getSimpleName());
                return loadBalancer;
            } else {
                log.warn("未找到指定的负载均衡器实现: key='{}'，将使用默认实现", key);
            }
        } else {
            log.info("负载均衡器类型未配置（key={}），将使用默认实现: RandomLoadBalancer", key);
        }

        // 2. SPI 加载失败或 key 无效，返回默认实现
        return DEFAULT_LOAD_BALANCER;
    }
}

