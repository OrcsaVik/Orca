package com.github.rpc.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum ProtocolMessageDTOSerializerEnum {

    JDK(0, "jdk"),
    JSON(1, "json"),
    KRYO(2, "kryo"),
    HESSIAN(3, "hessian");

    private final Integer type;

    private final String value;



//    /**
//     * 获取值列表
//     *
//     * @return
//     */
//    public static List<String> getValues() {
//        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
//    }

    /**
     * 根据 key 获取枚举
     *
     * @param key
     * @return
     */
    public static ProtocolMessageDTOSerializerEnum getEnumByKey(int key) {
        for (ProtocolMessageDTOSerializerEnum anEnum : ProtocolMessageDTOSerializerEnum.values()) {
            if (Objects.equals(anEnum.type, key)) {
                return anEnum;
            }
        }
        return null;
    }


    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static ProtocolMessageDTOSerializerEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (ProtocolMessageDTOSerializerEnum anEnum : ProtocolMessageDTOSerializerEnum.values()) {
            if (Objects.equals(anEnum.value, value)) {
                return anEnum;
            }
        }
        return null;
    }
}