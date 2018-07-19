package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class,HibernateJpaAutoConfiguration.class})
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }


    @Bean
    DatabaseServiceCredentials databaseServiceCredentials(@Value("${vcap.services}") String vcapValue){
        return new DatabaseServiceCredentials(vcapValue);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public HikariDataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariDataSource hikariDataSource = new HikariDataSource();
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql", "p-mysql"));
        hikariDataSource.setDataSource(dataSource);
        return hikariDataSource;
    }

    @Bean
    public HikariDataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariDataSource hikariDataSource = new HikariDataSource();
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql", "p-mysql"));
        hikariDataSource.setDataSource(dataSource);
        return hikariDataSource;
    }

    @Bean(name = "moviesEntityManager")
    public EntityManager moviesEntityManager(DatabaseServiceCredentials serviceCredentials) {
        return moviesEntityManagerFactory(serviceCredentials).createEntityManager();
    }


    @Bean(name = "moviesEntityManagerFactory")
    public EntityManagerFactory moviesEntityManagerFactory(DatabaseServiceCredentials serviceCredentials) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.getJpaPropertyMap().put("spring.jpa.hibernate.ddl-auto","create-drop");
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        adapter.setDatabase(Database.MYSQL);
        adapter.setGenerateDdl(true);
        emf.setDataSource(moviesDataSource(serviceCredentials));
        emf.setJpaVendorAdapter(adapter);
        emf.setPackagesToScan("org.superbiz.moviefun.movies");
        emf.setPersistenceUnitName("movies-unit");
        emf.afterPropertiesSet();
        return emf.getObject();
    }

    @Bean(name = "albumsEntityManager")
    public EntityManager albumsEntityManager(DatabaseServiceCredentials serviceCredentials) {
        return albumsEntityManagerFactory(serviceCredentials).createEntityManager();
    }


    @Bean(name = "albumsEntityManagerFactory")
    public EntityManagerFactory albumsEntityManagerFactory(DatabaseServiceCredentials serviceCredentials) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        adapter.setDatabase(Database.MYSQL);
        adapter.getJpaPropertyMap().put("spring.jpa.hibernate.ddl-auto","create-drop");
        adapter.setGenerateDdl(true);
        emf.setDataSource(albumsDataSource(serviceCredentials));
        emf.setJpaVendorAdapter(adapter);
        emf.setPackagesToScan("org.superbiz.moviefun.albums");
        emf.setPersistenceUnitName("albums-unit");
        emf.afterPropertiesSet();
        return emf.getObject();
    }

    @Bean(name = "transactionManagerAlbums")
    public PlatformTransactionManager transactionManagerAlbums(DatabaseServiceCredentials serviceCredentials) {
        JpaTransactionManager tm = new JpaTransactionManager();
        tm.setEntityManagerFactory(albumsEntityManagerFactory(serviceCredentials));
        return tm;
    }

    @Bean(name = "transactionManagerMovies")
    public PlatformTransactionManager transactionManagerMovies(DatabaseServiceCredentials serviceCredentials) {
        JpaTransactionManager tm = new JpaTransactionManager();
        tm.setEntityManagerFactory(moviesEntityManagerFactory(serviceCredentials));
        return tm;
    }

}
