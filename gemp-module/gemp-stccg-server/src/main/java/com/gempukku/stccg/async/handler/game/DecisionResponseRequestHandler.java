package com.gempukku.stccg.async.handler.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.LongPollingResource;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameCommunicationChannel;

public class DecisionResponseRequestHandler extends GameRequestHandlerNew implements UriRequestHandler {
    private final int _channelNumber;
    private final DecisionResponse _response;

    DecisionResponseRequestHandler(
            @JsonProperty(value = "gameId", required = true)
            String gameId,
            @JsonProperty(value = "channelNumber", required = true)
            int channelNumber,
            @JsonProperty(value = "decisionResponse", required = true)
            DecisionResponse response
    ) {
        super(gameId);
        _channelNumber = channelNumber;
        _response = response;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        CardGameMediator gameMediator = serverObjects.getGameServer().getGameById(_gameId);
        gameMediator.setPlayerAutoPassSettings(resourceOwner, getAutoPassPhases(request));

        gameMediator.playerAnswered(resourceOwner, _channelNumber, _response.getId(), _response.getValue());
        GameCommunicationChannel commChannel =
                gameMediator.getCommunicationChannel(resourceOwner, _channelNumber);
        LongPollingResource pollingResource =
                new GameUpdateLongPollingResource(commChannel, gameMediator, responseWriter);
        serverObjects.getLongPollingSystem().processLongPollingResource(pollingResource, commChannel);
    }

    private static class DecisionResponse {
        @JsonProperty("id")
        int _decisionId;
        @JsonProperty("value")
        String _decisionValue;

        public int getId() {
            return _decisionId;
        }

        public String getValue() {
            return _decisionValue;
        }
    }

}