package com.github.rpc.spring.bootstrap;

import com.github.rpc.common.proxy.RpcReference;
import com.github.rpc.common.proxy.ServiceProxyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

@Slf4j
public class RpcConsumerBootstrap implements BeanPostProcessor {

    /**
     * Bean 初始化后执行，注入服务
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();

        for (Field field : fields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                log.info("💉 注入 RPC 代理: {}.{}", beanClass.getSimpleName(), field.getName());

                Class<?> interfaceClass = rpcReference.interfaceClass();
                if (interfaceClass == void.class) {
                    interfaceClass = field.getType();
                }

                if (!interfaceClass.isInterface()) {
                    throw new IllegalArgumentException("RPC 引用必须是接口: " + field.getName());
                }

                // 使用工厂创建代理
                Object proxy = ServiceProxyFactory.getProxy(interfaceClass, rpcReference);

                // 注入字段
                try {
                    field.setAccessible(true);
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("注入失败: " + field.getName(), e);
                } finally {
                    field.setAccessible(false);
                }
            }
        }

        return bean;
    }
}

