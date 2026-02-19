package com.example.usercenterpractice.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Long类型转String序列化配置
 * 解决JavaScript大数字精度丢失问题
 */
@Configuration
public class LongToStringSerializerConfig {

    /**
     * 自定义序列化器：将Long序列化为字符串
     */
    public static class LongToStringSerializer extends JsonSerializer<Long> {
        @Override
        public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeString(value.toString());
            }
        }
    }

    /**
     * 配置Jackson的ObjectMapper
     * 1. 将所有Long类型序列化为字符串
     * 2. 配置JavaTimeModule支持LocalDateTime
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 将所有Long类型序列化为字符串，防止JavaScript精度丢失
            builder.serializerByType(Long.class, new LongToStringSerializer());
            builder.serializerByType(Long.TYPE, new LongToStringSerializer());
        };
    }
}
