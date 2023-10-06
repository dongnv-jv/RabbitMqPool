package vn.vnpay.rabbitmq.config.database;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.rabbitmq.bean.PaymentRecord;

import java.util.HashMap;
import java.util.Map;

public class DatabaseConnectionPool {
    Logger logger = LoggerFactory.getLogger(DatabaseConnectionPool.class);

    private StandardServiceRegistry registry;
    private SessionFactory sessionFactory;

    private static DatabaseConnectionPool dbUtil;

    public static DatabaseConnectionPool getInstance() {
        if (dbUtil == null) {
            dbUtil = new DatabaseConnectionPool();
        }
        return dbUtil;
    }


    public SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
                Map<String, Object> settings = new HashMap<>();
                settings.put(Environment.DRIVER, "org.postgresql.Driver");
                settings.put(Environment.URL, "jdbc:postgresql://localhost:5432/testdb");
                settings.put(Environment.USER, "postgres");
                settings.put(Environment.PASS, "123456");
                settings.put(Environment.HBM2DDL_AUTO, "update");
                settings.put(Environment.SHOW_SQL, true);

                settings.put("hibernate.hikari.connectionTimeout", "20000");
                settings.put("hibernate.hikari.minimumIdle", "10");
                settings.put("hibernate.hikari.maximumPoolSize", "20");
                settings.put("hibernate.hikari.idleTimeout", "300000");
                registryBuilder.applySettings(settings);
                registry = registryBuilder.build();
                logger.info("Hibernate Registry builder created.");

                MetadataSources sources = new MetadataSources(registry);
                sources.addAnnotatedClass(PaymentRecord.class);
                Metadata metadata = sources.getMetadataBuilder().build();
                sessionFactory = metadata.getSessionFactoryBuilder().build();

            } catch (Exception ex) {
                logger.error("SessionFactory creation failed", ex);
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
            }

        }
        return sessionFactory;
    }

    public void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
