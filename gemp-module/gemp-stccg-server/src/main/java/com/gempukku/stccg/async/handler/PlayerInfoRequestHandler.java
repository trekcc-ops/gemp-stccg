package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.net.HttpURLConnection;

@JsonIgnoreProperties("participantId")
public class PlayerInfoRequestHandler implements UriRequestHandlerNew {

    @Override
    public final void handleRequest(String uri, HttpRequest request,
                                    ResponseWriter responseWriter, String remoteIp, ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = getResourceOwnerSafely(request, serverObjects);
        String jsonString = new ObjectMapper().writeValueAsString(resourceOwner.GetUserInfo());
        responseWriter.writeJsonResponse(jsonString);
    }
}