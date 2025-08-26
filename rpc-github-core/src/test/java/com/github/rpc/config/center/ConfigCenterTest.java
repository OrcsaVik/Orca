package com.github.rpc.config.center;

import com.github.rpc.config.center.impl.LocalFileConfigCenter;
import com.github.rpc.config.center.impl.NacosConfigCenter;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.Assert.*;

/**
 * 配置中心单元测试
 */
public class ConfigCenterTest {

    @Test
    public void testLocalFileConfigCenter() throws Exception {
        // 创建本地文件配置中心
        ConfigCenter configCenter = new LocalFileConfigCenter();

        // 准备初始化参数
        Map<String, String> params = new HashMap<>();
        params.put("filePath", "src/test/resources/test-config.properties");

        // 初始化配置中心
        configCenter.init(params);

        // 测试获取配置
        String value = configCenter.getConfig("test.key");
        assertEquals("test-value", value);

        // 测试获取配置并转换为指定类型
        Integer intValue = configCenter.getConfig("test.int.key", Integer.class);
        assertEquals((Integer) 123, intValue);

        // 测试配置监听器
        AtomicReference<Object> newValue = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        configCenter.addListener("test.key", value1 -> {
            newValue.set(value1);
            latch.countDown();
        });

        // 等待配置变更
        boolean received = latch.await(100, TimeUnit.MILLISECONDS);

        // 关闭配置中心
        configCenter.shutdown();

        // 验证结果
        if (received) {
            assertEquals("test-value", newValue.get());
        }
    }

    @Test
    public void testConfigCenterFactory() {
        // 测试获取本地文件配置中心
        ConfigCenter localConfigCenter = ConfigCenterFactory.getInstance("local");
        assertNotNull(localConfigCenter);
        assertTrue(localConfigCenter instanceof LocalFileConfigCenter);

        // 测试获取Nacos配置中心
        ConfigCenter nacosConfigCenter = ConfigCenterFactory.getInstance("nacos");
        assertNotNull(nacosConfigCenter);
        assertTrue(nacosConfigCenter instanceof NacosConfigCenter);

        // 测试获取不存在的配置中心类型，应该返回默认的本地文件配置中心
        ConfigCenter defaultConfigCenter = ConfigCenterFactory.getInstance("not-exist");
        assertNotNull(defaultConfigCenter);
        assertTrue(defaultConfigCenter instanceof LocalFileConfigCenter);
    }

    @Test
    public void testAbstractConfigCenter() {
        // 创建测试用的配置中心实现
        TestConfigCenter configCenter = new TestConfigCenter();

        // 测试添加和移除监听器
        //测试添加和移除监听器
        Consumer<String> listener = value -> {
            // 监听器逻辑
        };
        configCenter.addListener("test.key", listener);
        assertEquals(1, configCenter.getListenerCount("test.key"));

        configCenter.removeListener("test.key");
        assertEquals(0, configCenter.getListenerCount("test.key"));
    }


    /**
     * 测试用的配置中心实现
     */
    static class TestConfigCenter extends AbstractConfigCenter {
        @Override
        public void init(Map<String, String> configProperties) {
        }

        @Override
        public String getConfig(String key) {
            return "test-value";
        }

        @Override
        protected void doRemoveListener(String key) {
        }

        @Override
        public void shutdown() {
        }

        public int getListenerCount(String key) {
            return listenerMap.containsKey(key) ? listenerMap.get(key).size() : 0;
        }

        public <T> void addListener(String key, Consumer<T> listener) {
            listenerMap.computeIfAbsent(key, k -> new ArrayList<>()).add((Consumer<Object>) listener);
        }
    }

//    @Test
//    public void testYamlEncoding() throws IOException {
//        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application-dev.yml")) {
//            Assert.assertNotNull("无法找到配置文件 application-dev.yml", is);
//
////            is.mark(4096); // 标记足够大，防止reset失效
////            byte[] bom = new byte[3];
////            is.read(bom, 0, 3);
////            if (!(bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF)) {
////                is.reset();
////            }
//
//            StringBuilder content = new StringBuilder();
//            int ch;
//            while ((ch = is.read()) != -1) {
//                // 过滤掉所有非法控制字符（仅保留常用可见字符和换行、回车、制表符）
//                if (ch == 0x09 || ch == 0x0A || ch == 0x0D || ch >= 0x20) {
//                    content.append((char) ch);
//                }
//            }
//
//            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
//            JsonNode node = mapper.readTree(content.toString());
//            Assert.assertNotNull("YAML 文件解析失败", node);
//        }
//    }

    @Test
    public void testLoadYamlConfig() throws Exception {
        // 加载 application-dev.yml 配置文件
        Yaml yaml = new Yaml();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application-dev.yml")) {
            if (input == null) {
                fail("无法找到配置文件 application-dev.yml");
            }

            Map<String, Object> config = yaml.load(input);
            assertNotNull(config.toString(), "配置文件加载失败");

            // 验证部分配置内容
            assertEquals("rpc-github-lhx", ((Map<String, Object>) config.get("rpc")).get("name"));
            assertEquals(8080, ((Map<String, Object>) config.get("rpc")).get("serverPort"));
        }
    }

//    @Test
//    public void testYamlFileLoad() throws IOException {
//        // 验证 application-dev.yml 是否能被正常加载
//        String fileName = "src/main/resources/application-dev.yml";
//        String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(fileName)), java.nio.charset.StandardCharsets.UTF_8);
//        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
//        JsonNode rootNode = mapper.readTree(content);
//        assertNotNull("YAML根节点不应为null", rootNode);
//        assertEquals("rpc-github-lhx", rootNode.get("rpc").get("name").asText());
//    }
}