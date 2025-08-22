package com.github.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor

public enum ProtocolMessageTypeEnum {

    REQUEST(0),
    RESPONSE(1),
    HEART_BEAT(2),
    OTHERS(3);

    private final int type;


    public static ProtocolMessageTypeEnum valueOf(int code) {
        for (ProtocolMessageTypeEnum protocoxlMessageTypeEnum : ProtocolMessageTypeEnum.values()) {
            if (Objects.equals(code, protocoxlMessageTypeEnum.getType())) {
                return protocoxlMessageTypeEnum;
            }
        }



        return null;
    }
}