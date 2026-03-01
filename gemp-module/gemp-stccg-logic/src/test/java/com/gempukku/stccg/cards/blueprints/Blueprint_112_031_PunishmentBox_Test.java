package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.game.GameTestBuilder;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_112_031_PunishmentBox_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private MissionCard mission;
    private PersonnelCard kallis;
    private PhysicalCard punishmentBox;
    private PhysicalCard armus;
    private PersonnelCard kallis2;
    private PersonnelCard togaran;

    private void initializeGame(boolean includeOfficer) throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission(MissionType.PLANET, Affiliation.BAJORAN, P1);
        outpost = builder.addOutpost(Affiliation.BAJORAN, P1);
        armus = builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P1, mission);
        punishmentBox = builder.addSeedCardUnderMission("112_031", "Punishment Box", P1, mission);

        // non-OFFICER
        kallis = builder.addCardOnPlanetSurface("112_152", "Kallis Ven", P1, mission, PersonnelCard.class);

        // OFFICERS
        if (includeOfficer) {
            // OFFICER
            togaran = builder.addCardOnPlanetSurface("202_056", "Togaran", P1, mission, PersonnelCard.class);
            kallis2 = builder.addCardInHand("112_152", "Kallis Ven", P1, PersonnelCard.class);
        } else {
            // non-OFFICER
            kallis2 = builder.addCardOnPlanetSurface("112_152", "Kallis Ven", P1, mission, PersonnelCard.class);
        }

        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void failDilemmaTest() throws Exception {
        initializeGame(false);
        AttemptMissionAction attemptAction = attemptMission(P1, mission);
        assertEquals(mission, punishmentBox.getAtopCard());
        assertTrue(kallis.isStopped());
        assertTrue(kallis2.isStopped());
        assertTrue(attemptAction.wasFailed());
        assertTrue(mission.getGameLocation(_game).hasCardSeededUnderneath(armus));

        // Try again next turn. Still no OFFICER, so we fail again.
        skipToNextTurnAndPhase(P1, Phase.EXECUTE_ORDERS);
        AttemptMissionAction attemptAction2 = attemptMission(P1, mission);
        assertTrue(kallis.isStopped());
        assertTrue(kallis2.isStopped());
        assertTrue(attemptAction2.wasFailed());
        assertTrue(mission.getGameLocation(_game).hasCardSeededUnderneath(armus));
    }

    @Test
    public void overcomeDilemmaTest() throws Exception {
        initializeGame(true);
        AttemptMissionAction attemptAction = attemptMission(P1, mission);
        assertEquals(mission, punishmentBox.getAtopCard());

        // Togaran is stopped, so the attempt continues and Armus kills Kallis
        assertTrue(togaran.isStopped());
        assertFalse(mission.getGameLocation(_game).hasCardSeededUnderneath(armus));
        assertTrue(personnelWasKilled(kallis));

        // Next turn, Togaran is unstopped; we try again
        skipToNextTurnAndPhase(P1, Phase.CARD_PLAY);
        assertFalse(togaran.isStopped());
        playCard(P1, kallis2);
        beamCard(P1, outpost, kallis2, mission);

        skipPhase(Phase.CARD_PLAY);
        attemptMission(P1, mission);
        assertTrue(togaran.isStopped());
        assertFalse(kallis2.isStopped());
    }

    
}