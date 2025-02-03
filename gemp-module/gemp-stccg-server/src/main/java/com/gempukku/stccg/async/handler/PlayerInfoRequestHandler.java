package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.net.HttpURLConnection;

public class PlayerInfoRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {

    public PlayerInfoRequestHandler(ServerObjects objects) {
        super(objects);
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request,
                                    ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            User resourceOwner = getUserIdFromCookiesOrUri(request);
            String jsonString = _jsonMapper.writeValueAsString(resourceOwner.GetUserInfo());
            responseWriter.writeJsonResponse(jsonString);
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }
}