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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Blueprint_155_086_Korris_Test extends AbstractAtTest {

    @Test
    public void volunteerTest() throws Exception {
        for (int i = 0; i < 50; i++) {
            attemptMission();
        }
    }

    private void attemptMission() throws CardNotFoundException, InvalidGameOperationException, DecisionResultInvalidException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        MissionCard mission = builder.addMission("101_168", "Investigate Disturbance", P1);
        PhysicalCard armus =
                builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P1, mission);
        PersonnelCard korris =
                builder.addCardOnPlanetSurface("155_086", "Korris", P1, mission, PersonnelCard.class);
        for (int i = 0; i < 30; i++) {
            builder.addCardOnPlanetSurface("101_271", "Kle'eg", P1, mission, PersonnelCard.class);
        }
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
        assertEquals(31, _game.getGameState().getAwayTeamForCard(korris).size());
        attemptMission(P1, mission);
        useGameText(P1, korris);
        assertEquals(Zone.DISCARD, korris.getZone());
        assertEquals(Zone.REMOVED, armus.getZone());
    }

}