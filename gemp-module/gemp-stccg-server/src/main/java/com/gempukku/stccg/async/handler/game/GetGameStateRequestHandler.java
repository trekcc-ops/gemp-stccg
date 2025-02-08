package com.gempukku.stccg.async.handler.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.DefaultGame;
import io.netty.handler.codec.http.HttpRequest;

import java.net.HttpURLConnection;

public class GetGameStateRequestHandler extends GameRequestHandlerNew implements UriRequestHandlerNew {

    public GetGameStateRequestHandler(
            @JsonProperty("gameId")
            String gameId
    ) {
        super(gameId);
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp,
                                    ServerObjects serverObjects)
            throws Exception {

        String userType = uri.substring(uri.lastIndexOf("/")).replace("/","");
        String gameIdToUse = (_gameId != null)? _gameId :
                uri.substring(uri.indexOf("getGameState/"))
                        .replace("getGameState","")
                        .replace(userType,"")
                        .replace("/","");

        User resourceOwner = getResourceOwnerSafely(request, serverObjects);
        String userId = resourceOwner.getName();

        boolean userCanAccess = resourceOwner.isAdmin() || userType.equals("thisPlayer");
        if (!userCanAccess) {
            // Throw 401 error if user does not have access
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED);
        }

        // throws a 404 error if game can't be found
        CardGameMediator gameMediator = serverObjects.getGameServer().getGameById(gameIdToUse);

        try {
            DefaultGame cardGame = gameMediator.getGame();
            String gameStateString = switch(userType) {
                case "admin" -> gameMediator.serializeCompleteGameState();
                case "player1" -> gameMediator.serializeGameStateForPlayer(cardGame.getPlayerId(1));
                case "player2" -> gameMediator.serializeGameStateForPlayer(cardGame.getPlayerId(2));
                case "thisPlayer" -> gameMediator.serializeGameStateForPlayer(userId);
                default -> throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND);
            };
            responseWriter.writeJsonResponse(gameStateString);
        } catch(JsonProcessingException exp) {
            // Throws a 500 error if the serialization doesn't work
            throw new HttpProcessingException(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }

    }


}