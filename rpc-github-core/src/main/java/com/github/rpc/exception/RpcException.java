package com.github.rpc.exception;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RpcException extends BizException {

    public RpcException(String message) {
        super(message);
    }


    public RpcException(BaseExceptionInterface baseExceptionInterface) {
        super(baseExceptionInterface);
    }
}
