package com.gempukku.stccg.async.handler.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.draft.DraftChoice;
import com.gempukku.stccg.draft.SoloDraft;
import com.gempukku.stccg.league.LeagueNotFoundException;
import com.gempukku.stccg.league.SoloDraftLeagueData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Map;

public class GetAvailableDraftPicksRequestHandler extends SoloDraftRequestHandlerNew implements UriRequestHandlerNew {

    private final String _leagueType;
    GetAvailableDraftPicksRequestHandler(
            @JsonProperty("leagueType")
            String leagueType
    ) {
        _leagueType = leagueType;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        CardBlueprintLibrary cardLibrary = serverObjects.getCardBlueprintLibrary();
        CollectionsManager collectionsManager = serverObjects.getCollectionsManager();

        try {
            SoloDraftLeagueData soloDraftLeagueData = getLeagueData(_leagueType, serverObjects);
            CollectionType collectionType = soloDraftLeagueData.getCollectionType();

            CardCollection collection = collectionsManager.getPlayerCollection(resourceOwner, collectionType.getCode());
            Iterable<DraftChoice> availableChoices;

            Map<String, Object> extraInformation = collection.getExtraInformation();
            boolean finished = (Boolean) extraInformation.get("finished");
            if (finished) {
                availableChoices = Collections.emptyList();
            } else {
                int stage = ((Number) extraInformation.get("stage")).intValue();
                long playerSeed = ((Number) extraInformation.get("seed")).longValue();
                //noinspection unchecked
                Iterable<String> draftPoolList = (Iterable<String>) extraInformation.get("draftPool");

                DefaultCardCollection draftPool = new DefaultCardCollection();
                if (draftPoolList != null)
                    for (String card : draftPoolList)
                        draftPool.addItem(card, 1);

                SoloDraft soloDraft = soloDraftLeagueData.getSoloDraft();
                availableChoices = soloDraft.getAvailableChoices(playerSeed, stage, draftPool);
            }

            Document doc = createNewDoc();
            Element availablePicksElem = doc.createElement("availablePicks");
            doc.appendChild(availablePicksElem);

            appendPicks(doc, availablePicksElem, availableChoices, cardLibrary);

            responseWriter.writeXmlResponseWithNoHeaders(doc);

        } catch(LeagueNotFoundException exp) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }


}