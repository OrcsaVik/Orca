# 配置中心使用指南

## 概述

配置中心是RPC框架的一个重要组件，用于支持动态配置热加载机制。通过配置中心，RPC框架可以在启动后实时感知配置变更，自动刷新服务行为，无需重启应用。

主要特性：

- 实时感知配置变更（如超时、路由、限流规则）
- 自动刷新服务行为（无需重启）
- 支持多格式配置（YAML/JSON/Properties）
- 通过SPI扩展灵活接入不同配置源
- 基于环境参数（如nacos:dev）动态选择配置源

## 快速开始

### 1. 添加配置中心配置

在`application.properties`或`application.yml`中添加配置中心相关配置：

```properties
# 配置中心配置
rpc.configCenterConfig.enabled=true
rpc.configCenterConfig.type=nacos
rpc.configCenterConfig.address=localhost:8848
rpc.configCenterConfig.namespace=public
rpc.configCenterConfig.group=DEFAULT_GROUP
rpc.configCenterConfig.dataId=rpc-config
rpc.configCenterConfig.username=nacos
rpc.configCenterConfig.password=nacos
rpc.configCenterConfig.timeout=3000
```

### 2. 设置环境参数

通过设置环境参数来选择配置源：

```java
// 设置环境为nacos
RpcApplication.setEnvironment("nacos");

// 初始化RPC应用
RpcApplication.init();
```

### 3. 添加配置监听器

```java
// 获取配置中心实例
ConfigCenter configCenter = RpcApplication.getConfigCenter();

// 添加配置监听器
configCenter.addListener("custom.config.key", value -> {
    System.out.println("配置变更: custom.config.key = " + value);
});
```

## 支持的配置中心

### Nacos配置中心

Nacos配置中心适配器支持从Nacos服务器获取配置，并实时监听配置变更。

```java
// 创建Nacos配置中心实例
ConfigCenter nacosConfigCenter = ConfigCenterFactory.getInstance("nacos");

// 准备初始化参数
Map<String, String> params = new HashMap<>();
params.put("address", "localhost:8848");
params.put("namespace", "public");
params.put("group", "DEFAULT_GROUP");
params.put("dataId", "custom-config");

// 初始化配置中心
nacosConfigCenter.init(params);
```

### 本地文件配置中心

本地文件配置中心适配器支持从本地文件读取配置，并监听文件变更。

```java
// 创建本地文件配置中心实例
ConfigCenter localConfigCenter = ConfigCenterFactory.getInstance("local");

// 准备初始化参数
Map<String, String> params = new HashMap<>();
params.put("filePath", "config/application.properties");

// 初始化配置中心
localConfigCenter.init(params);
```

## 配置格式支持

配置中心支持多种配置格式：

- YAML格式：使用`YamlConfigConverter`进行转换
- JSON格式：使用`JsonConfigConverter`进行转换
- Properties格式：使用`PropertiesConfigConverter`进行转换

## 扩展配置中心

通过SPI机制，可以灵活扩展配置中心实现：

1. 实现`ConfigCenter`接口或继承`AbstractConfigCenter`抽象类
2. 在`META-INF/services/com.github.rpc.config.center.ConfigCenter`文件中添加实现类的全限定名

```java
public class CustomConfigCenter extends AbstractConfigCenter {
    @Override
    public void init(Map<String, String> configProperties) {
        // 初始化逻辑
    }

    @Override
    public String getConfig(String key) {
        // 获取配置逻辑
        return "value";
    }

    @Override
    protected void doRemoveListener(String key) {
        // 移除监听器逻辑
    }

    @Override
    public void shutdown() {
        // 关闭逻辑
    }
}
```

## 最佳实践

1. 使用环境参数动态选择配置源，如开发环境使用本地文件，生产环境使用Nacos
2. 为关键配置添加监听器，实时响应配置变更
3. 合理设置配置中心超时时间，避免初始化过程阻塞过长时间
4. 在应用关闭时调用`shutdown()`方法，释放配置中心资源