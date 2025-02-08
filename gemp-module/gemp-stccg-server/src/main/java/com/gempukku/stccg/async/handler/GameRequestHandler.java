package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.GameServer;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.net.HttpURLConnection;

public class GameRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final GameServer _gameServer;

    public GameRequestHandler(ServerObjects objects) {
        super(objects);
        _gameServer = objects.getGameServer();
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp)
            throws Exception {
        if (uri.startsWith("/") && uri.endsWith("/gameState/admin") && request.method() == HttpMethod.GET) {
            getGameState(uri, request, uri.substring(1, uri.length() - 16), responseWriter);
        } else if (uri.startsWith("/") && uri.endsWith("/gameState/player") && request.method() == HttpMethod.GET) {
            getGameState(uri, request, uri.substring(1, uri.length() - 17), responseWriter);
        } else if (uri.startsWith("/") && uri.endsWith("/gameState/player1") && request.method() == HttpMethod.GET) {
            getGameState(uri, request, uri.substring(1, uri.length() - 18), responseWriter);
        } else if (uri.startsWith("/") && uri.endsWith("/gameState/player2") && request.method() == HttpMethod.GET) {
            getGameState(uri, request, uri.substring(1, uri.length() - 18), responseWriter);
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private void getGameState(String uri, HttpRequest request, String gameId, ResponseWriter responseWriter)
            throws HttpProcessingException {

        User resourceOwner = getResourceOwnerSafely(request);

        boolean userCanAccess = resourceOwner.isAdmin() || uri.endsWith("player");
        if (!userCanAccess) {
            // Throw 401 error if user does not have access
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED);
        }

        CardGameMediator gameMediator = _gameServer.getGameById(gameId); // throws a 404 error if game can't be found

        try {
            DefaultGame cardGame = gameMediator.getGame();
            String gameStateString;
                // Methods below may throw JsonProcessingExceptions
            if (uri.endsWith("admin")) {
                gameStateString = gameMediator.serializeCompleteGameState();
            } else if (uri.endsWith("player")) {
                gameStateString = gameMediator.serializeGameStateForPlayer(resourceOwner.getName());
            } else if (uri.endsWith("player1")) {
                gameStateString = gameMediator.serializeGameStateForPlayer(cardGame.getPlayerId(1));
            } else if (uri.endsWith("player2")) {
                gameStateString = gameMediator.serializeGameStateForPlayer(cardGame.getPlayerId(2));
            } else {
                // Throws a 404 error if the URL provided doesn't go anywhere
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND);
            }
            responseWriter.writeJsonResponse(gameStateString);
        } catch(JsonProcessingException exp) {
                // Throws a 500 error if the serialization doesn't work
            throw new HttpProcessingException(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }


}