package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.DeckDAO;
import com.gempukku.stccg.database.User;

@JsonIgnoreProperties("participantId")
public class DeleteDeckRequestHandler extends DeckRequestHandler implements UriRequestHandler {

    private final String _deckName;
    private final DeckDAO _deckDAO;

    DeleteDeckRequestHandler(
            @JsonProperty("deckName")
            String deckName,
            @JacksonInject DeckDAO deckDAO) {
        _deckName = deckName;
        _deckDAO = deckDAO;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        User resourceOwner = request.user();
        _deckDAO.deleteDeckForPlayer(resourceOwner, _deckName);
        responseWriter.writeXmlOkResponse();
    }

}