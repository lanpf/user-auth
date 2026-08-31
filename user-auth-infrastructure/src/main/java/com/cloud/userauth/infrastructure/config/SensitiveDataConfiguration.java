package com.cloud.userauth.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SensitiveDataProperties.class)
public class SensitiveDataConfiguration {
}
