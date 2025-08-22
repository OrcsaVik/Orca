package com.github.rpc.model.dto;

import cn.hutool.core.util.StrUtil;
import com.github.rpc.constants.RpcLoadConstant;
import lombok.Data;

@Data
public class ServiceMetaInfoDTO {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务版本号
     */
    private String serviceVersion = RpcLoadConstant.DEFAULT_SERVICE_VERSION;

    /**
     * 服务域名
     */
    private String serviceHost;

    /**
     * 服务端口号
     */
    private Integer servicePort;

    //权重
    private Integer weight;

    /**
     * 服务分组（暂未实现）
     */
    private String serviceGroup = "default";

    /**
     * 获取服务键名
     *
     * @return
     */
    public String getServiceKey() {
        // 后续可扩展服务分组
//        return String.format("%s:%s:%s", serviceName, serviceVersion, serviceGroup);
        return buildKey(serviceName, serviceVersion, serviceGroup);
    }

    /**
     * 获取服务注册节点键名
     * 服务名/主机IP：端口
     * @return
     */
    public String getServiceNodeKey() {
        return String.format("%s/%s:%s", getServiceKey(), serviceHost, servicePort);
    }

    /**
     * 获取完整服务地址
     *
     * @return
     */
    public String getServiceAddress() {
        if (!StrUtil.contains(serviceHost, "http")) {
            return String.format("http://%s:%s", serviceHost, servicePort);
        }
        return String.format("%s:%s", serviceHost, servicePort);
    }

    //注册服务键值
    public static String buildKey(String serviceName, String serviceGroup, String serviceVersion) {
        return String.format("%s:%s:%s", serviceName, serviceGroup, serviceVersion);
    }
}


