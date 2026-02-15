package com.gempukku.stccg.async.handler.game;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameServer;

public class GameCardInfoRequestHandler implements UriRequestHandler {
    private final int _cardId;
    private final CardGameMediator _mediator;

    private GameCardInfoRequestHandler(
            @JsonProperty(value = "gameId", required = true)
            String gameId,
            @JsonProperty(value = "cardId", required = true)
            int cardId,
            @JacksonInject GameServer gameServer
            ) throws HttpProcessingException {
        _mediator = gameServer.getGameById(gameId);
        _cardId = cardId;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        responseWriter.writeJsonResponse(_mediator.produceCardInfo(_cardId));
    }

}