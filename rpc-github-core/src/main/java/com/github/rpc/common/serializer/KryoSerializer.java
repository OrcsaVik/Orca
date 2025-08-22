package com.github.rpc.common.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;

import java.io.IOException;

public class KryoSerializer implements Serializer {

    /**
     * ThreadLocal 为每个线程维护独立的 Kryo 实例，避免并发问题。
     * Kryo 本身不是线程安全的，必须如此处理。
     */
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // 允许对象循环引用
        kryo.setReferences(true);
        // 可在此注册常用类以提升性能（可配置化扩展点）
        // kryo.register(User.class);
        // kryo.register(Request.class);
        return kryo;
    });

    /**
     * 将对象序列化为字节数组
     *
     * @param object 待序列化的对象
     * @param <T>    对象类型
     * @return 字节数组
     * @throws IOException 序列化失败时抛出
     */
    @Override
    public <T> byte[] serialize(T object) throws IOException {
        try (ByteBufferOutput output = new ByteBufferOutput(4096)) {
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            kryo.writeObject(output, object);
            return output.toBytes();
        } catch (Exception e) {
            throw new IOException("Kryo serialization failed for object: " + object.getClass(), e);
        }
    }

    /**
     * 将字节数组反序列化为指定类型的对象
     *
     * @param bytes  字节数组
     * @param tClass 目标类类型
     * @param <T>    对象类型
     * @return 反序列化后的对象
     * @throws IOException 反序列化失败时抛出
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> tClass) throws IOException {
        try (ByteBufferInput input = new ByteBufferInput(bytes)) {
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            T result = kryo.readObject(input, tClass);
            return result;
        } catch (Exception e) {
            throw new IOException("Kryo deserialization failed for class: " + tClass.getName(), e);
        }
    }

    /**
     * 可选：重置 Kryo 实例状态（通常不需要）
     * 如果希望每次使用后清理状态，可以调用此方法
     */
    public static void resetKryo() {
        KRYO_THREAD_LOCAL.remove();
    }
}