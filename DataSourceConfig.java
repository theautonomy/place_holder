package com.weili.datasource.config;

import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

    @ConfigurationProperties("datasources")
    private static record HikariConfigs(Map<String, HikariConfig> registration) {
        HikariConfigs {
            registration = Map.copyOf(registration);
        }
    }

    @Bean
    Map<String, DataSource> datasources(HikariConfigs hikariConfigs) {
        return Map.copyOf(
                hikariConfigs.registration().entrySet().stream()
                        .collect(
                                Collectors.toUnmodifiableMap(
                                        entry -> entry.getKey().toUpperCase(),
                                        entry -> new HikariDataSource(entry.getValue()))));
    }
}
