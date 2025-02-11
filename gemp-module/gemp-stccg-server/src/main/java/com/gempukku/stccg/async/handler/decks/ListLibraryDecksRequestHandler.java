package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.AbstractServer;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.database.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public class ListLibraryDecksRequestHandler extends DeckRequestHandler implements UriRequestHandlerNew {

    private static final Logger LOGGER = LogManager.getLogger(ListLibraryDecksRequestHandler.class);

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {

        User librarian = Objects.requireNonNull(serverObjects.getPlayerDAO().getPlayer("Librarian"));

        List<CardDeck> librarianDecks = serverObjects.getDeckDAO().getUserDecks(librarian);

        TreeSet<JsonSerializedDeck> jsonDecks = new TreeSet<>(new LibrarianJsonDeckSorter());
        for (CardDeck deck : librarianDecks) {
            JsonSerializedDeck jsonDeck = new JsonSerializedDeck(deck, serverObjects);
            try {
                _jsonMapper.writeValueAsString(jsonDeck);
                jsonDecks.add(jsonDeck);
            } catch(JsonProcessingException exp) {
                LOGGER.error("Unable to serialize deck '" + deck.getDeckName() + "'");
            }
        }
        String jsonString = _jsonMapper.writeValueAsString(jsonDecks);
        responseWriter.writeJsonResponse(jsonString);
    }

    private static class LibrarianJsonDeckSorter implements Comparator<JsonSerializedDeck> {

        public int compare(JsonSerializedDeck deck1, JsonSerializedDeck deck2) {
            return getDeckSortingString(deck1).compareTo(getDeckSortingString(deck2));
        }

        private String getDeckSortingString(JsonSerializedDeck deck) {
            String deckName = deck.getName();
            String prefix = (deckName.contains("starter")) ? "A" : "B";
            return prefix + "_" + deckName;
        }
    }

}