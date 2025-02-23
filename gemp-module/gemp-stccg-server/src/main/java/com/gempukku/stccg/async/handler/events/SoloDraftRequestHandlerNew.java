package com.gempukku.stccg.async.handler.events;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.draft.DraftChoice;
import com.gempukku.stccg.draft.DraftFormatLibrary;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueData;
import com.gempukku.stccg.league.LeagueNotFoundException;
import com.gempukku.stccg.league.SoloDraftLeagueData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.HttpURLConnection;

public abstract class SoloDraftRequestHandlerNew {

    SoloDraftLeagueData getLeagueData(String leagueType, ServerObjects serverObjects)
            throws LeagueNotFoundException, HttpProcessingException {

        League league = serverObjects.getLeagueService().getLeagueByType(leagueType);
        CardBlueprintLibrary cardLibrary = serverObjects.getCardBlueprintLibrary();
        FormatLibrary formatLibrary = serverObjects.getFormatLibrary();
        DraftFormatLibrary draftLibrary = serverObjects.getSoloDraftDefinitions();

        LeagueData leagueData = league.getLeagueData(cardLibrary, formatLibrary, draftLibrary);
        int leagueStart = leagueData.getSeries().getFirst().getStart();

        if (!leagueData.isSoloDraftLeague() || DateUtils.getCurrentDateAsInt() < leagueStart)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        return (SoloDraftLeagueData) leagueData;
    }

    protected void appendPicks(Document doc, Node rootElem,
                               Iterable<? extends DraftChoice> availablePics,
                               CardBlueprintLibrary library) {
        for (DraftChoice availableChoice : availablePics) {
            String choiceId = availableChoice.getChoiceId();
            String blueprintId = availableChoice.getBlueprintId();
            String choiceUrl = availableChoice.getChoiceUrl();
            Element availablePick = doc.createElement("availablePick");
            availablePick.setAttribute("id", choiceId);
            if (blueprintId != null) {
                availablePick.setAttribute("blueprintId", blueprintId);
                try {
                    availablePick.setAttribute("imageUrl", library.getCardBlueprint(blueprintId).getImageUrl());
                } catch (CardNotFoundException e) {
                    throw new RuntimeException("Blueprint " + blueprintId + " not found in library");
                }
            }
            if (choiceUrl != null)
                availablePick.setAttribute("url", choiceUrl);
            rootElem.appendChild(availablePick);
        }
    }
}