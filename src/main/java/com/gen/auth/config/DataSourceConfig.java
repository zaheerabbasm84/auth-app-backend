package com.gen.auth.config;

import com.gen.auth.datasource.TenantAwareDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public DataSource dataSource() {

        // Real Connection Pool
        HikariDataSource hikari = new HikariDataSource();

        hikari.setJdbcUrl(url);
        hikari.setUsername(username);
        hikari.setPassword(password);

        // Wrap Hikari with TenantAwareDataSource
        //new push
        return new TenantAwareDataSource(hikari);
    }
}