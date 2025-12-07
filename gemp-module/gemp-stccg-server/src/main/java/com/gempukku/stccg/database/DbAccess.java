package com.gempukku.stccg.database;

import com.gempukku.stccg.common.AppConfig;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.Properties;

public class DbAccess extends PoolingDataSource {
    private static final Logger LOGGER = LogManager.getLogger(DbAccess.class);
    private static final String CONNECTION_URL = AppConfig.getProperty("db.connection.url");
    private static final String CONNECTION_USERNAME = AppConfig.getProperty("db.connection.username");
    private static final String CONNECTION_PASSWORD = AppConfig.getProperty("db.connection.password");
    private static final String VALIDATION_QUERY = AppConfig.getProperty("db.connection.validateQuery");
    private static final boolean REWRITE_BATCHED_STATEMENTS = false;
    private static final int INNODB_AUTOINC_LOCK_MODE = 2;
    private static final int MAX_CONNECTION_ATTEMPTS = 200;
    private static final int SLEEP_TIME_BETWEEN_ATTEMPTS = 500; // in milliseconds

    public DbAccess() {
        try {
            Class.forName(AppConfig.getProperty("db.connection.class"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find the DB driver", e);
        }
        this._pool = createConnectionPool();

        if (!isConnectionEstablished()) {
            throw new RuntimeException("Couldn't establish database connection");
        }
    }

    public final PoolingDataSource getDataSource() {
        return this;
    }

    private static GenericObjectPool createConnectionPool() {
        Properties props = new Properties() {{
            setProperty("user", CONNECTION_USERNAME);
            setProperty("password", CONNECTION_PASSWORD);
            setProperty("rewriteBatchedStatements", String.valueOf(REWRITE_BATCHED_STATEMENTS));
            setProperty("innodb_autoinc_lock_mode", String.valueOf(INNODB_AUTOINC_LOCK_MODE));
        }};

        GenericObjectPool connectionPool = new GenericObjectPool();
        connectionPool.setTestOnBorrow(true);

        PoolableObjectFactory objectFactory = new PoolableConnectionFactory(
                new DriverManagerConnectionFactory(CONNECTION_URL, props),
                connectionPool,
                null,
                VALIDATION_QUERY,
                false,
                true
        );
        connectionPool.setFactory(objectFactory);
        return connectionPool;
    }

    private boolean isConnectionEstablished() {
        boolean connected = false;
        int attemptNum = 1;
        while (!connected && attemptNum <= MAX_CONNECTION_ATTEMPTS) {
            try {
                getConnection();
                connected = true;
                LOGGER.debug("Database connection successfully established.");
            } catch (SQLException exp) {
                LOGGER.debug("Unable to establish database connection. Trying again...");
                attemptNum++;
                try {
                    Thread.sleep(SLEEP_TIME_BETWEEN_ATTEMPTS);
                } catch(InterruptedException ignored) {

                }
            }
        }
        return connected;
    }

}