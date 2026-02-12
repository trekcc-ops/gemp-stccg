package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.cards.AwayTeam;
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

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_015_Armus_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private PersonnelCard picard;
    private MissionCard _mission;
    private PhysicalCard _armus;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_154", "Excavation", P1);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1);
        _armus = builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P2, _mission);
        picard = builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P1, outpost, PersonnelCard.class);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void armusTest() throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame();

        // Beam Picard to the planet
        beamCard(P1, outpost, picard, _mission);
        AwayTeam awayTeam = _game.getGameState().getAwayTeamForCard(picard);
        assertTrue(awayTeam.isOnSurface(_mission.getLocationId()));

        attemptMission(P1, _mission);

        // Confirm that Picard was discarded
        assertEquals(Zone.DISCARD, picard.getZone());

        // Confirm that Armus was removed from the game
        assertEquals(Zone.REMOVED, _armus.getZone());

        // Confirm the game has no Away Teams remaining
        assertEquals(0, _game.getGameState().getAwayTeams().size());

        // Confirm that mission was not solved
        assertFalse(_mission.getLocationDeprecatedOnlyUseForTests(_game).isCompleted());

        // Confirm the mission attempt was added to performed actions
        int missionAttempts = 0;
        for (Action action : _game.getActionsEnvironment().getPerformedActions())
            if (action instanceof AttemptMissionAction missionAction && missionAction.wasFailed())
                missionAttempts++;
        assertEquals(1, missionAttempts);
    }
}