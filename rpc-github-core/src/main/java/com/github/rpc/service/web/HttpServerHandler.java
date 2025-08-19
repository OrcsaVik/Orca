package com.github.rpc.service.web;

import com.github.rpc.RpcApplication;
import com.github.rpc.common.registry.localcache.LocalRegistry;
import com.github.rpc.common.serializer.Serializer;
import com.github.rpc.common.serializer.SerializerFactory;
import com.github.rpc.model.RpcRequest;
import com.github.rpc.model.RpcResponse;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class HttpServerHandler implements Handler<HttpServerRequest> {

    @Override
    public void handle(HttpServerRequest request) {
        // 1. 获取序列化器
        Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());


        log.info("Received HTTP request: {} {}", request.method(), request.uri());

        // 3. 异步处理请求体
        request.bodyHandler(body -> {
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest = null;

            // 4. ✅ 反序列化请求，异常要捕获并响应
            try {
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
            } catch (Exception e) {
                log.error("【反序列化失败】请求数据格式错误, uri={}", request.uri(), e);
                RpcResponse errorResponse = RpcResponse.fail("Invalid request data");
                doResponse(request, errorResponse, serializer);
                return;
            }

            // 5. ✅ 请求为空？直接返回错误
            if (rpcRequest == null) {
                log.warn("【空请求】收到 null rpcRequest, uri={}", request.uri());
                RpcResponse errorResponse = RpcResponse.fail("rpcRequest is null");
                doResponse(request, errorResponse, serializer);
                return;
            }

            // 6. ✅ 执行本地调用
            RpcResponse rpcResponse = new RpcResponse();
            try {
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                if (implClass == null) {
                    log.warn("【服务未注册】service={}", rpcRequest.getServiceName());
                    rpcResponse.setMessage("Service not found: " + rpcRequest.getServiceName());
                    rpcResponse.setException(new NoSuchMethodException("Service not registered"));
                } else {
                    Method method = implClass.getMethod(
                            rpcRequest.getMethodName(),
                            rpcRequest.getParameterTypes()
                    );
                    Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                    rpcResponse.setData(result);
                    rpcResponse.setDataType(method.getReturnType());
                    rpcResponse.setMessage("ok");
                }
            }catch (Exception e) {
                // 兜底异常
                log.error("【未知错误】处理请求失败, request={}", rpcRequest, e);
                rpcResponse.setMessage("Internal server error");
                rpcResponse.setException(e);
            }

            // 7. ✅ 返回响应
            doResponse(request, rpcResponse, serializer);
        });
    }

    /**
     * 响应
     */
    void doResponse(HttpServerRequest request, RpcResponse rpcResponse, Serializer serializer) {
        HttpServerResponse response = request.response()
                .putHeader("content-type", "application/json");

        try {
            byte[] serialized = serializer.serialize(rpcResponse);
            response.end(Buffer.buffer(serialized));
        } catch (Exception e) {
            log.error("【序列化响应失败】无法返回结果", e);
            // 尽力返回一个空响应
            response.setStatusCode(500).end("Serialization failed");
        }
    }
}