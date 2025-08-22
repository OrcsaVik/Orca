package com.github.rpc.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RpcReferenceConfig {
    private String serviceName;
    private String serviceVersion;
    private String loadBalancer;
    private String retryStrategy;
    private String tolerantStrategy;
    private boolean mock;
    // 可扩展：权重、延迟、区域等
}
