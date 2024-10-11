package com.gempukku.stccg.db;

import com.gempukku.stccg.common.AppConfig;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.Properties;

public class DbAccess {
    private static final Logger LOGGER = LogManager.getLogger(DbAccess.class);
    private final PoolingDataSource _dataSource;

    public DbAccess() {
        this(AppConfig.getProperty("db.connection.url"), AppConfig.getProperty("db.connection.username"), AppConfig.getProperty("db.connection.password"), false);
    }

    public DbAccess(String url, String user, String pass, boolean batch) {
        LOGGER.debug("Creating DbAccess for " + url);
        try {
            Class.forName(AppConfig.getProperty("db.connection.class"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find the DB driver", e);
        }

        _dataSource = setupDataSource(url, user, pass, batch);
        LOGGER.debug("DbAccess - _dataSource created for " + url);
    }

    public PoolingDataSource getDataSource() {
        return _dataSource;
    }

    private PoolingDataSource setupDataSource(String connectURI, String user, String pass, Boolean batch) {
        //
        // First, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        Properties props = new Properties() {{
            setProperty("user", user);
            setProperty("password", pass);
            setProperty("rewriteBatchedStatements", batch.toString().toLowerCase());
            setProperty("innodb_autoinc_lock_mode", "2");
        }};
        ConnectionFactory connectionFactory =
                new DriverManagerConnectionFactory(connectURI, props);

        //
        // Now we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        GenericObjectPool connectionPool =
                new GenericObjectPool();
        connectionPool.setTestOnBorrow(true);

        //
        // Next we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        connectionPool.setFactory(
                new PoolableConnectionFactory(
                        connectionFactory, connectionPool, null,
                        AppConfig.getProperty("db.connection.validateQuery"), false, true
                )
        );

        //
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        //

        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
        try {
            dataSource.getConnection();
            LOGGER.debug("setupDataSource - connection successfully created");
        } catch(SQLException exp) {
            LOGGER.debug("setupDataSource - unable to connect");
        }

        return dataSource;
    }
}
