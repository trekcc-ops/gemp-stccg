package com.gempukku.stccg.rules;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PresentTest extends AbstractAtTest {

    @Test
    public void opponentPresentDuringEncounterTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        MissionCard mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        PhysicalCard anaphasic = builder.addSeedCardUnderMission("101_012", "Anaphasic Organism", P2, mission);
        PhysicalCard yourTaitt1 = builder.addCardOnPlanetSurface("101_242", "Taitt", P1, mission);
        PhysicalCard yourTaitt2 = builder.addCardOnPlanetSurface("101_242", "Taitt", P1, mission);
        PhysicalCard opponentsTaitt = builder.addCardOnPlanetSurface("101_242", "Taitt", P2, mission);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();

        attemptMission(P1, mission);
        assertTrue(yourTaitt1.isPresentWith(_game, anaphasic));
        assertTrue(yourTaitt2.isPresentWith(_game, anaphasic));
        assertFalse(opponentsTaitt.isPresentWith(_game, anaphasic));
    }

}