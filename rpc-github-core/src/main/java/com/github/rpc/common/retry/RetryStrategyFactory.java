package com.github.rpc.common.retry;

import com.github.rpc.common.retry.impl.GuavaRetryStrategy;
import com.github.rpc.common.spi.SpiLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.common.StringUtils;

import java.util.Objects;

@Slf4j
public class RetryStrategyFactory {

    static {
        SpiLoader.load(RetryStrategy.class);
    }

    /**
     * 默认重试器
     */
    private static final RetryStrategy DEFAULT_RETRY_STRATEGY = new GuavaRetryStrategy();

    /**
     *
     *      * 默认加载+动态选择
     *
     * @param key
     * @return
     */
    public static RetryStrategy getInstance(String key) {
        if (Objects.nonNull(key) && !StringUtils.isBlank(key)) {
            RetryStrategy strategy = SpiLoader.getInstance(RetryStrategy.class, key);
            if (strategy != null) return strategy;
            log.warn("未找到重试策略: {}, 使用默认", key);
        }
        return new GuavaRetryStrategy(); // 默认就是 Guava 实现
    }

}

