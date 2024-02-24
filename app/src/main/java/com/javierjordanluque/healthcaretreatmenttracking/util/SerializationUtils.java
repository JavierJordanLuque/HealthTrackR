package com.javierjordanluque.healthcaretreatmenttracking.util;

import java.lang.reflect.Method;
import java.time.LocalDate;

public class SerializationUtils {
    public static byte[] serialize(Object object) {
        if (object instanceof String) {
            return serializeString((String) object);
        } else if (object instanceof Enum) {
            return serializeEnum((Enum<?>) object);
        } else if (object instanceof LocalDate) {
            return serializeLocalDate((LocalDate) object);
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
        } else if (type.equals(LocalDate.class)) {
            return deserializeLocalDate(data);
        } else {
            throw new IllegalArgumentException("Object type (" + type.getName() + ") not compatible for deserialization");
        }
    }

    private static byte[] serializeString(String string) {
        return string.getBytes();
    }

    private static String deserializeString(byte[] data) {
        return new String(data);
    }

    private static byte[] serializeEnum(Enum<?> enumValue) {
        return enumValue.name().getBytes();
    }

    private static <T extends Enum<T>> T deserializeEnum(byte[] data, Class<?> enumType) throws Exception {
        Method valueOfMethod = enumType.getMethod("valueOf", String.class);
        return (T) valueOfMethod.invoke(null, deserializeString(data));
    }

    private static byte[] serializeLocalDate(LocalDate localDate) {
        return serializeString(localDate.toString());
    }

    private static LocalDate deserializeLocalDate(byte[] data) {
        return LocalDate.parse(deserializeString(data));
    }
}