package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_161_046_Parem_Test extends AbstractAtTest {

    private PersonnelCard parem;
    private MissionCard mission;
    private PersonnelCard karina;
    private PhysicalCard oldParem;
    private PhysicalCard takket;

    private void initializeGame(Phase startingPhase) throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission("101_154", "Excavation", P1);
        builder.addSeedCardUnderMission("101_014", "Archer", P2, mission);
        builder.addOutpost(Affiliation.ROMULAN, P1);
        parem = builder.addCardOnPlanetSurface("161_046", "Parem", P1, mission, PersonnelCard.class);
        oldParem = builder.addCardInHand("101_316", "Parem", P1);
        takket = builder.addCardInHand("101_320", "Takket", P1);
        karina = builder.addCardOnPlanetSurface("112_238", "Karina", P1, mission, PersonnelCard.class);
        builder.setPhase(startingPhase);
        builder.startGame();
    }

    @Test
    public void attributeTest() throws DecisionResultInvalidException, CardNotFoundException,
            InvalidGameOperationException {
        // Verify that even though Parem has lower attributes, he's killed by Archer because he gets boosted up to 21.
        initializeGame(Phase.EXECUTE_ORDERS);
        assertEquals(18, parem.getIntegrity(_game) + parem.getCunning(_game) + parem.getStrength(_game));
        assertEquals(20, karina.getIntegrity(_game) + karina.getCunning(_game) + karina.getStrength(_game));
        attemptMission(P1, mission);
        assertTrue(personnelWasKilled(parem));
    }

    @Test
    public void personaTest() throws DecisionResultInvalidException, CardNotFoundException,
            InvalidGameOperationException {
        initializeGame(Phase.CARD_PLAY);
        // Verify that you can't report Premiere Parem for duty if TNG Parem is already in play.
        assertThrows(DecisionResultInvalidException.class, () -> playCard(P1, oldParem));
        assertDoesNotThrow(() -> playCard(P1, takket));
    }


}