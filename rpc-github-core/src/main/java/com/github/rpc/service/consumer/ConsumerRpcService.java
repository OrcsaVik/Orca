package com.github.rpc.service.consumer;

import com.github.rpc.domain.consumer.ServiceRpcConsumer;

//TODO静态变量 消费者调用接口
public interface ConsumerRpcService {




    ServiceRpcConsumer getConsumser();

    ServiceRpcConsumer setConsumser(ServiceRpcConsumer consumser);


}
