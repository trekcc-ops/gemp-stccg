package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;


public class GetErrataRequestHandler implements UriRequestHandler {
    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        CardBlueprintLibrary library = serverObjects.getCardBlueprintLibrary();
        String jsonString = new ObjectMapper().writeValueAsString(library.getErrata());
        responseWriter.writeJsonResponse(jsonString);
    }

}