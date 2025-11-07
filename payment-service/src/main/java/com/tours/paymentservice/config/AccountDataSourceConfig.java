package com.tours.paymentservice.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.tours.paymentservice.account.repository",
        entityManagerFactoryRef = "accountEntityManager",
        transactionManagerRef = "accountTransactionManager"
)
public class AccountDataSourceConfig {

    @Bean
    @Primary
    public JpaProperties jpaProperties() {
        return new JpaProperties();
    }

    @Bean
    @Primary
    public HibernateProperties hibernateProperties() {
        return new HibernateProperties();
    }

    @Bean
    @Primary
    @org.springframework.boot.context.properties.ConfigurationProperties("spring.datasource.account")
    public DataSourceProperties accountDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource accountDataSource(@Qualifier("accountDataSourceProperties") DataSourceProperties props) {
        return props.initializeDataSourceBuilder().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean accountEntityManager(
            @Qualifier("accountDataSource") DataSource dataSource,
            JpaProperties jpaProperties,
            HibernateProperties hibernateProperties) {
        
        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(
            new org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter(),
            jpaProperties.getProperties(),
            null);
            
        return builder
                .dataSource(dataSource)
                .properties(hibernateProperties.determineHibernateProperties(
                    jpaProperties.getProperties(), new HibernateSettings()))
                .packages("com.tours.paymentservice.account.entity")
                .persistenceUnit("accountPU")
                .build();
    }

    @Bean
    public PlatformTransactionManager accountTransactionManager(@Qualifier("accountEntityManager") LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }
}
