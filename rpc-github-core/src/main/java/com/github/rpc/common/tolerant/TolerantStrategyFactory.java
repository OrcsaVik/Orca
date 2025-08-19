package com.github.rpc.common.tolerant;

import com.github.rpc.common.spi.SpiLoader;
import com.github.rpc.common.tolerant.impl.FailSafeTolerantStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.common.StringUtils;

import java.util.Objects;

@Slf4j
public class TolerantStrategyFactory {

    static {
        SpiLoader.load(TolerantStrategy.class);
    }

    /**
     * 默认容错策略
     */
    private static final TolerantStrategy DEFAULT_TOLERANT_STRATEGY = new FailSafeTolerantStrategy();

    /**
     *  * 默认加载+动态选择
     *
     * @param key
     * @return
     */
    public static TolerantStrategy getInstance(String key) {
        // 参数校验 + SPI 加载
        if (Objects.nonNull(key) && !StringUtils.isBlank(key)) {
            TolerantStrategy strategy = SpiLoader.getInstance(TolerantStrategy.class, key);
            if (strategy != null) {
                return strategy;
            }
            log.warn("未找到容错策略实现: key='{}'，将使用默认策略: {}", key, DEFAULT_TOLERANT_STRATEGY.getClass().getSimpleName());
        } else {
            log.info("容错策略未配置，将使用默认策略: {}", DEFAULT_TOLERANT_STRATEGY.getClass().getSimpleName());
        }
        return DEFAULT_TOLERANT_STRATEGY;
    }

}
