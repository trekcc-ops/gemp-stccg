package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.hall.HallServer;

public class SetDailyMessageRequestHandler implements UriRequestHandler, AdminRequestHandler {
    
    private final String _newMessage;
    private final HallServer _hallServer;
    SetDailyMessageRequestHandler(
        @JsonProperty(value = "newMessage", required = true)
        String newMessage,
        @JacksonInject HallServer hallServer
    ) {
        _newMessage = newMessage;
        _hallServer = hallServer;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        validateAdmin(request);
        _hallServer.setDailyMessage(_newMessage);
        responseWriter.writeJsonOkResponse();
    }
}