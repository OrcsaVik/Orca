package com.github.rpc.domain.consumer;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ServiceRpcConsumer implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;


}