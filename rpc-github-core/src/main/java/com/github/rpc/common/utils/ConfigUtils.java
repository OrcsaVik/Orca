package com.github.rpc.common.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.rpc.config.center.converter.ConfigConverter;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * 配置加载工具类
 * 支持从本地文件和配置中心加载配置
 */
public class ConfigUtils {

    /**
     * 从本地文件加载配置
     *
     * @param tClass 配置类
     * @param prefix 配置前缀
     * @param environment 环境
     * @param <T> 配置类型
     * @return 配置对象
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment) {
        // 构建配置文件名
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }

        // 尝试加载不同格式的配置文件
        // 1. 优先尝试加载YAML格式
        String yamlFileName = configFileBuilder.toString() + ".yaml";
        if (FileUtil.exist(yamlFileName)) {
            return loadYamlConfig(tClass, prefix, yamlFileName);
        }
        
        // 2. 尝试加载YML格式
        String ymlFileName = configFileBuilder.toString() + ".yml";
        if (FileUtil.exist(ymlFileName)) {
            return loadYamlConfig(tClass, prefix, ymlFileName);
        }
        
        // 3. 最后尝试加载Properties格式
        String propsFileName = configFileBuilder.toString() + ".properties";
        return loadPropertiesConfig(tClass, prefix, propsFileName);
    }
    
    /**
     * 加载YAML格式配置文件
     *
     * @param tClass 配置类
     * @param prefix 配置前缀
     * @param fileName 文件名
     * @param <T> 配置类型
     * @return 配置对象
     */
    private static <T> T loadYamlConfig(Class<T> tClass, String prefix, String fileName) {
        //存在解析错误
                try {
            //从对应yaml文件 文件名application-dev.yaml 加载对应文件
            String content = FileUtil.readUtf8String(fileName);
            //TODO
            return parseYamlConfig(tClass, content, prefix);
        } catch (Exception e) {
            throw new RuntimeException("加载YAML配置文件失败: " + fileName, e);
        }

    }
    
    /**
     * 加载Properties格式配置文件
     *
     * @param tClass 配置类
     * @param prefix 配置前缀
     * @param fileName 文件名
     * @param <T> 配置类型
     * @return 配置对象
     */
    private static <T> T loadPropertiesConfig(Class<T> tClass, String prefix, String fileName) {
        try {
            Props props = new Props(fileName);
            return props.toBean(tClass, prefix);
        } catch (Exception e) {
            throw new RuntimeException("加载Properties配置文件失败: " + fileName, e);
        }
    }
    
    /**
     * 解析YAML格式配置
     *
     * @param tClass 配置类
     * @param content YAML内容
     * @param prefix 配置前缀
     * @param <T> 配置类型
     * @return 配置对象
     */
    private static <T> T parseYamlConfig(Class<T> tClass, String content, String prefix) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            JsonNode rootNode = mapper.readTree(content);

            // 打印 YAML 内容和 prefix
            System.out.println("YAML内容:\n" + content);
            System.out.println("prefix: " + prefix);

            JsonNode targetNode = rootNode;
            if (prefix != null && !prefix.trim().isEmpty()) {
                String[] pathParts = prefix.split("\\.");
                for (String part : pathParts) {
                    targetNode = targetNode.get(part);
                    if (targetNode == null) {
                        throw new RuntimeException("YAML 配置中未找到路径: " + prefix);
                    }
                }
            }

            // 打印目标节点
            System.out.println("目标节点: " + targetNode);

            return mapper.treeToValue(targetNode, tClass);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("解析YAML配置失败", e);
        }
    }
    
    /**
     * 解析配置字符串为指定类型的对象
     *
     * @param tClass 配置类
     * @param content 配置内容
     * @param <T> 配置类型
     * @return 配置对象
     */
    public static <T> T parseConfig(Class<T> tClass, String content) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("配置内容不能为空");
        }
        
        // 根据内容格式判断使用哪种转换器 todo
        if (content.trim().startsWith("{") || content.trim().startsWith("[")) {
            // JSON格式
            com.github.rpc.config.center.converter.ConfigConverterFactory instance = com.github.rpc.config.center.converter.ConfigConverterFactory.getInstance();
            ConfigConverter<T> convertery = instance.getConverter(tClass);
            return convertery.convert(content);
        } else if (content.trim().startsWith("---") || content.contains(":")) {
            // YAML格式
            return parseYamlConfig(tClass, content, null);
        } else {
            // 尝试作为Properties格式解析
            try {
                Properties props = new Properties();
                props.load(new StringReader(content));
                
                // 使用Jackson将Properties转换为对象
                ObjectMapper mapper = new ObjectMapper();
                return mapper.convertValue(props, tClass);
            } catch (IOException e) {
                throw new RuntimeException("解析Properties配置失败", e);
            }
        }
    }


}