package com.github.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 1L;


    private static final String DEFAULT_SERVICE_VERSION = "1.0-SNAPSHOT";

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 服务版本
     */
    private String serviceVersion = DEFAULT_SERVICE_VERSION;

    /**
     * 参数类型列表
     * 便于进行反序列化进行转化类型
     */
    private Class<?>[] parameterTypes;

    /**
     * 参数列表
     */
    private Object[] args;

}
