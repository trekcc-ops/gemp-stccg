package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.hall.HallServer;

public class SetDailyMessageRequestHandler implements UriRequestHandler, AdminRequestHandler {
    
    private final String _newMessage;
    SetDailyMessageRequestHandler(
        @JsonProperty(value = "newMessage", required = true)
        String newMessage
    ) {
        _newMessage = newMessage;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        validateAdmin(request);
        HallServer hallServer = serverObjects.getHallServer();
        hallServer.setDailyMessage(_newMessage);
        responseWriter.writeJsonOkResponse();
    }
}