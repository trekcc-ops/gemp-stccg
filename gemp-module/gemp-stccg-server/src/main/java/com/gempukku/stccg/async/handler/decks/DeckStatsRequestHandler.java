package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.HTMLUtils;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.formats.DefaultGameFormat;

public class DeckStatsRequestHandler extends DeckRequestHandler implements UriRequestHandlerNew {

    private final String _targetFormat;
    private final String _deckContents;

    DeckStatsRequestHandler(
            @JsonProperty("targetFormat")
            String targetFormat,
            @JsonProperty("deckContents")
            String deckContents
    ) {
        _targetFormat = targetFormat;
        _deckContents = deckContents;

    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {

        DefaultGameFormat format = validateFormat(_targetFormat, serverObjects.getFormatLibrary());
        CardDeck deck = new CardDeck("tempDeck", _deckContents, format);
        if(format == null || _targetFormat == null)
        {
            responseWriter.writeHtmlResponse("Invalid format: " + _targetFormat);
        }

        assert format != null;
        String response = HTMLUtils.getDeckValidation(serverObjects.getCardBlueprintLibrary(), deck, format);
        responseWriter.writeHtmlResponse(response);
    }



}