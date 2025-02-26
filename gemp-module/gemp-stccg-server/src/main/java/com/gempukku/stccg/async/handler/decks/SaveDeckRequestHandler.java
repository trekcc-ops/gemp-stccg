package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.GameFormat;

@JsonIgnoreProperties("participantId")
public class SaveDeckRequestHandler extends DeckRequestHandler implements UriRequestHandler {

    private final String _deckName;
    private final String _targetFormat;
    private final String _notes;
    private final String _deckContents;

    SaveDeckRequestHandler(
            @JsonProperty("deckName")
            String deckName,
            @JsonProperty("targetFormat")
            String targetFormat,
            @JsonProperty("notes")
            String notes,
            @JsonProperty("deckContents")
            String deckContents
    ) {
        _deckName = deckName;
        _targetFormat = targetFormat;
        _notes = notes;
        _deckContents = deckContents;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        GameFormat validatedFormat = validateFormat(_targetFormat, serverObjects.getFormatLibrary());
        CardDeck deck = new CardDeck(_deckName, _deckContents, validatedFormat, _notes);
        serverObjects.getDeckDAO().saveDeckForPlayer(deck, resourceOwner);
        responseWriter.writeXmlOkResponse();
    }

}