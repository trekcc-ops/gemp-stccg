package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.common.JSONData;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;

import java.util.Map;

public class DeckFormatsRequestHandler extends DeckRequestHandlerNew implements UriRequestHandlerNew {

    private final boolean _includeEvents;

    DeckFormatsRequestHandler(
            @JsonProperty("includeEvents")
            boolean includeEvents
    ) {
        _includeEvents = includeEvents;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {

        String json;
        FormatLibrary formatLibrary = serverObjects.getFormatLibrary();

        if(_includeEvents) {
            json = _jsonMapper.writeValueAsString(formatLibrary);
        } else {
            Map<String, GameFormat> formats = formatLibrary.getHallFormats();
            Object[] output = formats.entrySet().stream()
                    .map(x -> new JSONData.ItemStub(x.getKey(), x.getValue().getName()))
                    .toArray();
            json = _jsonMapper.writeValueAsString(output);
        }
        responseWriter.writeJsonResponse(json);
    }

}