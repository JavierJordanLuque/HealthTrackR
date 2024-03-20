package com.javierjordanluque.healthtrackr.util.security;

import com.javierjordanluque.healthtrackr.util.exceptions.DeserializationException;
import com.javierjordanluque.healthtrackr.util.exceptions.SerializationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SerializationUtils {
    public static byte[] serialize(Object object) throws SerializationException {
        try {
            if (object instanceof String) {
                return serializeString((String) object);
            } else if (object instanceof Enum) {
                return serializeEnum((Enum<?>) object);
            } else if (object instanceof Long) {
                return serializeTimestamp((long) object);
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException exception) {
            throw new SerializationException("Failed to serialize object class (" + object.getClass().getName() + ")", exception);
        }
    }

    public static Object deserialize(byte[] data, Class<?> type) throws DeserializationException {
        try {
            if (type.equals(String.class)) {
                return deserializeString(data);
            } else if (type.isEnum()) {
                return deserializeEnum(data, type);
            } else if (type.equals(Long.class)) {
                return deserializeTimestamp(data);
            } else {
                throw new IllegalArgumentException();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException exception) {
            throw new DeserializationException("Failed to deserialize with class (" + type.getName() + ")", exception);
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

    private static <T extends Enum<T>> T deserializeEnum(byte[] data, Class<?> enumType) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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
