package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.database.User;

@JsonIgnoreProperties("participantId")
public class DeleteDeckRequestHandler extends DeckRequestHandler implements UriRequestHandlerNew {

    private final String _deckName;

    DeleteDeckRequestHandler(
            @JsonProperty("deckName")
            String deckName
    ) {
        _deckName = deckName;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        serverObjects.getDeckDAO().deleteDeckForPlayer(resourceOwner, _deckName);
        responseWriter.writeXmlOkResponse();
    }

}