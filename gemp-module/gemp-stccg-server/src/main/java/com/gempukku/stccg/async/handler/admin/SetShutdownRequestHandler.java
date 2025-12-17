package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.hall.HallServer;

public class SetShutdownRequestHandler implements UriRequestHandler, AdminRequestHandler {
    private final boolean _shutdown;

    SetShutdownRequestHandler(
        @JsonProperty(value = "shutdown", required = true)
        boolean shutdown
    ) {
        _shutdown = shutdown;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        HallServer hallServer = serverObjects.getHallServer();
        ChatServer chatServer = serverObjects.getChatServer();
        validateAdmin(request);
        hallServer.setShutdown(_shutdown, chatServer);
        responseWriter.writeJsonOkResponse();
    }
}