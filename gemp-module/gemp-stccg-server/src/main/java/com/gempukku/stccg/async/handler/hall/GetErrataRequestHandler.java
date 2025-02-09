package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.hall.HallServer;
import io.netty.handler.codec.http.HttpRequest;


public class GetErrataRequestHandler implements UriRequestHandlerNew {
    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp,
                                    ServerObjects serverObjects)
            throws Exception {
        CardBlueprintLibrary library = serverObjects.getCardBlueprintLibrary();
        String jsonString = new ObjectMapper().writeValueAsString(library.getErrata());
        responseWriter.writeJsonResponse(jsonString);
    }

}