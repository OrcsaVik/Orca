package com.github.rpc.service.web;

import com.github.rpc.common.registry.localcache.LocalRegistry;
import com.github.rpc.enums.ProtocolMessageStatusEnum;
import com.github.rpc.enums.ProtocolMessageTypeEnum;
import com.github.rpc.model.RpcRequest;
import com.github.rpc.model.RpcResponse;
import com.github.rpc.model.dto.ProtocolMessageDTO;
import com.github.rpc.zip.ProtocolMessageDTODecoder;
import com.github.rpc.zip.ProtocolMessageEncoder;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

public class TcpServerHandler implements Handler<NetSocket> {

    /**
     * 处理请求
     *
     * @param socket the event to handle
     */
    @Override
    public void handle(NetSocket socket) {
        TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
            // 接受请求，解码
            ProtocolMessageDTO<RpcRequest> ProtocolMessageDTO;
            try {
                ProtocolMessageDTO = (ProtocolMessageDTO<RpcRequest>) ProtocolMessageDTODecoder.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException("协议消息解码错误");
            }
            RpcRequest rpcRequest = ProtocolMessageDTO.getBody();
            ProtocolMessageDTO.MessageHeader header = ProtocolMessageDTO.getHeader();

            // 处理请求
            // 构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();
            try {
                // 获取要调用的服务实现类，通过反射调用
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                // 封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            // 发送响应，编码
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getType());
            header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());
            ProtocolMessageDTO<RpcResponse> responseProtocolMessageDTO = new ProtocolMessageDTO<>(header, rpcResponse);
            try {
                Buffer encode = ProtocolMessageEncoder.encode(responseProtocolMessageDTO);
                socket.write(encode);
            } catch (IOException e) {
                throw new RuntimeException("协议消息编码错误");
            }
        });
        socket.handler(bufferHandlerWrapper);
    }

}
