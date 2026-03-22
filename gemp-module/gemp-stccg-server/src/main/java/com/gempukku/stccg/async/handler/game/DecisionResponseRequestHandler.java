package com.gempukku.stccg.async.handler.game;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.LongPollingResource;
import com.gempukku.stccg.async.LongPollingSystem;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameServer;

public class DecisionResponseRequestHandler implements GameRequestHandler, UriRequestHandler {
    private final int _channelNumber;
    private final int _decisionId;
    private final String _decisionValue;
    private final CardGameMediator _mediator;
    private final LongPollingSystem _longPollingSystem;

    DecisionResponseRequestHandler(
            @JsonProperty(value = "gameId", required = true)
            String gameId,
            @JsonProperty(value = "channelNumber", required = true)
            int channelNumber,
            @JsonProperty(value = "decisionId", required = true)
            int decisionId,
            @JsonProperty(value = "decisionValue", required = true)
            String decisionValue,
            @JacksonInject GameServer gameServer,
            @JacksonInject LongPollingSystem longPollingSystem
            ) throws HttpProcessingException {
        _channelNumber = channelNumber;
        _decisionId = decisionId;
        _decisionValue = decisionValue;
        _mediator = gameServer.getGameById(gameId);
        _longPollingSystem = longPollingSystem;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        User resourceOwner = request.user();
        String userName = request.userName();
        _mediator.setPlayerAutoPassSettings(userName, getAutoPassPhases(request));
        _mediator.playerAnswered(userName, _channelNumber, _decisionId, _decisionValue);
        validateUserCanAccessGameState(resourceOwner, _mediator);
        LongPollingResource pollingResource = new GameUpdateLongPollingResource(_mediator, resourceOwner,
                responseWriter, _channelNumber);
        pollingResource.processInSystem(_longPollingSystem);
    }

}