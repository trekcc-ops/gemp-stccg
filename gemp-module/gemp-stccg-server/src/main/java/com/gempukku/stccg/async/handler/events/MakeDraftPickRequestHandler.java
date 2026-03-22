package com.gempukku.stccg.async.handler.events;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.draft.DraftChoice;
import com.gempukku.stccg.draft.SoloDraft;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.league.SoloDraftLeague;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MakeDraftPickRequestHandler implements SoloDraftRequestHandler, UriRequestHandler {

    private final String _choiceId;
    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final CollectionsManager _collectionsManager;
    private final CollectionType collectionType;
    private final SoloDraftLeague _league;

    MakeDraftPickRequestHandler(
            @JsonProperty("leagueType")
            String leagueType,
            @JsonProperty("choiceId")
            String choiceId,
            @JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
            @JacksonInject CollectionsManager collectionsManager,
            @JacksonInject LeagueService leagueService
            ) throws HttpProcessingException {
        _choiceId = choiceId;
        _cardBlueprintLibrary = cardBlueprintLibrary;
        _collectionsManager = collectionsManager;
        _league = getLeague(leagueType, leagueService);
        collectionType = _league.getCollectionType();
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {

        User resourceOwner = request.user();

        CardCollection collection = _collectionsManager.getPlayerCollectionWithLibrary(
                resourceOwner, collectionType.getCode(), _cardBlueprintLibrary);

        boolean finished = (Boolean) collection.getExtraInformation().get("finished");
        if (finished)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        int stage = ((Number) collection.getExtraInformation().get("stage")).intValue();
        long playerSeed = ((Number) collection.getExtraInformation().get("seed")).longValue();

        //noinspection unchecked
        Iterable<String> draftPoolList = (Iterable<String>) collection.getExtraInformation().get("draftPool");
        DefaultCardCollection draftPool = new DefaultCardCollection();

        if (draftPoolList != null)
            for (String card : draftPoolList)
                draftPool.addItem(card, 1);

        SoloDraft soloDraft = _league.getSoloDraft();
        Iterable<DraftChoice> possibleChoices = soloDraft.getAvailableChoices(playerSeed, stage, draftPool);

        DraftChoice draftChoice = getSelectedDraftChoice(_choiceId, possibleChoices);
        if (draftChoice == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400

        // may throw an InvalidDraftResultException
        CardCollection selectedCards = soloDraft.getCardsForChoiceId(_choiceId, playerSeed, stage);


        Map<String, Object> extraInformationChanges = new HashMap<>();
        boolean hasNextStage = soloDraft.hasNextStage(stage);
        extraInformationChanges.put("stage", stage + 1);
        if (!hasNextStage)
            extraInformationChanges.put("finished", true);

        if (draftPoolList != null) {
            Collection<String> draftPoolListUpdate = new ArrayList<>();
            for (GenericCardItem item : draftPool.getAll()) {
                String blueprint = item.getBlueprintId();
                for (int i = 0; i < draftPool.getItemCount(blueprint); i++)
                    draftPoolListUpdate.add(blueprint);
            }

            if (draftPoolList != draftPoolListUpdate)
                extraInformationChanges.put("draftPool",draftPoolListUpdate);
        }

        CardCollection playerCollection = _collectionsManager.getPlayerCollectionWithLibrary(resourceOwner,
            collectionType.getCode(), _cardBlueprintLibrary);
        _collectionsManager.addItemsToUserCollection(false, "Draft pick", resourceOwner,
                collectionType, selectedCards.getAll(), extraInformationChanges, playerCollection);

        Document doc = createNewDoc();

        Element pickResultElem = doc.createElement("pickResult");
        doc.appendChild(pickResultElem);

        for (GenericCardItem item : selectedCards.getAll()) {
            Element pickedCard = doc.createElement("pickedCard");
            pickedCard.setAttribute("blueprintId", item.getBlueprintId());
            pickedCard.setAttribute("count", String.valueOf(item.getCount()));
            pickedCard.setAttribute(
                    "imageUrl", _cardBlueprintLibrary.getCardBlueprint(item.getBlueprintId()).getImageUrl());
            pickResultElem.appendChild(pickedCard);
        }

        if (hasNextStage) {
            appendPicks(doc, pickResultElem,
                    soloDraft.getAvailableChoices(playerSeed, stage + 1, draftPool), _cardBlueprintLibrary);
        }

        responseWriter.writeXmlResponseWithNoHeaders(doc);
    }

    private static DraftChoice getSelectedDraftChoice(String choiceId,
                                                      Iterable<? extends DraftChoice> choices) {
        for (DraftChoice availableChoice : choices) {
            if (availableChoice.getChoiceId().equals(choiceId))
                return availableChoice;
        }
        return null;
    }



}