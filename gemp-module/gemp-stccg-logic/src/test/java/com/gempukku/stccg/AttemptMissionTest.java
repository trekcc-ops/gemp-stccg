package com.gempukku.stccg;

import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AttemptMissionTest extends AbstractAtTest {

    PersonnelCard picard;
    MissionCard mission;
    FacilityCard outpost;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission("101_154", "Excavation", P1);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1);
        picard = builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P1, outpost, PersonnelCard.class);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void attemptMissionTest() throws DecisionResultInvalidException, InvalidGameOperationException, CardNotFoundException {
        initializeGame();

        beamCard(P1, outpost, picard, mission);
        assertTrue(_game.getGameState().getAwayTeamForCard(picard).isOnSurface(mission.getLocationDeprecatedOnlyUseForTests(_game)));

        attemptMission(P1, mission);

        // Confirm that mission was solved and player earned points
        assertTrue(mission.getLocationDeprecatedOnlyUseForTests(_game).isCompleted());
        assertEquals(mission.getPoints(), _game.getPlayer(P1).getScore());
    }

}