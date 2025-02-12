package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.database.User;

import java.net.HttpURLConnection;

public class RenameDeckRequestHandler extends DeckRequestHandler implements UriRequestHandlerNew {

    private final String _newDeckName;
    private final String _oldDeckName;

    RenameDeckRequestHandler(
            @JsonProperty("deckName")
            String newDeckName,
            @JsonProperty("oldDeckName")
            String oldDeckName
    ) {
        _newDeckName = newDeckName;
        _oldDeckName = oldDeckName;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        CardDeck deck = serverObjects.getDeckDAO().renameDeck(resourceOwner, _oldDeckName, _newDeckName);
        if (deck == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        String jsonString = _jsonMapper.writeValueAsString(new JsonSerializedDeck(deck, serverObjects));
        responseWriter.writeJsonResponse(jsonString);
    }

}