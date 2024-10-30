package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.net.HttpURLConnection;

public class PlayerInfoRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {

    public PlayerInfoRequestHandler(ServerObjects objects) {
        super(objects);
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request,
                                    ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
            String participantId = getQueryParameterSafely(queryDecoder, FormParameter.participantId);
            User resourceOwner = getResourceOwnerSafely(request, participantId);

            responseWriter.writeJsonResponse(JsonUtils.toJsonString(resourceOwner.GetUserInfo()));

        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }
}