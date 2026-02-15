package com.gempukku.stccg.async.handler.game;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameServer;

public class CancelGameRequestHandler implements UriRequestHandler {

    private final CardGameMediator _mediator;

    CancelGameRequestHandler(
            @JsonProperty("gameId")
            String gameId,
            @JacksonInject GameServer gameServer
    ) throws HttpProcessingException {
        _mediator = gameServer.getGameById(gameId);
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        String userName = request.userName();
        _mediator.cancel(userName);
        responseWriter.writeJsonOkResponse();
    }

}