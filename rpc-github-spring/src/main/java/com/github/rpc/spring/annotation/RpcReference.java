package com.github.rpc.spring.annotation;

import com.github.rpc.constants.LoadBalancerConstant;
import com.github.rpc.constants.RetryStrategyConstant;
import com.github.rpc.constants.RpcLoadConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RpcReference {

    /**
     * 服务接口类
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 版本
     */
    String serviceVersion() default RpcLoadConstant.DEFAULT_SERVICE_VERSION;

    /**
     * 负载均衡器
     */
    String loadBalancer() default LoadBalancerConstant.RANDOM;

    /**
     * 重试策略
     */
    String retryStrategy() default RetryStrategyConstant.GUAVA;

    /**
     * 容错策略
     */
    String tolerantStrategy() default "failsafe";

    /**
     * 模拟调用
     */
    boolean mock() default false;

}

