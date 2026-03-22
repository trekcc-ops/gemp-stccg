package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;


public class GetErrataRequestHandler implements UriRequestHandler {

    private final CardBlueprintLibrary _cardBlueprintLibrary;

    GetErrataRequestHandler(@JacksonInject CardBlueprintLibrary cardBlueprintLibrary) {
        _cardBlueprintLibrary = cardBlueprintLibrary;
    }
    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        String jsonString = new ObjectMapper().writeValueAsString(_cardBlueprintLibrary.getErrata());
        responseWriter.writeJsonResponse(jsonString);
    }

}