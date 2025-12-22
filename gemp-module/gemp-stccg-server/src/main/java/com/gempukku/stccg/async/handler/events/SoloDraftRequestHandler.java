package com.gempukku.stccg.async.handler.events;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.draft.DraftChoice;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueNotFoundException;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.league.SoloDraftLeague;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.HttpURLConnection;
import java.time.ZonedDateTime;

public interface SoloDraftRequestHandler {

    default SoloDraftLeague getLeague(String leagueType, LeagueService leagueService)
            throws HttpProcessingException {
        try {
            League league = leagueService.getLeagueById(leagueType);
            ZonedDateTime leagueStart = league.getStart();
            if (league instanceof SoloDraftLeague soloDraftData && leagueStart.isBefore(ZonedDateTime.now())) {
                return soloDraftData;
            } else {
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
            }
        } catch(LeagueNotFoundException exp) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND, exp.getMessage());
        }
    }


    default void appendPicks(Document doc, Node rootElem,
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