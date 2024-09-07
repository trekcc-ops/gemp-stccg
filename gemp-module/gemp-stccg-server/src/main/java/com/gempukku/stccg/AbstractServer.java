package com.gempukku.stccg;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.sql.SQLException;

public abstract class AbstractServer {
    private static final Logger LOGGER = LogManager.getLogger(AbstractServer.class);
    private static final ServerCleaner _cleaningTask = new ServerCleaner();

    private boolean _started;

    public void startServer() {
        LOGGER.debug("Starting startServer function for " + getClass().getSimpleName());
        if (!_started) {
            _cleaningTask.addServer(this);
            _started = true;
            LOGGER.debug("Started: "+getClass().getSimpleName());
            doAfterStartup();
        }
    }

    protected void doAfterStartup() {

    }

    public void stopServer() {
        if (_started) {
            _cleaningTask.removeServer(this);
            _started = false;
            LOGGER.debug("Stopped: "+getClass().getSimpleName());
        }
    }

    protected abstract void cleanup() throws SQLException, IOException;
}
