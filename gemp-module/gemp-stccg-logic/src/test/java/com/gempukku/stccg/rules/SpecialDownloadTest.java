package com.gempukku.stccg.rules;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.playcard.DownloadAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpecialDownloadTest extends AbstractAtTest {

    private MissionCard _mission;
    private PersonnelCard betor;
    private PhysicalCard lursa;
    private FacilityCard outpost;
    private ShipCard ship;

    private void initializeGame()
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_193", "Test Mission", P1);
        outpost = builder.addOutpost(Affiliation.KLINGON, P1);
        ship = builder.addShipInSpace("116_105", "I.K.C. Lukara", P1, _mission);
        lursa = builder.addDrawDeckCard("101_280", "Lursa", P1);
        betor = builder.addCardAboardShipOrFacility("163_036", "B'Etor", P1, outpost, PersonnelCard.class);
        builder.addCardAboardShipOrFacility("101_276", "Kromm", P1, ship, PersonnelCard.class);
        builder.addCardAboardShipOrFacility("155_079", "Captain Worf", P1, ship, PersonnelCard.class);
        builder.addCardAboardShipOrFacility("155_080", "Commander K'Ehleyr", P1, ship, PersonnelCard.class);
        builder.addCardAboardShipOrFacility("155_093", "Koral", P1, ship, PersonnelCard.class);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void downloadAtStartOfAttemptTest() throws DecisionResultInvalidException, CardNotFoundException,
            InvalidGameOperationException {
        initializeGame();

        // Crew has initial INTEGRITY of 27, so can't pass mission
        Action attemptAction = attemptMission(P1, _mission);
        skipAction(P1, DownloadAction.class);
        assertTrue(attemptAction.wasFailed());

        // Download Lursa during the second attempt; she joins the crew, and it now has enough INTEGRITY to solve
        Action attemptAction2 = attemptMission(P1, _mission);
        downloadCard(P1, lursa);
        selectCard(P1, ship); // select ship for destination of download action
        assertTrue(attemptAction2.wasSuccessful());
    }


}