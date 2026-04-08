package com.mcly.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mcly.gate")
public record GateProperties(String hmacSecret, int tokenTtlSeconds) {
}
