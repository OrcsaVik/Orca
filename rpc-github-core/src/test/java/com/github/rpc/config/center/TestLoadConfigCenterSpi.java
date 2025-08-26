package com.github.rpc.config.center;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


public class TestLoadConfigCenterSpi {
    @Test
    public void testLoadConfigCenterSpi2() throws Exception {
        // 准备测试数据
        String configCenterServiceFile = "META-INF/rpc/system/com.github.rpc.config.center.ConfigCenter";
        URL resource = ResourceUtil.getResource(configCenterServiceFile);

        assertNotNull("SPI 配置文件不存在，请检查路径和文件名", resource);

        InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        boolean foundImpl = false;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.trim().startsWith("nacos=")) {
                String className = line.split("=")[1].trim();
                Class<?> clazz = Class.forName(className);
                assertNotNull("Nacos 配置中心实现类加载失败", clazz);
                System.out.println(JSONUtil.toJsonStr(clazz));
                foundImpl = true;
                break;
            }
        }
        assertTrue("未在 SPI 文件中找到 nacos 实现配置", foundImpl);
    }

    @Test
    public void testLoadConfigCenterSpi() throws Exception {
        String spiFile = "META-INF/rpc/system/com.github.rpc.config.center.ConfigCenter";
        URL resource = ResourceUtil.getResource(spiFile);

        assertNotNull("SPI 配置文件不存在", resource);

        List<String> classNames = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    classNames.add(line);
                    Class<?> clazz = Class.forName(line);
                    assertNotNull("类加载失败: " + line, clazz);
                }
            }
        }

        // 非 JSON 格式化输出类信息
        System.out.println("=== SPI 配置文件加载的类信息 ===");
        System.out.println("文件路径: " + spiFile);
        System.out.println("发现 " + classNames.size() + " 个实现类:");
        for (int i = 0; i < classNames.size(); i++) {
            String className = classNames.get(i);
            try {
                Class<?> clazz = Class.forName(className);
                Package pkg = clazz.getPackage();
                System.out.printf("%d. %s%n", i + 1, className);
                System.out.printf("   包名: %s%n", pkg != null ? pkg.getName() : "<default>");
                System.out.printf("   简单类名: %s%n", clazz.getSimpleName());
                System.out.printf("   是否为接口: %s%n", clazz.isInterface());
                System.out.printf("   是否为抽象类: %s%n", Modifier.isAbstract(clazz.getModifiers()));
            } catch (Exception e) {
                System.out.printf("%d. %s (加载信息读取失败: %s)%n", i + 1, className, e.getMessage());
            }
        }
        System.out.println("==============================");

        // 断言至少加载一个类
        assertFalse("SPI 文件中未找到任何有效实现类", classNames.isEmpty());
    }

}
