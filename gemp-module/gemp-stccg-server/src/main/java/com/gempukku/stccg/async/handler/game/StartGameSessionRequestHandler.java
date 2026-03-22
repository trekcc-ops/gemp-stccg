package com.gempukku.stccg.async.handler.game;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameServer;

public class StartGameSessionRequestHandler implements GameRequestHandler, UriRequestHandler {

    private final CardGameMediator _mediator;

    StartGameSessionRequestHandler(
            @JsonProperty("gameId")
            String gameId,
            @JacksonInject GameServer gameServer) throws HttpProcessingException {
        _mediator = gameServer.getGameById(gameId);
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        User resourceOwner = request.user();
        String userName = request.userName();
        _mediator.setPlayerAutoPassSettings(userName, getAutoPassPhases(request));
        validateUserCanAccessGameState(resourceOwner, _mediator);
        String jsonString = _mediator.signupUserForGameAndGetGameState(userName);
        responseWriter.writeJsonResponse(jsonString);
    }

}