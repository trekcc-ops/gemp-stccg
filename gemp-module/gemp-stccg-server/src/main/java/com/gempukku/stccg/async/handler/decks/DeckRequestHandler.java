package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.formats.DefaultGameFormat;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DeckRequestHandler {

    protected ObjectMapper _jsonMapper = new ObjectMapper();


    protected class JsonSerializedDeck {

        private final CardDeck _cardDeck;
        private final CardBlueprintLibrary _library;
        private final FormatLibrary _formatLibrary;

        JsonSerializedDeck(CardDeck cardDeck, CardBlueprintLibrary cardBlueprintLibrary, FormatLibrary formatLibrary) {
            _cardDeck = cardDeck;
            _library = cardBlueprintLibrary;
            _formatLibrary = formatLibrary;
        }

        @JsonProperty("deckName")
        String getName() {
            return _cardDeck.getDeckName();
        }

        @JsonProperty("notes")
        String getNotes() {
            return _cardDeck.getNotes();
        }

        @JsonProperty("targetFormat")
        Map<String, String> getFormat() throws HttpProcessingException {
            if (_cardDeck.getTargetFormat().isEmpty()) {
                return new HashMap<>();
            } else {
                GameFormat format = validateFormat(_cardDeck.getTargetFormat(), _formatLibrary);
                Map<String, String> result = new HashMap<>();
                result.put("formatName", format.getName());
                result.put("formatCode", format.getCode());
                return result;
            }
        }

        @JsonProperty("individualCards")
        List<Map<String, String>> getCards() throws CardNotFoundException {
            List<Map<String, String>> result = new ArrayList<>();
            for (SubDeck subDeck : _cardDeck.getSubDecks().keySet()) {
                for (String card : _cardDeck.getSubDecks().get(subDeck)) {
                    Map<String, String> cardInfo = new HashMap<>();
                    cardInfo.put("blueprintId", card);
                    cardInfo.put("subDeck", subDeck.name());
                    cardInfo.put("imageUrl", _library.getCardBlueprint(card).getImageUrl());
                    result.add(cardInfo);
                }
            }
            return result;
        }

        @JsonProperty("cards")
        Map<SubDeck, List<Map<String, Object>>> getCardsBySubDeck() throws CardNotFoundException {
            Map<SubDeck, List<Map<String, Object>>> result = new HashMap<>();
            for (SubDeck subDeck : _cardDeck.getSubDecks().keySet()) {
                Map<String, Integer> cardCounts = new HashMap<>();
                for (String card : _cardDeck.getSubDecks().get(subDeck)) {
                    cardCounts.putIfAbsent(card, 0);
                    cardCounts.put(card, cardCounts.get(card) + 1);
                }
                List<Map<String, Object>> cardsInSubDeck = new ArrayList<>();
                for (String blueprintId : cardCounts.keySet()) {
                    CardBlueprint blueprint = _library.getCardBlueprint(blueprintId);
                    Map<String, Object> cardMap = new HashMap<>();
                    cardMap.put("blueprintId", blueprintId);
                    cardMap.put("count", cardCounts.get(blueprintId));
                    cardMap.put("cardTitle", blueprint.getTitle());
                    cardMap.put("imageUrl", blueprint.getImageUrl());
                    cardsInSubDeck.add(cardMap);
                }
                result.put(subDeck, cardsInSubDeck);
            }
            return result;
        }

    }

    DefaultGameFormat validateFormat(String name, FormatLibrary formatLibrary) throws HttpProcessingException {
        DefaultGameFormat validatedFormat = formatLibrary.get(name);
        if(validatedFormat == null)
            validatedFormat = formatLibrary.getFormatByName(name);
        if (validatedFormat == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND);
        return validatedFormat;
    }

}