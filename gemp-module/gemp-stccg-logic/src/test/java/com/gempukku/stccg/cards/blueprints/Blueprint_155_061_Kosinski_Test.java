package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.missionrequirements.AndMissionRequirement;
import com.gempukku.stccg.condition.missionrequirements.AttributeMissionRequirement;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.condition.missionrequirements.RegularSkillMissionRequirement;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_155_061_Kosinski_Test extends AbstractAtTest {

    private PersonnelCard kosinski;
    private PersonnelCard taitt1;
    private PersonnelCard taitt2;
    private PhysicalCard climb;
    private MissionCard mission;
    private FacilityCard outpost;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission("101_154", "Excavation", P1);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1);
        climb = builder.addSeedCardUnderMission("152_002", "Dangerous Climb", P1, mission);
        kosinski = builder.addCardAboardShipOrFacility("155_061", "Kosinski", P1, outpost, PersonnelCard.class);
        taitt1 = builder.addCardAboardShipOrFacility("101_242", "Taitt", P1, outpost, PersonnelCard.class);
        taitt2 = builder.addCardAboardShipOrFacility("101_242", "Taitt", P1, outpost, PersonnelCard.class);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void kosinskiTest() throws DecisionResultInvalidException, CardNotFoundException,
            InvalidGameOperationException {
        initializeGame();

        Collection<ReportableCard> awayTeamPersonnel = List.of(taitt1, taitt2, kosinski);

        beamCards(P1, outpost, awayTeamPersonnel, mission);
        AwayTeam team = _game.getGameState().getAwayTeamForCard(taitt1);
        MissionRequirement dilemmaRequirement = new AndMissionRequirement(
                new RegularSkillMissionRequirement(SkillName.GEOLOGY, 2),
                new AttributeMissionRequirement(CardAttribute.CUNNING, 20)
        );

        // Verify that, in a vacuum, the Away Team *could* meet the dilemma requirements
        assertTrue(dilemmaRequirement.canBeMetBy(team.getAttemptingPersonnel(_game), _game));

        // Verify that, in practice, the Away Team failed to resolve the dilemma
        attemptMission(P1, team, mission);
        assertTrue(mission.getLocationDeprecatedOnlyUseForTests(_game).getSeedCards().contains(climb));
        assertFalse(mission.getLocationDeprecatedOnlyUseForTests(_game).isCompleted());
    }

}