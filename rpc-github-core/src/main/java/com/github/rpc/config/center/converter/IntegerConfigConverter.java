package com.github.rpc.config.center.converter;

/**
 * 整数配置转换器
 */
public class IntegerConfigConverter implements ConfigConverter<Integer> {

    @Override
    public Integer convert(String value) {
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value);
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }
}