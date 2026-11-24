package com.gen.auth.config;


import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.gen.auth.datasource.CurrentTenantIdentifierResolverImpl;
import com.gen.auth.datasource.MultiTenantConnectionProviderImpl;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;


@Configuration
public class HibernateConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            DataSource dataSource,
            MultiTenantConnectionProviderImpl connectionProvider,
            CurrentTenantIdentifierResolverImpl tenantResolver) {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER,
                connectionProvider);

        properties.put(
                AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER,
                tenantResolver);

        return builder
                .dataSource(dataSource)
                .packages("com.gen.auth")
                .properties(properties)
                .build();
    }

}
