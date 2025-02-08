package com.gempukku.stccg.async.handler.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.GameServer;
import io.netty.handler.codec.http.HttpRequest;

public class CancelGameRequestHandler extends GameRequestHandlerNew implements UriRequestHandlerNew {

    CancelGameRequestHandler(
            @JsonProperty("gameId")
            String gameId
    ) {
        super(gameId);
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp,
                                    ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = getResourceOwnerSafely(request, serverObjects);
        GameServer gameServer = serverObjects.getGameServer();
        gameServer.cancelGame(resourceOwner, _gameId);
        responseWriter.writeXmlOkResponse();
    }

}