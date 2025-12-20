package com.gempukku.stccg.async.handler.game;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.GameServer;

import java.net.HttpURLConnection;

public class GetGameStateRequestHandler implements UriRequestHandler {

    private final GameServer _gameServer;
    private final String _gameId; // May not be useful for this handler

    public GetGameStateRequestHandler(
            @JsonProperty("gameId")
            String gameId,
            @JacksonInject GameServer gameServer) {
        _gameServer = gameServer;
        _gameId = gameId;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        String uri = request.uriWithoutParameters();
        String userType = uri.substring(uri.lastIndexOf("/")).replace("/","");
        String gameIdToUse = (_gameId != null)? _gameId :
                uri.substring(uri.indexOf("getGameState/"))
                        .replace("getGameState","")
                        .replace(userType,"")
                        .replace("/","");
        String userId = request.userName();

        boolean userCanAccess = request.userIsAdmin() || userType.equals("thisPlayer");
        if (!userCanAccess) {
            // Throw 401 error if user does not have access
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED);
        }

        // throws a 404 error if game can't be found
        CardGameMediator gameMediator = _gameServer.getGameById(gameIdToUse);

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