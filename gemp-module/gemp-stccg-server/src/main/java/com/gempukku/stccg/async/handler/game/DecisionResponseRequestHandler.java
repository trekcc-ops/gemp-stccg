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
    private final int _decisionId;
    private final String _decisionValue;

    DecisionResponseRequestHandler(
            @JsonProperty(value = "gameId", required = true)
            String gameId,
            @JsonProperty(value = "channelNumber", required = true)
            int channelNumber,
            @JsonProperty(value = "decisionId", required = true)
            int decisionId,
            @JsonProperty(value = "decisionValue", required = true)
            String decisionValue
    ) {
        super(gameId);
        _channelNumber = channelNumber;
        _decisionId = decisionId;
        _decisionValue = decisionValue;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        String userName = request.userName();
        CardGameMediator gameMediator = serverObjects.getGameServer().getGameById(_gameId);
        gameMediator.setPlayerAutoPassSettings(userName, getAutoPassPhases(request));
        gameMediator.playerAnswered(userName, _channelNumber, _decisionId, _decisionValue);
        GameCommunicationChannel commChannel =
                gameMediator.getCommunicationChannel(resourceOwner, _channelNumber);
        LongPollingResource pollingResource =
                new GameUpdateLongPollingResource(commChannel, gameMediator, responseWriter);
        serverObjects.getLongPollingSystem().processLongPollingResource(pollingResource, commChannel);
    }

}