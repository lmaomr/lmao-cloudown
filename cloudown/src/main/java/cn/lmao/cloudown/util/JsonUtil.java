package cn.lmao.cloudown.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;

/**
 * 完整的JSON工具类（线程安全）
 */
public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * 对象转JSON字符串
     */
    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new JsonException("对象转JSON失败", e);
        }
    }

    /**
     * JSON字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new JsonException("JSON转对象失败", e);
        }
    }

    /**
     * JSON转复杂类型（支持泛型）
     */
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return mapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new JsonException("JSON转复杂类型失败", e);
        }
    }

    /**
     * JSON转Map
     */
    public static Map<String, Object> toMap(String json) {
        return fromJson(json, new TypeReference<>() {});
    }

    /**
     * 美化输出
     */
    public static String prettyPrint(Object obj) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new JsonException("JSON美化输出失败", e);
        }
    }

    /**
     * 自定义异常
     */
    public static class JsonException extends RuntimeException {
        public JsonException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
