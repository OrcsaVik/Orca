package com.github.rpc.config.center.converter;

/**
 * 双精度浮点数配置转换器
 */
public class DoubleConfigConverter implements ConfigConverter<Double> {

    @Override
    public Double convert(String value) {
        if (value == null) {
            return null;
        }
        return Double.parseDouble(value);
    }

    @Override
    public Class<Double> getType() {
        return Double.class;
    }
}