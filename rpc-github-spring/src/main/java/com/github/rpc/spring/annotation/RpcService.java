package com.github.rpc.spring.annotation;

import com.github.rpc.constants.RpcLoadConstant;
import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService {

    /**
     * 服务接口类
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 版本
     */
    String serviceVersion() default RpcLoadConstant.DEFAULT_SERVICE_VERSION;

    //分组 默认为空
    String serviceGroup() default "default";

    /**
     * 权重（用于负载均衡）
     */
    int weight() default 100;
}
