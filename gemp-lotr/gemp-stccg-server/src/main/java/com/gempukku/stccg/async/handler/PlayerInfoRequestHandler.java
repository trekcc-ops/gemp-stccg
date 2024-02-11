package com.gempukku.stccg.async.handler;

import com.alibaba.fastjson.JSON;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ResponseWriter;
import com.gempukku.stccg.game.User;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.lang.reflect.Type;
import java.util.Map;

public class PlayerInfoRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {

    public PlayerInfoRequestHandler(Map<Type, Object> context) {
        super(context);

    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
            String participantId = getQueryParameterSafely(queryDecoder, "participantId");
            User resourceOwner = getResourceOwnerSafely(request, participantId);

            responseWriter.writeJsonResponse(JSON.toJSONString(resourceOwner.GetUserInfo()));

        } else {
            throw new HttpProcessingException(404);
        }
    }




}
