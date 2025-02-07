package com.gempukku.stccg.async.handler.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import io.netty.handler.codec.http.HttpRequest;

public class ConcedeGameRequestHandler implements UriRequestHandlerNew {
    private final String _gameId;

    ConcedeGameRequestHandler(
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
        CardGameMediator gameMediator = serverObjects.getGameServer().getGameById(_gameId);
        gameMediator.concede(resourceOwner);
        responseWriter.writeXmlOkResponse();
    }

}