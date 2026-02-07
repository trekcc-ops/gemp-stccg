package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.database.DeckDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.FormatLibrary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class ListUserDecksRequestHandler extends DeckRequestHandler implements UriRequestHandler {

    private static final Logger LOGGER = LogManager.getLogger(ListLibraryDecksRequestHandler.class);
    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final FormatLibrary _formatLibrary;
    private final DeckDAO _deckDAO;

    ListUserDecksRequestHandler(@JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
                                @JacksonInject FormatLibrary formatLibrary,
                                @JacksonInject DeckDAO deckDAO) {
        _cardBlueprintLibrary = cardBlueprintLibrary;
        _formatLibrary = formatLibrary;
        _deckDAO = deckDAO;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        User user = request.user();
        List<CardDeck> userDecks = _deckDAO.getUserDecks(user);

        TreeSet<JsonSerializedDeck> jsonDecks = new TreeSet<>(new UserJsonDeckSorter());
        userDecks.forEach(deck -> jsonDecks.add(new JsonSerializedDeck(deck, _cardBlueprintLibrary, _formatLibrary)));
        for (CardDeck deck : userDecks) {
            JsonSerializedDeck jsonDeck = new JsonSerializedDeck(deck, _cardBlueprintLibrary, _formatLibrary);
            try {
                _jsonMapper.writeValueAsString(jsonDeck);
                jsonDecks.add(jsonDeck);
            } catch(JsonProcessingException exp) {
                LOGGER.error("Unable to serialize deck '" + deck.getDeckName() + "'", exp);
            }
        }
        String jsonString = _jsonMapper.writeValueAsString(jsonDecks);
        responseWriter.writeJsonResponse(jsonString);
    }

    private static class UserJsonDeckSorter implements Comparator<JsonSerializedDeck> {

        public int compare(JsonSerializedDeck deck1, JsonSerializedDeck deck2) {
            return getDeckSortingString(deck1).compareTo(getDeckSortingString(deck2));
        }

        private String getDeckSortingString(JsonSerializedDeck deck) {
            try {
                String formatName = deck.getFormat().get("formatName");
                String deckName = deck.getName();
                return formatName + "_" + deckName;
            } catch(HttpProcessingException exp) {
                return "?_" + deck.getName();
            }
        }
    }

}