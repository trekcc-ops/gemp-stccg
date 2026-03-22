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
import com.gempukku.stccg.game.GameCommunicationChannel;
import com.gempukku.stccg.game.GameServer;

public class UpdateGameStateRequestHandler implements GameRequestHandler, UriRequestHandler {
    private final int _channelNumber;
    private final CardGameMediator _mediator;
    private final LongPollingSystem _longPollingSystem;

    UpdateGameStateRequestHandler(
            @JsonProperty(value = "gameId", required = true)
            String gameId,
            @JsonProperty(value = "channelNumber", required = true)
            int channelNumber,
            @JacksonInject GameServer gameServer,
            @JacksonInject LongPollingSystem longPollingSystem) throws HttpProcessingException {
        _mediator = gameServer.getGameById(gameId);
        _channelNumber = channelNumber;
        _longPollingSystem = longPollingSystem;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter) throws Exception {
        User resourceOwner = request.user();
        String userName = request.userName();
        _mediator.setPlayerAutoPassSettings(userName, getAutoPassPhases(request));
        validateUserCanAccessGameState(resourceOwner, _mediator);
        GameCommunicationChannel commChannel = _mediator.getCommunicationChannel(resourceOwner, _channelNumber);
        LongPollingResource pollingResource =
                new GameUpdateLongPollingResource(commChannel, _mediator, responseWriter);
        pollingResource.processInSystem(_longPollingSystem);
    }

}