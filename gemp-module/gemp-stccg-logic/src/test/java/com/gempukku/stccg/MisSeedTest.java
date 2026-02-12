package com.gempukku.stccg;

import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MisSeedTest extends AbstractAtTest {

    private PersonnelCard picard;
    private MissionCard mission;
    private FacilityCard outpost;
    private PhysicalCard tarses;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission("101_154", "Excavation", P1);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1);
        tarses = builder.addSeedCardUnderMission("101_236", "Simon Tarses", P1, mission);
        picard = builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P1, outpost, PersonnelCard.class);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void misSeedTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame();

        beamCard(P1, outpost, picard, mission);
        attemptMission(P1, mission);

        // Confirm that mission was not solved and Simon Tarses was removed from play
        assertEquals(Zone.REMOVED, tarses.getZone());
        assertFalse(mission.getLocationDeprecatedOnlyUseForTests(_game).isCompleted());
    }
}