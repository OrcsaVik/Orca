package com.github.rpc.config;

import com.github.rpc.constants.RegistryConstant;
import lombok.Getter;
import lombok.Setter;

/**
 * todo 配置中心一般连接参数
 */
@Setter
@Getter
public class RegistryConfig {

    /**
     * 注册中心类别
     */
    private String registry = RegistryConstant.ZOOKEEPER;

    /**
     * 注册中心地址 监听注册中心
     */
    private String address = "http://localhost:2181";

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 超时时间（单位毫秒）
     */
    private Long timeout = 5000L;
}
