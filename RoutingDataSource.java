package com.weili.datasource.config;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

@Component
public class RoutingDataSource extends AbstractRoutingDataSource {

    public RoutingDataSource(
            Map<String, DataSource> datasources, DefaultDataSourceName defaultDataSourceName) {
        DataSource defaultDataSource =
                Optional.ofNullable(datasources.get(defaultDataSourceName.name().toUpperCase()))
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Default data source name is not right"));

        this.setDefaultTargetDataSource(defaultDataSource);

        Map<Object, Object> dataSourceMap =
                datasources.entrySet().stream()
                        .collect(
                                Collectors.toUnmodifiableMap(
                                        entry -> entry.getKey(), entry -> entry.getValue()));
        this.setTargetDataSources(dataSourceMap);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getBranchContext();
    }
}
