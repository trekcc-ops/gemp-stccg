package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ResponseWriter;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.db.vo.League;
import com.gempukku.stccg.draft.SoloDraft;
import com.gempukku.stccg.draft.SoloDraftDefinitions;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.league.LeagueData;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.league.SoloDraftLeagueData;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.HttpURLConnection;
import java.util.*;

public class SoloDraftRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final SoloDraftDefinitions _soloDraftDefinitions;
    private final FormatLibrary _formatLibrary;
    private final LeagueService _leagueService;

    public SoloDraftRequestHandler(ServerObjects objects) {
        super(objects);
        _leagueService = objects.getLeagueService();
        _formatLibrary = objects.getFormatLibrary();
        _soloDraftDefinitions = objects.getSoloDraftDefinitions();
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp)
            throws Exception {
        if (uri.isEmpty() || uri.charAt(0) != '/')
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        else if (request.method() == HttpMethod.POST) {
            makePick(request, uri.substring(1), responseWriter);
        } else if (request.method() == HttpMethod.GET) {
            getAvailablePicks(request, uri.substring(1), responseWriter);
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private void getAvailablePicks(HttpRequest request, String leagueType, ResponseWriter responseWriter)
            throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");

        League league = findLeagueByType(leagueType);

        if (league == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        LeagueData leagueData = league.getLeagueData(_cardBlueprintLibrary, _formatLibrary, _soloDraftDefinitions);
        int leagueStart = leagueData.getSeries().getFirst().getStart();

        if (!leagueData.isSoloDraftLeague() || DateUtils.getCurrentDateAsInt() < leagueStart)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        SoloDraftLeagueData soloDraftLeagueData = (SoloDraftLeagueData) leagueData;
        CollectionType collectionType = soloDraftLeagueData.getCollectionType();

        User resourceOwner = getResourceOwnerSafely(request, participantId);

        CardCollection collection = _collectionsManager.getPlayerCollection(resourceOwner, collectionType.getCode());

        Iterable<SoloDraft.DraftChoice> availableChoices;

        boolean finished = (Boolean) collection.getExtraInformation().get("finished");
        if (!finished) {
            int stage = ((Number) collection.getExtraInformation().get("stage")).intValue();
            long playerSeed = ((Number) collection.getExtraInformation().get("seed")).longValue();
            //noinspection unchecked
            List<String> draftPoolList = (List<String>) collection.getExtraInformation().get("draftPool");

            DefaultCardCollection draftPool = new DefaultCardCollection();
            if (draftPoolList != null)
                for (String card : draftPoolList)
                    draftPool.addItem(card, 1);

            SoloDraft soloDraft = soloDraftLeagueData.getSoloDraft();
            availableChoices = soloDraft.getAvailableChoices(playerSeed, stage, draftPool);
        } else {
            availableChoices = Collections.emptyList();
        }

        Document doc = createNewDoc();
        Element availablePicksElem = doc.createElement("availablePicks");
        doc.appendChild(availablePicksElem);

        appendAvailablePics(doc, availablePicksElem, availableChoices);

        responseWriter.writeXmlResponse(doc);
    }

    private League findLeagueByType(String leagueType) {
        for (League activeLeague : _leagueService.getActiveLeagues()) {
            if (activeLeague.getType().equals(leagueType))
                return activeLeague;
        }
        return null;
    }

    private void makePick(HttpRequest request, String leagueType, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");
        String selectedChoiceId = getFormParameterSafely(postDecoder, "choiceId");

        League league = findLeagueByType(leagueType);

        if (league == null)
            throw new HttpProcessingException(404);

        LeagueData leagueData = league.getLeagueData(_cardBlueprintLibrary, _formatLibrary, _soloDraftDefinitions);
        int leagueStart = leagueData.getSeries().getFirst().getStart();

        if (!leagueData.isSoloDraftLeague() || DateUtils.getCurrentDateAsInt() < leagueStart)
            throw new HttpProcessingException(404);

        SoloDraftLeagueData soloDraftLeagueData = (SoloDraftLeagueData) leagueData;
        CollectionType collectionType = soloDraftLeagueData.getCollectionType();

        User resourceOwner = getResourceOwnerSafely(request, participantId);

        CardCollection collection = _collectionsManager.getPlayerCollection(resourceOwner, collectionType.getCode());
        boolean finished = (Boolean) collection.getExtraInformation().get("finished");
        if (finished)
            throw new HttpProcessingException(404);

        int stage = ((Number) collection.getExtraInformation().get("stage")).intValue();
        long playerSeed = ((Number) collection.getExtraInformation().get("seed")).longValue();

        //noinspection unchecked
        List<String> draftPoolList = (List<String>) collection.getExtraInformation().get("draftPool");
        DefaultCardCollection draftPool = new DefaultCardCollection();
        
        if (draftPoolList != null)
            for (String card : draftPoolList)
                draftPool.addItem(card, 1);

        SoloDraft soloDraft = soloDraftLeagueData.getSoloDraft();
        Iterable<SoloDraft.DraftChoice> possibleChoices = soloDraft.getAvailableChoices(playerSeed, stage, draftPool);

        SoloDraft.DraftChoice draftChoice = getSelectedDraftChoice(selectedChoiceId, possibleChoices);
        if (draftChoice == null)
            throw new HttpProcessingException(400);

        CardCollection selectedCards = soloDraft.getCardsForChoiceId(selectedChoiceId, playerSeed, stage);
        Map<String, Object> extraInformationChanges = new HashMap<>();
        boolean hasNextStage = soloDraft.hasNextStage(stage);
        extraInformationChanges.put("stage", stage + 1);
        if (!hasNextStage)
            extraInformationChanges.put("finished", true);

        if (draftPoolList != null) {
            List<String> draftPoolListUpdate = new ArrayList<>();
            for (GenericCardItem item : draftPool.getAll()) {
                String blueprint = item.getBlueprintId();
                for (int i = 0; i < draftPool.getItemCount(blueprint); i++)
                    draftPoolListUpdate.add(blueprint);
            }

            if (draftPoolList != draftPoolListUpdate) 
                extraInformationChanges.put("draftPool",draftPoolListUpdate);
        }

        _collectionsManager.addItemsToPlayerCollection(false, "Draft pick", resourceOwner,
                collectionType, selectedCards.getAll(), extraInformationChanges);

        Document doc = createNewDoc();

        Element pickResultElem = doc.createElement("pickResult");
        doc.appendChild(pickResultElem);

        for (GenericCardItem item : selectedCards.getAll()) {
            Element pickedCard = doc.createElement("pickedCard");
            pickedCard.setAttribute("blueprintId", item.getBlueprintId());
            pickedCard.setAttribute("count", String.valueOf(item.getCount()));
            pickedCard.setAttribute("imageUrl", _cardBlueprintLibrary.getCardBlueprint(item.getBlueprintId()).getImageUrl());
            pickResultElem.appendChild(pickedCard);
        }

        if (hasNextStage) {
            appendAvailablePics(doc, pickResultElem,
                    soloDraft.getAvailableChoices(playerSeed, stage + 1, draftPool));
        }

        responseWriter.writeXmlResponse(doc);
        } finally {
            postDecoder.destroy();
        }
    }

    private void appendAvailablePics(Document doc, Element rootElem, Iterable<? extends SoloDraft.DraftChoice> availablePics) {
        for (SoloDraft.DraftChoice availableChoice : availablePics) {
            String choiceId = availableChoice.getChoiceId();
            String blueprintId = availableChoice.getBlueprintId();
            String choiceUrl = availableChoice.getChoiceUrl();
            Element availablePick = doc.createElement("availablePick");
            availablePick.setAttribute("id", choiceId);
            if (blueprintId != null) {
                availablePick.setAttribute("blueprintId", blueprintId);
                try {
                    availablePick.setAttribute("imageUrl", _cardBlueprintLibrary.getCardBlueprint(blueprintId).getImageUrl());
                } catch (CardNotFoundException e) {
                    throw new RuntimeException("Blueprint " + blueprintId + " not found in library");
                }
            }
            if (choiceUrl != null)
                availablePick.setAttribute("url", choiceUrl);
            rootElem.appendChild(availablePick);
        }
    }

    private SoloDraft.DraftChoice getSelectedDraftChoice(String choiceId, Iterable<? extends SoloDraft.DraftChoice> availableChoices) {
        for (SoloDraft.DraftChoice availableChoice : availableChoices) {
            if (availableChoice.getChoiceId().equals(choiceId))
                return availableChoice;
        }
        return null;
    }
}