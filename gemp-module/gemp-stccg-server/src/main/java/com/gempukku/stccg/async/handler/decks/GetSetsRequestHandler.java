package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.common.JSONData;
import com.gempukku.stccg.formats.DefaultGameFormat;

import java.util.Map;

public class GetSetsRequestHandler extends DeckRequestHandler implements UriRequestHandlerNew {

    private final String _format;

    GetSetsRequestHandler(
            @JsonProperty("format")
            String format
    ) {
        _format = format;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        DefaultGameFormat currentFormat = serverObjects.getFormatLibrary().get(_format);

        Map<String, String> sets = currentFormat.getValidSetsAndTheirCards(serverObjects.getCardBlueprintLibrary());
        Object[] output = sets.entrySet().stream()
                .map(x -> new JSONData.ItemStub(x.getKey(), x.getValue()))
                .toArray();

        responseWriter.writeJsonResponse(_jsonMapper.writeValueAsString(output));
    }

}