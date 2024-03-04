package com.javierjordanluque.healthcaretreatmenttracking.util;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SerializationUtils {
    public static byte[] serialize(Object object) {
        if (object instanceof String) {
            return serializeString((String) object);
        } else if (object instanceof Enum) {
            return serializeEnum((Enum<?>) object);
        } else if (object instanceof Long) {
            return serializeTimestamp((long) object);
        } else {
            throw new IllegalArgumentException("Object type (" + object.getClass().getName() + ") not compatible for serialization");
        }
    }

    public static Object deserialize(byte[] data, Class<?> type) throws Exception {
        if (type.equals(String.class)) {
            return deserializeString(data);
        } else if (type.isEnum()) {
            try {
                return deserializeEnum(data, type);
            } catch (Exception e) {
                throw new Exception("Could not deserialize data (" + deserializeString(data) + ") to enum type (" + type.getName() + ")");
            }
        } else if (type.equals(Long.class)) {
            return deserializeTimestamp(data);
        } else {
            throw new IllegalArgumentException("Object type (" + type.getName() + ") not compatible for deserialization");
        }
    }

    private static byte[] serializeString(String string) {
        return string.getBytes(StandardCharsets.UTF_8);
    }

    private static String deserializeString(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    private static byte[] serializeEnum(Enum<?> enumValue) {
        return enumValue.name().getBytes(StandardCharsets.UTF_8);
    }

    private static <T extends Enum<T>> T deserializeEnum(byte[] data, Class<?> enumType) throws Exception {
        Method valueOfMethod = enumType.getMethod("valueOf", String.class);
        return (T) valueOfMethod.invoke(null, deserializeString(data));
    }

    private static byte[] serializeTimestamp(long timestamp) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(timestamp);
        return buffer.array();
    }

    private static long deserializeTimestamp(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        return buffer.getLong();
    }
}
