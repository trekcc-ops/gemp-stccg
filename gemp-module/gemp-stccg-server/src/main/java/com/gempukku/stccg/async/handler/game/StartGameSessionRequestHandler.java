package com.gempukku.stccg.async.handler.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameCommunicationChannel;
import io.netty.handler.codec.http.HttpRequest;

public class StartGameSessionRequestHandler extends GameRequestHandlerNew implements UriRequestHandlerNew {

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

        CardGameMediator gameMediator = serverObjects.getGameServer().getGameById(_gameId); // throws 404 error if not found

        gameMediator.setPlayerAutoPassSettings(resourceOwner, getAutoPassPhases(request));

        // may throw 403 error
        String jsonString = gameMediator.signupUserForGameAndGetGameState(resourceOwner);
        responseWriter.writeJsonResponse(jsonString);
    }

}