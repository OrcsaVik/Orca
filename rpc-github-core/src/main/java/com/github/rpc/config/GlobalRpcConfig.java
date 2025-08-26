package com.github.rpc.config;

import com.github.rpc.constants.LoadBalancerConstant;
import com.github.rpc.constants.RetryStrategyConstant;
import com.github.rpc.constants.SerializerStrategyConstant;
import com.github.rpc.constants.TolerantStrategyConstant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GlobalRpcConfig {


    private String name = "rpc-github-lhx";

    /**
     * 版本号
     */
    private String version = "1.0-SNAPSHOT";

    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;

    /**
     * 序列化器
     */
    private String serializer = SerializerStrategyConstant.KRYO;

    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerConstant.RANDOM;

    /**
     * 重试策略
     */
    private String retryStrategy = RetryStrategyConstant.GUAVA;

    /**
     * 容错策略
     */
    private String tolerantStrategy = TolerantStrategyConstant.FAIL_FAST;

    /**
     * 模拟调用
     */
    private boolean mock = false;

    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();
    
    /**
     * 配置中心配置
     */
    private ConfigCenterConfig configCenterConfig = new ConfigCenterConfig();


    /**
     * 重试次数
     */
    private Integer maxRetryAttempts = 5;

    /**
     * 重试间隔
     */
    private Long retryInterval = 200L;
}
