package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.database.DeckDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.FormatLibrary;

import java.net.HttpURLConnection;

public class RenameDeckRequestHandler extends DeckRequestHandler implements UriRequestHandler {

    private final String _newDeckName;
    private final String _oldDeckName;
    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final FormatLibrary _formatLibrary;
    private final DeckDAO _deckDAO;

    RenameDeckRequestHandler(
            @JsonProperty("deckName")
            String newDeckName,
            @JsonProperty("oldDeckName")
            String oldDeckName,
            @JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
            @JacksonInject FormatLibrary formatLibrary,
            @JacksonInject DeckDAO deckDAO) {
        _newDeckName = newDeckName;
        _oldDeckName = oldDeckName;
        _cardBlueprintLibrary = cardBlueprintLibrary;
        _formatLibrary = formatLibrary;
        _deckDAO = deckDAO;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        User resourceOwner = request.user();
        CardDeck deck = _deckDAO.renameDeck(resourceOwner, _oldDeckName, _newDeckName);
        if (deck == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        String jsonString =
                _jsonMapper.writeValueAsString(new JsonSerializedDeck(deck, _cardBlueprintLibrary, _formatLibrary));
        responseWriter.writeJsonResponse(jsonString);
    }

}