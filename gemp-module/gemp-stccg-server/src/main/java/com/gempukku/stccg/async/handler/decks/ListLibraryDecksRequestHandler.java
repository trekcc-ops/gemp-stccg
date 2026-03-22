package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.database.DeckDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.service.AdminService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public class ListLibraryDecksRequestHandler extends DeckRequestHandler implements UriRequestHandler {

    private static final Logger LOGGER = LogManager.getLogger(ListLibraryDecksRequestHandler.class);

    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final FormatLibrary _formatLibrary;
    private final AdminService _adminService;
    private final DeckDAO _deckDAO;

    ListLibraryDecksRequestHandler(
            @JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
            @JacksonInject FormatLibrary formatLibrary,
            @JacksonInject AdminService adminService,
            @JacksonInject DeckDAO deckDAO) {
        _cardBlueprintLibrary = cardBlueprintLibrary;
        _formatLibrary = formatLibrary;
        _adminService = adminService;
        _deckDAO = deckDAO;
    }


    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {

        User librarian = Objects.requireNonNull(_adminService.getPlayer("Librarian"));

        List<CardDeck> librarianDecks = _deckDAO.getUserDecks(librarian);

        TreeSet<JsonSerializedDeck> jsonDecks = new TreeSet<>(new LibrarianJsonDeckSorter());
        for (CardDeck deck : librarianDecks) {
            JsonSerializedDeck jsonDeck = new JsonSerializedDeck(deck, _cardBlueprintLibrary, _formatLibrary);
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