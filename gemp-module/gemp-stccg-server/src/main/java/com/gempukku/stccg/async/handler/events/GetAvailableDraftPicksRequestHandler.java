package com.gempukku.stccg.async.handler.events;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.draft.DraftChoice;
import com.gempukku.stccg.draft.SoloDraft;
import com.gempukku.stccg.league.LeagueNotFoundException;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.league.SoloDraftLeague;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.Map;

public class GetAvailableDraftPicksRequestHandler implements SoloDraftRequestHandler, UriRequestHandler {

    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final CollectionsManager _collectionsManager;
    private final CollectionType _collectionType;
    private final SoloDraftLeague _league;
    GetAvailableDraftPicksRequestHandler(
            @JsonProperty("leagueType")
            String leagueType,
            @JacksonInject CollectionsManager collectionsManager,
            @JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
            @JacksonInject LeagueService leagueService) throws LeagueNotFoundException {
        _collectionsManager = collectionsManager;
        _cardBlueprintLibrary = cardBlueprintLibrary;
        if (leagueService.getLeagueById(leagueType) instanceof SoloDraftLeague draftLeague) {
            _league = draftLeague;
        } else {
            throw new LeagueNotFoundException();
        }
        _collectionType = _league.getCollectionType();
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        User resourceOwner = request.user();

        CardCollection collection = _collectionsManager.getPlayerCollectionWithLibrary(
                resourceOwner, _collectionType.getCode(), _cardBlueprintLibrary);

        Iterable<DraftChoice> availableChoices;

        Map<String, Object> extraInformation = collection.getExtraInformation();
        boolean finished = (Boolean) extraInformation.get("finished");
        if (finished) {
            availableChoices = Collections.emptyList();
        } else {
            int stage = ((Number) extraInformation.get("stage")).intValue();
            long playerSeed = ((Number) extraInformation.get("seed")).longValue();
            Iterable<String> draftPoolList = (Iterable<String>) extraInformation.get("draftPool");

            DefaultCardCollection draftPool = new DefaultCardCollection();
            if (draftPoolList != null)
                for (String card : draftPoolList)
                    draftPool.addItem(card, 1);

            SoloDraft soloDraft = _league.getSoloDraft();
            availableChoices = soloDraft.getAvailableChoices(playerSeed, stage, draftPool);
        }

        Document doc = createNewDoc();
        Element availablePicksElem = doc.createElement("availablePicks");
        doc.appendChild(availablePicksElem);

        appendPicks(doc, availablePicksElem, availableChoices, _cardBlueprintLibrary);

        responseWriter.writeXmlResponseWithNoHeaders(doc);

    }


}