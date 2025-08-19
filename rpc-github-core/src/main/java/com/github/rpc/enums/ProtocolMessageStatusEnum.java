package com.github.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum ProtocolMessageStatusEnum {

    OK("ok", 20),
    BAD_REQUEST("badRequest", 401),
    BAD_RESPONSE("badResponse", 402);

    private final String text;

    private final Integer value;


    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static ProtocolMessageStatusEnum valueOf(int value) {
        for (ProtocolMessageStatusEnum protocolMessageStatusEnum : ProtocolMessageStatusEnum.values()) {
            if (Objects.equals(protocolMessageStatusEnum.value, value)) {
                return protocolMessageStatusEnum;
            }
        }
        return null;
    }
}