// src/main/java/xxx/config/JacksonForceConfig.java
package com.sky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
public class JacksonForceConfig implements WebMvcConfigurer {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> c : converters) {
            if (c instanceof MappingJackson2HttpMessageConverter jackson) {
                ObjectMapper m = jackson.getObjectMapper();
                JavaTimeModule javaTime = new JavaTimeModule();
                javaTime.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DTF));
                javaTime.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DTF));
                m.registerModule(javaTime);
                m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 关键：关掉时间戳/数组
                // 如需固定时区可加：m.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            }
        }
    }
}
