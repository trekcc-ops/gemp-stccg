package com.gempukku.stccg.async.handler.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameCommunicationChannel;
import io.netty.handler.codec.http.HttpRequest;

public class StartGameSessionRequestHandler extends GameRequestHandlerNew implements UriRequestHandlerNew {

    StartGameSessionRequestHandler(
            @JsonProperty("gameId")
            String gameId
    ) {
        super(gameId);
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp,
                                    ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = getResourceOwnerSafely(request, serverObjects);

        CardGameMediator gameMediator = serverObjects.getGameServer().getGameById(_gameId); // throws 404 error if not found

        gameMediator.setPlayerAutoPassSettings(resourceOwner, getAutoPassPhases(request));

        // may throw 403 error
        GameCommunicationChannel channel = gameMediator.signupUserForGameAndGetChannel(resourceOwner);
        String xmlString = gameMediator.serializeEventsToString(channel);
        responseWriter.writeJsonResponse(xmlString);
    }

}