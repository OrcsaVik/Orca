# Orca
一个从0到1自主研发的轻量式RPC框架，支持服务注册发现、负载均衡、序列化、容错重试等完整功能，旨在提供简单易用、高性能、可扩展的分布式服务调用解决方案，满足微服务架构下的服务间通信需求。


### rpc-github-core/ 核心模块包结构

rpc-github-core/
├── src/main/java/com/github/rpc/
│ ├── RpcApplication.java # 框架初始化入口类
│ ├── common/ # 通用组件包
│ │ ├── proxy/ # 代理相关
│ │ │ └── ServiceProxy.java # RPC服务代理实现
│ │ ├── registry/ # 注册中心相关
│ │ │ ├── Registry.java # 注册中心接口
│ │ │ ├── RegistryFactory.java # 注册中心工厂
│ │ │ └── localcache/ # 本地缓存
│ │ │ └── LocalRegistry.java # 本地服务注册表
│ │ └── utils/ # 工具类
│ │ └── ConfigUtils.java # 配置加载工具
│ ├── config/ # 配置相关
│ │ ├── GlobalRpcConfig.java # 全局RPC配置
│ │ └── RegistryConfig.java # 注册中心配置
│ ├── constants/ # 常量定义
│ │ ├── LoadBalancerConstant.java # 负载均衡常量
│ │ ├── RetryStrategyConstant.java # 重试策略常量
│ │ ├── SerializerStrategyConstant.java # 序列化策略常量
│ │ └── RpcLoadConstant.java # RPC加载常量
│ ├── exception/ # 异常处理
│ │ ├── RpcException.java # RPC异常类
│ │ ├── BizException.java # 业务异常基类
│ │ └── BaseExceptionInterface.java # 异常接口
│ ├── model/ # 数据模型
│ │ ├── RpcRequest.java # RPC请求模型
│ │ ├── RpcResponse.java # RPC响应模型
│ │ └── dto/ # 数据传输对象
│ │ └── ServiceMetaInfoDTO.java # 服务元信息DTO
│ └── service/ # 服务相关
│ └── web/ # Web服务
│ └── VertxHttpServer.java # Vert.x HTTP服务器


### Spring集成包作用分析

rpc-github-spring/
├── src/main/java/com/github/rpc/spring/
│ ├── annotation/ # 注解定义包
│ │ ├── EnableRpc.java # 启用RPC框架注解
│ │ ├── RpcService.java # RPC服务提供者注解
│ │ └── RpcReference.java # RPC服务消费者注解
│ └── bootstrap/ # 启动引导包
│ ├── RpcInitBootstrap.java # RPC框架初始化引导
│ ├── RpcProviderBootstrap.java # 服务提供者引导
│ └── RpcConsumerBootstrap.java # 服务消费者引导

