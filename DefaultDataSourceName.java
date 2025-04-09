package com.weili.datasource.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties("default.datasource")
public record DefaultDataSourceName(String name) {
    public DefaultDataSourceName {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Default data source needs to be set");
        }
    }
}
