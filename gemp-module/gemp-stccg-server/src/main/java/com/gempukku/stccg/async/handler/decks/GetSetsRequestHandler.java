package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.JSONData;
import com.gempukku.stccg.formats.DefaultGameFormat;
import com.gempukku.stccg.formats.FormatLibrary;

import java.net.HttpURLConnection;
import java.util.Map;

public class GetSetsRequestHandler extends DeckRequestHandler implements UriRequestHandler {

    private final Map<String, String> _sets;

    GetSetsRequestHandler(
            @JsonProperty("format")
            String format,
            @JacksonInject FormatLibrary formatLibrary,
            @JacksonInject CardBlueprintLibrary cardBlueprintLibrary) throws HttpProcessingException {
        DefaultGameFormat currentFormat = formatLibrary.get(format);
        if (currentFormat == null) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND);
        } else {
            _sets = currentFormat.getValidSetsAndTheirCards(cardBlueprintLibrary);
        }
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        Object[] output = _sets.entrySet().stream()
                .map(x -> new JSONData.ItemStub(x.getKey(), x.getValue()))
                .toArray();
        responseWriter.writeJsonResponse(_jsonMapper.writeValueAsString(output));
    }

}