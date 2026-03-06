package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_161_038_Agyer_Test extends AbstractAtTest {

    private PhysicalCard boomerInDeck;
    private PhysicalCard boomerInHand;
    private PhysicalCard selveth;
    private PhysicalCard agyer;

    private void initializeGame()
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        MissionCard mission = builder.addMission("101_154", "Excavation", P1);
        MissionCard mission2 = builder.addMission(MissionType.PLANET, Affiliation.KLINGON, P2);
        FacilityCard outpost = builder.addOutpost(Affiliation.ROMULAN, P1, mission);

        // downloadable cards
        boomerInDeck = builder.addDrawDeckCard("152_018", "Space Boomer", P1);
        boomerInHand = builder.addCardInHand("152_018", "Space Boomer", P1);

        // targets card can be played on - your personnel with Stellar Cartography x2 or Navigation x2 at Agyer's location
        selveth = builder.addCardAboardShipOrFacility("112_240", "Selveth", P1, outpost, PersonnelCard.class);
        agyer = builder.addCardOnPlanetSurface("161_038", "Agyer", P1, mission);

        // Can't DL Space Boomer to these cards:
            // Tarus only has 1 Stellar Cartography
        builder.addCardAboardShipOrFacility("101_322", "Tarus", P1, outpost, PersonnelCard.class);
            // Can't play Space Boomer on opponent's personnel
        builder.addCardOnPlanetSurface("112_240", "Selveth", P2, mission);
            // Travis isn't at Agyer's location
        builder.addCardOnPlanetSurface("152_054", "Travis Mayweather", P1, mission2);

        builder.setPhase(Phase.EXECUTE_ORDERS);
        _game = builder.startGame();
    }

    @Test
    public void downloadTest() throws Exception {
        initializeGame();

        // Verify cards you can download
        initiateDownloadAction(P1, agyer);
        assertTrue(selectableCardsAre(P1, List.of(boomerInHand, boomerInDeck)));
        selectCard(P1, boomerInDeck);

        // Verify that it can only be played to Selveth and Agyer
        assertTrue(selectableCardsAre(P1, List.of(selveth, agyer)));
        selectCard(P1, selveth);

        // Verify that Space Boomer was played on Selveth
        assertTrue(boomerInDeck.isInPlay());
        assertTrue(boomerInDeck.isAtop(selveth));
    }

}