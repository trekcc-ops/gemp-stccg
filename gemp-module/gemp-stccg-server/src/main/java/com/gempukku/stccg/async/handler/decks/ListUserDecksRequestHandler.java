package com.gempukku.stccg.async.handler.decks;

import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.database.User;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class ListUserDecksRequestHandler extends DeckRequestHandler implements UriRequestHandlerNew {

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        User user = request.user();
        List<CardDeck> userDecks = serverObjects.getDeckDAO().getUserDecks(user);

        TreeSet<JsonSerializedDeck> jsonDecks = new TreeSet<>(new UserJsonDeckSorter());
        userDecks.forEach(deck -> jsonDecks.add(new JsonSerializedDeck(deck, serverObjects)));

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