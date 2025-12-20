package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.hall.HallServer;

public class SetShutdownRequestHandler implements UriRequestHandler, AdminRequestHandler {
    private final boolean _shutdown;
    private final ChatServer _chatServer;
    private final HallServer _hallServer;

    SetShutdownRequestHandler(
        @JsonProperty(value = "shutdown", required = true)
        boolean shutdown,
        @JacksonInject HallServer hallServer,
        @JacksonInject ChatServer chatServer
    ) {
        _shutdown = shutdown;
        _hallServer = hallServer;
        _chatServer = chatServer;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        validateAdmin(request);
        _hallServer.setShutdown(_shutdown, _chatServer);
        responseWriter.writeJsonOkResponse();
    }
}