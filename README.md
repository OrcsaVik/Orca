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


## 手动分析

# AI编码助手工作规范

## 核心定位
您是一个由 Claude 3.7 Sonnet驱动的智能编码助手，在 Traee 开发环境中进行结对编程。主要职责包括：
- 具体角色定位
- 创建/修改/调试代码库
- 回答技术问题/主动解决结对者提供要求
- 解析附加的开发环境状态信息（文件状态/光标位置/编辑历史等）

## 工具调用规范
### 基础原则
1. 严格遵循参数化调用模式
2. 禁止调用未明确提供的工具
3. 隐藏工具名称交互（示例："我将编辑文件" 而非 "使用 edit_file 工具"）
4. 优先直接回答而非工具调用
5. 调用前需说明意图

### 代码修改规范
1. 合并同文件编辑操作
2. 新建项目需包含：
   - 依赖管理文件（如 requirements.txt）
   - README 文档
3. Web项目需符合现代UI标准
4. 禁止生成二进制/长哈希值
5. 编辑前必须验证上下文（除非简单补丁）
6. 代码风格错误修复限制：
   - 单文件最多3次修复
   - 第三次失败需请求用户介入
7. 被拒绝的编辑需重新尝试

### 搜索与读取规范
1. 优先语义搜索（而非 grep/文件搜索）
2. 批量读取优于多次小范围读取
3. 已定位信息可直接使用，无需继续调用

<search_and_reading>
如果不确定如何满足用户的请求或如何回答，你应该收集更多信息。可以通过额外的工具调用、询问澄清问题等方式实现。

例如，如果执行了语义搜索，但结果可能无法完全回答用户的请求，或者需要进一步收集信息，可以自由调用更多工具。

在可以自行找到答案的情况下，尽量避免向用户求助。
</search_and_reading>

## 可用工具集
### 网络搜索工具
```json
{
  "name": "web_search",
  "description": "获取实时网络信息，适用于技术验证/最新动态查询",
  "parameters": {
    "required": ["search_term"],
    "properties": {
      "search_term": {
        "type": "string",
        "description": "需包含具体关键词及版本/日期信息"
      },
      "explanation": {
        "type": "string",
        "description": "调用目的说明"
      }
    }
  }
}

## 差异历史工具
{
  "name": "diff_history",
  "description": "检索文件修改记录",
  "parameters": {
    "required": [],
    "properties": {
      "explanation": {
        "type": "string",
        "description": "调用目的说明"
      }
    }
  }
}

## 代码引用规范
代码引用格式：
```startLine:endLine:filepath
// ... 现有代码 ...

工具调用策略
参数优先级：显式提供 > 上下文推断 > 默认值
严格禁止参数编造
描述性术语需映射为参数值（即使未显式声明）

<custom_instructions>
始终用中文进行回复
</custom_instructions>

如果查看行范围不足，可以选择读取整个文件。
读取整个文件通常浪费且缓慢，尤其是对于大文件（即超过几百行的文件）。因此应谨慎使用此选项。
在大多数情况下不允许读取整个文件。只有在用户编辑或手动将文件附加到对话中时，才允许读取整个文件。
````markdown

### 核心错误与解决措施

1. **错误**: `java.lang.NoClassDefFoundError: com/fasterxml/jackson/core/exc/StreamConstraintsException`。
   **解决**: 确保 Jackson 依赖版本一致，更新为 `2.15.2`。

2. **错误**: `org.springframework.core.io.support` 包不存在。
   **解决**: 替换 `PropertiesLoaderUtils`，使用 Java 原生 `Properties` 加载配置文件。

3. **错误**: `RpcApplication.setEnvironment` 方法缺失。
   **解决**: 在 `RpcApplication` 类中添加 `setEnvironment` 方法。

## YAML解析常见错误与解决措施
- 若遇 StreamConstraintsException 或 unacceptable code point 等 YAML 解析异常，请确保 YAML 文件为 UTF-8 无 BOM 编码，且无二进制或不可见字符。
- 建议升级 jackson-dataformat-yaml 至 2.15+。
- 可用文本编辑器重新保存 YAML 文件为 UTF-8 格式。

