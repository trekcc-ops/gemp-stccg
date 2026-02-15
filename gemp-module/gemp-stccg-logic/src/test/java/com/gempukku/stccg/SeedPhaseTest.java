package com.gempukku.stccg;

import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SeedPhaseTest extends AbstractAtTest {

    private PhysicalCard maglock;
    private MissionCard playerTwoMission;
    private FacilityCard outpost1;
    private FacilityCard outpost2;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);

        for (int i = 0; i < 6; i++) {
            // 6 copies of the same universal mission for each player
            builder.addMissionToDeck("161_021", "Advanced Combat Training", P1);
            playerTwoMission = builder.addMissionToDeck("161_021", "Advanced Combat Training", P2);
        }

        outpost1 = builder.addSeedDeckCard("101_106", "Romulan Outpost", P1, FacilityCard.class);
        outpost2 = builder.addSeedDeckCard("101_106", "Romulan Outpost", P2, FacilityCard.class);
        maglock = builder.addSeedDeckCard("109_010", "Maglock", P1);

        _game = builder.getGame();
        builder.setPhase(Phase.SEED_MISSION);
        builder.startGame();
    }

    @Test
    public void autoSeedTest() throws DecisionResultInvalidException, InvalidGameOperationException, CardNotFoundException {
        initializeGame();
        autoSeedMissions();

        // There should now be 12 missions seeded
        assertEquals(12, _game.getGameState().getUnorderedMissionLocations().size());

        assertEquals(Phase.SEED_DILEMMA, _game.getCurrentPhase());
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA)
            skipDilemma();

        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());
        seedFacility(P1, outpost1, playerTwoMission);
        seedFacility(P2, outpost2, playerTwoMission);

        // Verify that both facilities were seeded
        assertEquals(2, Filters.filterCardsInPlay(_game, CardType.FACILITY).size());

        // Verify that the seed phase is over and both players have drawn starting hands
        assertEquals(7, _game.getPlayer(P1).getCardsInHand().size());
        assertEquals(7, _game.getPlayer(P2).getCardsInHand().size());
    }

    @Test
    public void seedDilemmasTest() throws Exception {
        initializeGame();
        autoSeedMissions();

        assertEquals(12, _game.getGameState().getUnorderedMissionLocations().size());

        assertEquals(Phase.SEED_DILEMMA, _game.getCurrentPhase());

        MissionLocation missionLocation = playerTwoMission.getLocationDeprecatedOnlyUseForTests(_game);
        assertNotNull(missionLocation);
        assertNotEquals(playerTwoMission.getOwnerName(), maglock.getOwnerName());

        Player archerOwner = _game.getPlayer(maglock.getOwnerName());

        assertEquals(0, missionLocation.getPreSeedCardCountForPlayer(archerOwner));
        seedDilemma(maglock, missionLocation);
        assertEquals(1, missionLocation.getPreSeedCardCountForPlayer(archerOwner));
        removeDilemma(maglock, missionLocation);
        assertEquals(0, missionLocation.getPreSeedCardCountForPlayer(archerOwner));
        seedDilemma(maglock, missionLocation);
        assertEquals(1, missionLocation.getPreSeedCardCountForPlayer(archerOwner));

        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA)
            skipDilemma();

        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());
        assertEquals(1, missionLocation.getSeedCards().size());
        assertTrue(missionLocation.getSeedCards().contains(maglock));
    }

}