package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.MissionLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_163_044_LtCrosis_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private MissionCard _mission;
    private MissionCard mission2;
    private PersonnelCard crosis;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_170", "Investigate Raid", P1);
        mission2 = builder.addMissionToDeck("101_170", "Investigate Raid", P2);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1); // Federation Outpost
        builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P2, _mission);
        crosis = builder.addCardInHand("163_044", "Lieutenant Crosis", P1, PersonnelCard.class);
        builder.setPhase(Phase.SEED_MISSION);
        builder.startGame();
    }

    @Test
    public void affiliationIconTest() throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame();
        seedMission(mission2);
        assertTrue(_mission.isInPlay());
        assertTrue(mission2.isInPlay());
        assertEquals(_mission.getGameLocation(_game), mission2.getGameLocation(_game));

        MissionLocation missionLocation = (MissionLocation) _mission.getGameLocation(_game);
        assertFalse(missionLocation.getAffiliationIcons(_game, P1).contains(Affiliation.NON_ALIGNED));
        assertFalse(missionLocation.getAffiliationIcons(_game, P2).contains(Affiliation.NON_ALIGNED));

        skipToNextTurnAndPhase(P1, Phase.CARD_PLAY);
        playCard(P1, crosis);

        assertTrue(missionLocation.getAffiliationIcons(_game, P1).contains(Affiliation.NON_ALIGNED));
        assertTrue(missionLocation.getAffiliationIcons(_game, P2).contains(Affiliation.NON_ALIGNED));

        beamCard(P1, outpost, crosis, _mission);
        AwayTeam awayTeam = _game.getGameState().getAwayTeamForCard(crosis);
        assertTrue(awayTeam.isOnSurface(_mission.getLocationId()));

        attemptMission(P1, _mission);

        // Confirm that Crosis was killed; missions are no longer non-aligned
        assertEquals(Zone.DISCARD, crosis.getZone());
        assertFalse(missionLocation.getAffiliationIcons(_game, P1).contains(Affiliation.NON_ALIGNED));
        assertFalse(missionLocation.getAffiliationIcons(_game, P2).contains(Affiliation.NON_ALIGNED));
    }
}