package com.gempukku.stccg.async.handler.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.LongPollingResource;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameCommunicationChannel;
import io.netty.handler.codec.http.HttpRequest;

public class DecisionResponseRequestHandler extends GameRequestHandlerNew implements UriRequestHandlerNew {
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
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp,
                                    ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = getResourceOwnerSafely(request, serverObjects);
        CardGameMediator gameMediator = serverObjects.getGameServer().getGameById(_gameId);
        gameMediator.setPlayerAutoPassSettings(resourceOwner, getAutoPassPhases(request));

        gameMediator.playerAnswered(resourceOwner, _channelNumber, _decisionId, _decisionValue);
        GameCommunicationChannel commChannel =
                gameMediator.getCommunicationChannel(resourceOwner, _channelNumber);
        LongPollingResource pollingResource =
                new GameUpdateLongPollingResource(commChannel, gameMediator, responseWriter);
        serverObjects.getLongPollingSystem().processLongPollingResource(pollingResource, commChannel);
    }

}