package com.github.rpc.zip;

import com.github.rpc.common.serializer.Serializer;
import com.github.rpc.common.serializer.SerializerFactory;
import com.github.rpc.constants.ProtocolConstant;
import com.github.rpc.enums.ProtocolMessageDTOSerializerEnum;
import com.github.rpc.enums.ProtocolMessageTypeEnum;
import com.github.rpc.enums.ResponseCodeEnum;
import com.github.rpc.exception.BizException;
import com.github.rpc.model.RpcRequest;
import com.github.rpc.model.RpcResponse;
import com.github.rpc.model.dto.ProtocolMessageDTO;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;


/**
 * TODO 协议消息的解码 针对适用TCP
 */
public class ProtocolMessageDTODecoder {

    /**
     * 解码 手动进行拆包
     *
     * @param buffer
     * @return
     * @throws IOException
     */
    public static ProtocolMessageDTO<?> decode(Buffer buffer) throws IOException {
        // 分别从指定位置读出 Buffer
        ProtocolMessageDTO.MessageHeader header = new ProtocolMessageDTO.MessageHeader();
        byte magic = buffer.getByte(0);
        // 校验魔数
        if (magic != ProtocolConstant.PROTOCOL_MAGIC) {
            throw new BizException(ResponseCodeEnum.NOT_VALID_MESSAGE_MAGIC);
        }
        //拆包
        header.setMagic(magic);
        header.setVersion(buffer.getByte(1));
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        //
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getLong(5));
        header.setBodyLength(buffer.getInt(13));
        //追加对应请求体的长度
        // 解决粘包问题，只读指定长度的数据 本身占四个字节Message
        byte[] bodyBytes = buffer.getBytes(17, 17 + header.getBodyLength());
        // 解析消息体
        ProtocolMessageDTOSerializerEnum serializerEnum = ProtocolMessageDTOSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new BizException(ResponseCodeEnum.NOT_VALID_MESSAGE_SERIALIZER);
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        //请求还是响应 int) header.getType( 直接映射
        ProtocolMessageTypeEnum messageTypeEnum = ProtocolMessageTypeEnum.valueOf((int) header.getType());
        if (messageTypeEnum == null) {
            throw new BizException(ResponseCodeEnum.NOT_VALID_MESSAGE_TYPE);
        }
        switch (messageTypeEnum) {
            case REQUEST:
                RpcRequest request = serializer.deserialize(bodyBytes, RpcRequest.class);
                return new ProtocolMessageDTO<>(header, request);
            case RESPONSE:
                RpcResponse response = serializer.deserialize(bodyBytes, RpcResponse.class);
                return new ProtocolMessageDTO<>(header, response);
            //针对心跳处理机制
            case HEART_BEAT:
            case OTHERS:
            default:
                throw new BizException(ResponseCodeEnum.UNSUPPORTED_MESSAGE_TYPE);
        }
    }

}
