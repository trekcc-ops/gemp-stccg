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
import com.gempukku.stccg.game.GameServer;

public class UpdateGameStateRequestHandler extends GameRequestHandlerNew implements UriRequestHandler {
    private final int _channelNumber;

    UpdateGameStateRequestHandler(
            @JsonProperty(value = "gameId", required = true)
            String gameId,
            @JsonProperty(value = "channelNumber", required = true)
            int channelNumber
    ) {
        super(gameId);
        _channelNumber = channelNumber;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        GameServer gameServer = serverObjects.getGameServer();
        gameServer.setPlayerAutoPassSettings(resourceOwner, _gameId, getAutoPassPhases(request));


        CardGameMediator gameMediator = serverObjects.getGameServer().getGameById(_gameId);
        GameCommunicationChannel commChannel =
                gameMediator.getCommunicationChannel(resourceOwner, _channelNumber);
        LongPollingResource pollingResource =
                new GameUpdateLongPollingResource(commChannel, gameMediator, responseWriter);
        serverObjects.getLongPollingSystem().processLongPollingResource(pollingResource, commChannel);
    }

}