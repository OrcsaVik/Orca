package com.github.rpc.common.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.github.rpc.RpcApplication;
import com.github.rpc.common.loadblanace.LoadBalancerFactory;
import com.github.rpc.common.loadblanace.LoadBalancerStrategy;
import com.github.rpc.common.registry.Registry;
import com.github.rpc.common.registry.RegistryFactory;
import com.github.rpc.common.retry.RetryStrategy;
import com.github.rpc.common.retry.RetryStrategyFactory;
import com.github.rpc.common.serializer.KryoSerializer;
import com.github.rpc.common.serializer.Serializer;
import com.github.rpc.common.serializer.SerializerFactory;
import com.github.rpc.common.tolerant.TolerantStrategy;
import com.github.rpc.common.tolerant.TolerantStrategyFactory;
import com.github.rpc.config.GlobalRpcConfig;
import com.github.rpc.config.RpcReferenceConfig;
import com.github.rpc.config.RpcThreadPool;
import com.github.rpc.constants.RpcLoadConstant;
import com.github.rpc.constants.TolerantStrategyConstant;
import com.github.rpc.exception.BizException;
import com.github.rpc.model.RpcRequest;
import com.github.rpc.model.RpcResponse;
import com.github.rpc.model.dto.ServiceMetaInfoDTO;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * TODO 这里默认适用服务类代理对象
 */
@Slf4j

public class ServiceProxy implements InvocationHandler {

    private RpcReferenceConfig config;

    public void setConfig(RpcReferenceConfig config) {
        this.config = config;
    }


    /**
     * 调用代理
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        // 从注册中心获取服务提供者请求地址
        GlobalRpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RpcReferenceConfig thisConfig = config;
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
        ServiceMetaInfoDTO serviceMetaInfo = new ServiceMetaInfoDTO();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceVersion(RpcLoadConstant.DEFAULT_SERVICE_VERSION);
        List<ServiceMetaInfoDTO> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            throw new BizException("暂无服务地址");
        }

        // 负载均衡
        LoadBalancerStrategy loadBalancer = LoadBalancerFactory.getInstance(thisConfig.getLoadBalancer());
        // 将调用方法名（请求路径）作为负载均衡参数
        Map<String, Object> requestParams = new HashMap<>();
        //调用方法
        requestParams.put("methodName", rpcRequest.getMethodName());
        ServiceMetaInfoDTO selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
        // http 请求
        // 指定序列化器
        Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        byte[] bodyBytes = serializer.serialize(rpcRequest);
        // 使用重试机制
        RpcResponse rpcResponse = null;
        RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(thisConfig.getRetryStrategy());
        //假设这里使用空字符拆n TODO 待实现
        TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(TolerantStrategyConstant.FAIL_FAST);
        // 使用线程池执行 HTTP 请求（异步执行，但主线程阻塞等待结果）子线程进行处理
        Future<RpcResponse> future = RpcThreadPool.newExecutor().submit(() -> {
            try {
                return retryStrategy.doRetry(() ->
                        doHttpRequest(selectedServiceMetaInfo, bodyBytes, serializer)
        );
            } catch (Exception e) {
                return tolerantStrategy.doTolerant(null, e);
            }finally {
                //清理ThreadLocal
                KryoSerializer.resetKryo();
            }
        });

        try {
             rpcResponse = future.get(1, TimeUnit.SECONDS);
        } catch (Exception e){
            log.warn("服务调用存在错误");
            return RpcResponse.fail("服务发生错误");

        }

        //返回调用结果
        return rpcResponse.getData();


    }


    //发送http请求到vertix服务器
    private static RpcResponse doHttpRequest(ServiceMetaInfoDTO selectedServiceMetaInfo, byte[] bodyBytes,Serializer serializer) throws IOException {

        //序列化处理 加载 默认Json
        // 发送 HTTP 请求 POST 自动关闭资源
        try (HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
                .body(bodyBytes)
                .execute()) {
            byte[] result = httpResponse.bodyBytes();
            // 反序列化
            RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
            return rpcResponse;
        }
    }
}

