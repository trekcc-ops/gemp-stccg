package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_152_018_SpaceBoomer_Test extends AbstractAtTest {

    private PhysicalCard boomer;
    private PersonnelCard selveth;
    private ShipCard scienceVessel;
    private MissionCard mission;
    private PhysicalCard boomer2;

    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        mission = builder.addMission("155_041", "Investigate Destruction", P1); // SC+Dip+OFF OR Nav+SEC+Treach
        boomer = builder.addCardInHand("152_018", "Space Boomer", P1);
        boomer2 = builder.addCardInHand("152_018", "Space Boomer", P1);

        scienceVessel = builder.addShipInSpace("101_362", "Science Vessel", P1);

        // Space Boomer can be played on Selveth, but not Tarus or Parem
        // Mission can be solved with Selveth + Parem
        selveth = builder.addCardAboardShipOrFacility("112_240", "Selveth", P1, scienceVessel, PersonnelCard.class);
        builder.addCardAboardShipOrFacility("101_322", "Tarus", P1, scienceVessel, PersonnelCard.class);
        builder.addCardAboardShipOrFacility("161_046", "Parem", P1, scienceVessel, PersonnelCard.class);

        builder.setPhase(Phase.CARD_PLAY);
        _game = builder.startGame();
    }

    @Test
    public void spaceBoomerTest() throws Exception {
        initializeGame();
        playCard(P1, boomer);

        // Verify that Space Boomer is automatically played on Selveth as the only valid target
        assertTrue(boomer.isInPlay());
        assertTrue(boomer.isAtop(selveth));

        // Verify RANGE of Science Vessel isn't modified by Space Boomer since no missions are solved
        assertEquals(8, scienceVessel.getRange(_game));

        attemptMission(P1, mission);
        assertTrue(mission.isCompleted(_game));

        assertEquals(11, scienceVessel.getRange(_game));

        // Verify that it is not cumulative. You can play it twice, but still just RANGE +3.
        skipToNextTurnAndPhase(P1, Phase.CARD_PLAY);
        playCard(P1, boomer2);
        assertTrue(boomer2.isInPlay());
        assertTrue(boomer2.isAtop(selveth));
        assertEquals(11, scienceVessel.getRange(_game));
    }

}