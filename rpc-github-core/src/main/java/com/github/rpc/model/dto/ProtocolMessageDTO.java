package com.github.rpc.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//作为rpc之间的消息传输DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProtocolMessageDTO<T> {

    /**
     * 消息头
     */
    private MessageHeader header;

    /**
     * 消息体（请求或响应对象）
     */
    private T body;

    /**
     * TODO协议消息头 单独实现 总共17个字节
     */
    @Data
    public static class MessageHeader {

        /**
         * 魔数，保证安全性
         */
        private byte magic;

        /**
         * 版本号
         */
        private byte version;

        /**
         * 序列化器
         */
        private byte serializer;

        /**
         * 消息类型（请求 / 响应）
         * 0 请求 1 响应
         */
        private byte type;

        /**
         * 状态
         */
        private byte status;

        /**
         * 请求 id
         */
        private Long requestId;

        /**
         * 消息体长度
         */
        private Integer bodyLength;
    }

}

