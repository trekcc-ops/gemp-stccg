package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.DeckValidation;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.formats.DefaultGameFormat;
import com.gempukku.stccg.formats.FormatLibrary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeckStatsRequestHandler extends DeckRequestHandler implements UriRequestHandler {

    private final String _targetFormat;
    private final String _deckContents;
    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final FormatLibrary _formatLibrary;

    DeckStatsRequestHandler(
            @JsonProperty("targetFormat")
            String targetFormat,
            @JsonProperty("deckContents")
            String deckContents,
            @JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
            @JacksonInject FormatLibrary formatLibrary) {
        _targetFormat = targetFormat;
        _deckContents = deckContents;
        _cardBlueprintLibrary = cardBlueprintLibrary;
        _formatLibrary = formatLibrary;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        DefaultGameFormat format = validateFormat(_targetFormat, _formatLibrary);
        CardDeck deck = new CardDeck("tempDeck", _deckContents, format);
        if(format == null || _targetFormat == null) {
            Map<String, List<String>> response = new HashMap<>();
            response.put("errors", List.of("Invalid format: " + _targetFormat));
            responseWriter.writeJsonResponse(mapper.writeValueAsString(response));
        } else {
            DeckValidation validation = new DeckValidation(deck, _cardBlueprintLibrary, format);
            responseWriter.writeJsonResponse(mapper.writeValueAsString(validation));
        }
    }



}