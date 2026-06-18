package com.duszek.pindrop.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.stream.Stream;

@Configuration
public class FlywayConfig {

    @Bean
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        return flyway;
    }

    /**
     * Ensures the entityManagerFactory bean (Hibernate schema validation) is
     * created only after Flyway has finished running migrations.
     * Spring Boot 4.x removed FlywayAutoConfiguration so this ordering must be
     * declared explicitly.
     *
     * getBeanNamesForType returns "&entityManagerFactory" (with the factory-bean
     * prefix) for FactoryBean types; BeanFactoryUtils.transformedBeanName strips
     * that prefix before looking up the bean definition.
     */
    @Bean
    public static BeanDefinitionRegistryPostProcessor flywayEntityManagerDependsOn() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {}

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                for (String name : beanFactory.getBeanNamesForType(AbstractEntityManagerFactoryBean.class, true, false)) {
                    String rawName = BeanFactoryUtils.transformedBeanName(name);
                    var bd = beanFactory.getBeanDefinition(rawName);
                    var existing = bd.getDependsOn() != null ? bd.getDependsOn() : new String[0];
                    bd.setDependsOn(Stream.concat(Arrays.stream(existing), Stream.of("flyway"))
                            .distinct().toArray(String[]::new));
                }
            }
        };
    }
}
