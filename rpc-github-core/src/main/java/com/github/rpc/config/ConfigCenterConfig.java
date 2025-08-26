package com.github.rpc.config;

import lombok.Data;

/**
 * 配置中心配置
 */
@Data
public class ConfigCenterConfig {

    /**
     * 配置中心类型，默认为本地文件
     */
    private String type = "local";

    /**
     * 配置中心地址
     */
    private String address = "http://localhost:8848";

    /**
     * 命名空间
     */
    private String namespace = "public";

    /**
     * 分组
     */
    private String group = "DEFAULT_GROUP";

    /**
     * 数据ID
     */
    private String dataId = "rpc-config";

    /**
     * 用户名
     */
    private String username = "";

    /**
     * 密码
     */
    private String password = "";

    /**
     * 超时时间（毫秒）
     */
    private int timeout = 5000;

    /**
     * 是否启用配置中心
     */
    private boolean enabled = false;
}