package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.database.User;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public class ListLibraryDecksRequestHandler extends DeckRequestHandlerNew implements UriRequestHandlerNew {

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {

        User librarian = Objects.requireNonNull(serverObjects.getPlayerDAO().getPlayer("Librarian"));

        List<CardDeck> librarianDecks = serverObjects.getDeckDAO().getUserDecks(librarian);

        TreeSet<JsonSerializedDeck> jsonDecks = new TreeSet<>(new LibrarianJsonDeckSorter());
        librarianDecks.forEach(deck -> jsonDecks.add(new JsonSerializedDeck(deck, serverObjects)));

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