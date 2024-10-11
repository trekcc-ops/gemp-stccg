package com.gempukku.stccg;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;

public abstract class AbstractServer {
    private static final Logger LOGGER = LogManager.getLogger(AbstractServer.class);
    private static final ServerCleaner _cleaningTask = new ServerCleaner();

    private boolean _started;

    public void startServer() {
        basicStartup();
    }
    protected void basicStartup() {
        LOGGER.debug("Starting startServer function for " + this);
        if (!_started) {
            _cleaningTask.addServer(this);
            _started = true;
            LOGGER.debug("Started: " + this);
        }
    }
    protected abstract void cleanup() throws SQLException, IOException;
    @Override
    public final String toString() {
        Class<? extends AbstractServer> thisClass = getClass();
        return thisClass.getSimpleName();
    }
}