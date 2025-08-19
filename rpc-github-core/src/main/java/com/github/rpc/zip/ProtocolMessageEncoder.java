package com.github.rpc.zip;

import com.github.rpc.common.serializer.Serializer;
import com.github.rpc.common.serializer.SerializerFactory;
import com.github.rpc.enums.ProtocolMessageDTOSerializerEnum;
import com.github.rpc.enums.ResponseCodeEnum;
import com.github.rpc.exception.BizException;
import com.github.rpc.model.dto.ProtocolMessageDTO;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

public class ProtocolMessageEncoder {

    /**
     * 编码
     *
     * @param protocolMessage
     * @return
     * @throws IOException
     */
    public static Buffer encode(ProtocolMessageDTO<?> protocolMessage) throws IOException {
        if (protocolMessage == null || protocolMessage.getHeader() == null) {
            return Buffer.buffer();
        }
        ProtocolMessageDTO.MessageHeader header = protocolMessage.getHeader();
        // 依次向缓冲区写入字节
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getSerializer());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());
        // 获取序列化器 根据Key
        ProtocolMessageDTOSerializerEnum serializerEnum = ProtocolMessageDTOSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new BizException(ResponseCodeEnum.NOT_VALID_MESSAGE_SERIALIZER);
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        byte[] bodyBytes = serializer.serialize(protocolMessage.getBody());
        // 写入 body 长度和数据
        buffer.appendInt(bodyBytes.length);
        buffer.appendBytes(bodyBytes);
        return buffer;
    }
}

