package org.mifos.processor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.Processor;
import org.mifos.connector.common.interceptor.annotation.EnableJsonWebSignature;
import org.mifos.processor.bulk.api.ApiOriginFilter;
import org.mifos.processor.bulk.camel.config.HttpClientConfigurerTrustAllCACerts;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableJsonWebSignature
public class BulkProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(BulkProcessorApplication.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    public Processor pojoToString(ObjectMapper objectMapper) {
        return exchange -> exchange.getIn().setBody(objectMapper.writeValueAsString(exchange.getIn().getBody()));
    }

    @Bean
    public CsvMapper csvMapper() {
        return new CsvMapper();
    }

    @Bean
    public HttpClientConfigurerTrustAllCACerts httpClientConfigurer() {
        return new HttpClientConfigurerTrustAllCACerts();
    }

    @Bean
    public FilterRegistrationBean<ApiOriginFilter> apiOriginFilter() {
        FilterRegistrationBean<ApiOriginFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ApiOriginFilter());
        registration.addUrlPatterns("/**");
        registration.setName("apiOriginFilter");
        registration.setOrder(Integer.MIN_VALUE+1);
        return registration;
    }

}
