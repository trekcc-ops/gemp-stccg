package com.gempukku.stccg.async.handler.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.game.CardGameMediator;

public class GameCardInfoRequestHandler extends GameRequestHandlerNew implements UriRequestHandler {
    private final int _cardId;

    GameCardInfoRequestHandler(
            @JsonProperty(value = "gameId", required = true)
            String gameId,
            @JsonProperty(value = "cardId", required = true)
            int cardId
    ) {
        super(gameId);
        _cardId = cardId;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter,
                                    ServerObjects serverObjects)
            throws Exception {
            // getGameById throws 404 error if not found
        CardGameMediator gameMediator = serverObjects.getGameServer().getGameById(_gameId);
        responseWriter.writeJsonResponse(gameMediator.produceCardInfo(_cardId));
    }

}