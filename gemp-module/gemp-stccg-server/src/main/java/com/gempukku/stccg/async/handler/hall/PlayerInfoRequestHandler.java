package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.net.HttpURLConnection;

@JsonIgnoreProperties("participantId")
public class PlayerInfoRequestHandler implements UriRequestHandlerNew {

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        String jsonString = new ObjectMapper().writeValueAsString(resourceOwner.GetUserInfo());
        responseWriter.writeJsonResponse(jsonString);
    }
}