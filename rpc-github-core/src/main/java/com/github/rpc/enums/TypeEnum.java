package com.github.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeEnum {
    // 请求
    Request(0),
    // 响应
    Response(1);

    private final Integer value;
}
