package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.common.JSONData;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;

import java.util.Map;

public class DeckFormatsRequestHandler extends DeckRequestHandler implements UriRequestHandler {

    private final boolean _includeEvents;
    private final FormatLibrary _formatLibrary;

    DeckFormatsRequestHandler(
            @JsonProperty("includeEvents")
            boolean includeEvents,
            @JacksonInject FormatLibrary formatLibrary
    ) {
        _includeEvents = includeEvents;
        _formatLibrary = formatLibrary;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {

        String json;

        if(_includeEvents) {
            json = _jsonMapper.writeValueAsString(_formatLibrary);
        } else {
            Map<String, GameFormat> formats = _formatLibrary.getHallFormats();
            Object[] output = formats.entrySet().stream()
                    .map(x -> new JSONData.ItemStub(x.getKey(), x.getValue().getName()))
                    .toArray();
            json = _jsonMapper.writeValueAsString(output);
        }
        responseWriter.writeJsonResponse(json);
    }

}