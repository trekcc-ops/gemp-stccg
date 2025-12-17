package com.gempukku.stccg.async.handler.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;

public class StartGameSessionRequestHandler extends GameRequestHandlerNew implements UriRequestHandler {

    StartGameSessionRequestHandler(
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

        CardGameMediator gameMediator = serverObjects.getGameServer().getGameById(_gameId); // throws 404 error if not found

        gameMediator.setPlayerAutoPassSettings(userName, getAutoPassPhases(request));

        // may throw 403 error
        String jsonString = gameMediator.signupUserForGameAndGetGameState(resourceOwner);
        responseWriter.writeJsonResponse(jsonString);
    }

}