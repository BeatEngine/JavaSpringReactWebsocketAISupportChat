package org.beatengine.aibotdemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;


import javax.sql.DataSource;
import java.util.Properties;

//@EnableWebMvc
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories
public class ApplicationConfiguration{

    /**
     * This maps index to /static/index.html instead of /template/index.html
     * @return Resolver
     */
    @Bean
    public ClassLoaderTemplateResolver secondaryTemplateResolver() {
        ClassLoaderTemplateResolver secondaryTemplateResolver = new ClassLoaderTemplateResolver();
        secondaryTemplateResolver.setPrefix("static/");
        secondaryTemplateResolver.setSuffix(".html");
        secondaryTemplateResolver.setTemplateMode(TemplateMode.HTML);
        secondaryTemplateResolver.setCharacterEncoding("UTF-8");
        secondaryTemplateResolver.setOrder(1);
        secondaryTemplateResolver.setCheckExistence(true);

        return secondaryTemplateResolver;
    }

    /* @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
         registry.addResourceHandler("/resources/**")
                 .addResourceLocations("/resources/static/")
                .setCacheControl(CacheControl.maxAge(Duration.ofHours(18)))
                 .resourceChain(true)
                 .addResolver(new PathResourceResolver());

    }*/

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em
                = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("org.beatengine.aibotdemo");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalProperties());

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());

        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
        return new PersistenceExceptionTranslationPostProcessor();
    }

    Properties additionalProperties() {
        Properties properties = new Properties();
        /*
        spring.jpa.generate-ddl=true
        spring.jpa.hibernate.ddl-auto=update
        * */
        // Let Hibernate quote all names with '"' --> "Object"
        properties.setProperty("hibernate.globally_quoted_identifiers", "true");

        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        //properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        return properties;
    }

    @Bean
    public DataSource dataSource(){
        McpSupportDemoApplication.loadRootProjectEnvironmentVariables();
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        /*
        * app.datasource.url=jdbc:postgresql://localhost/demo_app_mcp
          app.datasource.username=
          app.datasource.password=
          app.datasource.pool-size=30*/
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://"+System.getProperty("APP_POSTGRES_HOST")+"/" + System.getProperty("APPLICATION_DB"));
        dataSource.setUsername(System.getProperty("POSTGRES_USER"));
        dataSource.setPassword(System.getProperty("POSTGRES_PASSWORD"));
        return dataSource;
    }
}
