package ru.otus.hw.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "ru.otus.hw.domain.pg")
@EnableJpaRepositories(basePackages = "ru.otus.hw.repo.pg")
public class DatasourcePgConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            EntityManagerFactoryBuilder builder
    ) {
        return builder
                .dataSource(dataSource)
                .packages("ru.otus.hw.domain.pg")
                .persistenceUnit("pg-unit")
                .build();
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager pgTransactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
