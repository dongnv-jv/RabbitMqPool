package vn.vnpay.rabbitmq.config.database;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.rabbitmq.bean.PaymentRecord;

import java.util.HashMap;
import java.util.Map;
public class DatabaseConnectionPool {
    private Logger logger = LoggerFactory.getLogger(DatabaseConnectionPool.class);

    private StandardServiceRegistry registry;
    private SessionFactory sessionFactory;
    private volatile static DatabaseConnectionPool databaseConnectionPool;

    public static DatabaseConnectionPool getInstance() {
        if (databaseConnectionPool == null) {
            throw new IllegalStateException("DatabaseConnectionPool not initialized. Call init() before getInstance()");
        }
        return databaseConnectionPool;
    }

    public static void initDatabaseConnectionPool(String driver, String url, String username, String password, String ddlAuto, boolean showSql) {
        if (databaseConnectionPool == null) {
            synchronized (DatabaseConnectionPool.class) {
                if (databaseConnectionPool == null) {
                    databaseConnectionPool = new DatabaseConnectionPool(driver, url, username, password, ddlAuto, showSql);
                }
            }
        }
    }

    public DatabaseConnectionPool(String driver, String url, String username, String password, String ddlAuto, boolean showSql) {
        if (sessionFactory == null) {
            try {
                StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
                Map<String, Object> settings = new HashMap<>();
                settings.put(AvailableSettings.DRIVER, driver);
                settings.put(AvailableSettings.URL, url);
                settings.put(AvailableSettings.USER, username);
                settings.put(AvailableSettings.PASS, password);
                settings.put(AvailableSettings.HBM2DDL_AUTO, ddlAuto);
                settings.put(AvailableSettings.SHOW_SQL, showSql);

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
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
