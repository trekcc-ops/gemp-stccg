package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.User;

@JsonIgnoreProperties("participantId")
public class PlayerInfoRequestHandler implements UriRequestHandler {

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        User resourceOwner = request.user();
        String jsonString = new ObjectMapper().writeValueAsString(resourceOwner.GetUserInfo());
        responseWriter.writeJsonResponse(jsonString);
    }
}