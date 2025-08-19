package com.github.rpc.common.retry.impl;

import com.github.rholder.retry.*;
import com.github.rpc.RpcApplication;
import com.github.rpc.common.retry.RetryStrategy;
import com.github.rpc.config.GlobalRpcConfig;
import com.github.rpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class GuavaRetryStrategy implements RetryStrategy {

    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        // 从配置中读取重试参数（你可以从 RpcConfig 或注解中获取）
        GlobalRpcConfig config = RpcApplication.getRpcConfig();
        int maxAttempts = Optional.ofNullable(config.getMaxRetryAttempts()).orElse(3);
        long retryInterval = Optional.ofNullable(config.getRetryInterval()).orElse(100L); // ms

        // 构建重试器（核心：声明式编程，优雅永不过时）
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                // 仅在网络异常、连接超时等可恢复异常时重试 TODO待实现
                .retryIfExceptionOfType(IOException.class)
                .retryIfExceptionOfType(TimeoutException.class)
                .retryIfRuntimeException()
                // 重试次数
                .withStopStrategy(StopStrategies.stopAfterAttempt(maxAttempts))
                // 固定间隔 or 指数退避？你说了算！
                .withWaitStrategy(WaitStrategies.fixedWait(retryInterval, TimeUnit.MILLISECONDS))
                // 日志监听器（关键！不然出问题你都不知道重了几次）
                .withRetryListener(new RetryListener() {
                    @Override
                    public <T> void onRetry(Attempt<T> attempt) {
                        if (attempt.hasException()) {
                            log.warn("【RPC重试中】第 {} 次重试，异常: {}",
                                    attempt.getAttemptNumber(),
                                    attempt.getExceptionCause().getMessage());
                        }
                    }
                })
                .build();

        // 执行带重试的调用
        return retryer.call(callable);
    }
}