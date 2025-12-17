package com.gempukku.stccg.async.handler.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;

public class ConcedeGameRequestHandler extends GameRequestHandlerNew implements UriRequestHandler {

    ConcedeGameRequestHandler(
            @JsonProperty("gameId")
            String gameId
    ) {
        super(gameId);
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        String userName = request.userName();
        CardGameMediator gameMediator = serverObjects.getGameServer().getGameById(_gameId);
        gameMediator.concede(userName);
        responseWriter.writeJsonOkResponse();
    }

}