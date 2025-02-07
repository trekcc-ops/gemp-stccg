package com.gempukku.stccg.async.handler.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameCommunicationChannel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StartGameSessionRequestHandler implements UriRequestHandlerNew {
    private final String _gameId;

    StartGameSessionRequestHandler(
            @JsonProperty("gameId")
            String gameId
    ) {
        _gameId = gameId;
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp,
                                    ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = getResourceOwnerSafely(request, serverObjects);

        CardGameMediator gameMediator = serverObjects.getGameServer().getGameById(_gameId); // throws 404 error if not found

        gameMediator.setPlayerAutoPassSettings(resourceOwner.getName(), getAutoPassPhases(request));

        // may throw 403 error
        GameCommunicationChannel channel = gameMediator.signupUserForGameAndGetChannel(resourceOwner);
        String xmlString = gameMediator.serializeEventsToString(channel);
        responseWriter.writeXmlResponseWithNoHeaders(xmlString);
    }

    private Set<Phase> getAutoPassPhases(HttpMessage request) {
        ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;
        String cookieHeader = request.headers().get(HttpHeaderNames.COOKIE);
        if (cookieHeader != null) {
            Set<Cookie> cookies = cookieDecoder.decode(cookieHeader);
            for (Cookie cookie : cookies) {
                if ("autoPassPhases".equals(cookie.name())) {
                    final String[] phases = cookie.value().split("0");
                    Set<Phase> result = new HashSet<>();
                    for (String phase : phases)
                        result.add(Phase.valueOf(phase.toUpperCase().replace(" ","_")));
                    return result;
                }
            }
            for (Cookie cookie : cookies) {
                if ("autoPass".equals(cookie.name()) && "false".equals(cookie.value()))
                    return Collections.emptySet();
            }
        }
        Set<Phase> defaultResult = new HashSet<>();
        defaultResult.add(Phase.EXECUTE_ORDERS);
        return defaultResult;
    }


}