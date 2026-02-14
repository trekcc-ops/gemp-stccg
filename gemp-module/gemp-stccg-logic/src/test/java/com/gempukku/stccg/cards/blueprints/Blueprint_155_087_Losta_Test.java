package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_155_087_Losta_Test extends AbstractAtTest {

    @Test
    public void downloadOnBorethTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        MissionCard mission = builder.addMission("101_168", "Investigate Disturbance", P1);
        PhysicalCard lowerDecks = builder.addDrawDeckCard("103_042", "Lower Decks", P1);
        builder.addCardOnPlanetSurface("155_087", "Losta", P1, mission, PersonnelCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
        downloadCard(P1, lowerDecks);
        assertTrue(lowerDecks.isInPlay());
        assertEquals(Zone.CORE, lowerDecks.getZone());
    }

    @Test
    public void downloadWithKahlessTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        MissionCard mission = builder.addMission("101_154", "Excavation", P1);
        PhysicalCard lowerDecks = builder.addDrawDeckCard("103_042", "Lower Decks", P1);
        builder.addCardOnPlanetSurface("155_087", "Losta", P1, mission, PersonnelCard.class);
        builder.addCardOnPlanetSurface("155_082", "Kahless", P1, mission, PersonnelCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
        downloadCard(P1, lowerDecks);
        assertTrue(lowerDecks.isInPlay());
        assertEquals(Zone.CORE, lowerDecks.getZone());
    }

    @Test
    public void downloadFailTest() throws CardNotFoundException, InvalidGameOperationException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        MissionCard mission = builder.addMission("101_154", "Excavation", P1);
        PhysicalCard lowerDecks = builder.addDrawDeckCard("103_042", "Lower Decks", P1);
        builder.addCardOnPlanetSurface("155_087", "Losta", P1, mission, PersonnelCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
        assertThrows(DecisionResultInvalidException.class, () -> downloadCard(P1, lowerDecks));
    }

}