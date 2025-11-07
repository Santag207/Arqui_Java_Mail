package com.tours.paymentservice.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.tours.paymentservice.inventory.repository",
        entityManagerFactoryRef = "inventoryEntityManager",
        transactionManagerRef = "inventoryTransactionManager"
)
public class InventoryDataSourceConfig {

    @Bean
    @org.springframework.boot.context.properties.ConfigurationProperties("spring.datasource.inventory")
    public DataSourceProperties inventoryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource inventoryDataSource(@Qualifier("inventoryDataSourceProperties") DataSourceProperties props) {
        return props.initializeDataSourceBuilder().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean inventoryEntityManager(
            @Qualifier("inventoryDataSource") DataSource dataSource,
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
                .packages("com.tours.paymentservice.inventory.entity")
                .persistenceUnit("inventoryPU")
                .build();
    }

    @Bean
    public PlatformTransactionManager inventoryTransactionManager(@Qualifier("inventoryEntityManager") LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }
}
