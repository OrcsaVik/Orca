package com.github.rpc.enums;

import com.github.rpc.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("RPC-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("RPC-10001", "参数错误"),


    // ----------- 业务异常状态码 -----------
    NOT_VALID_MESSAGE_MAGIC("RPC-20000", "<\"消息 magic 非法\">"),
    NOT_VALID_MESSAGE_SERIALIZER("RPC-20001", "序列化消息的协议不存在"),
    NOT_VALID_MESSAGE_TYPE("RPC-20002", "序列化消息的类型不存在"),
    // 新增枚举项

    UNSUPPORTED_MESSAGE_TYPE("RPC-20003", "暂不支持该消息类型"),
    SPI_NOT_FOUND("RPC-20004", "SpiLoader 未加载 服务类型 -->"),
    INSTANCE_INIT_ERROR("RPC-20005", "服务类实例化失败-->")
    ;
    // 新增枚举项




    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;

}
