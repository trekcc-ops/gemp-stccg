package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.condition.missionrequirements.AndMissionRequirement;
import com.gempukku.stccg.condition.missionrequirements.AttributeMissionRequirement;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.condition.missionrequirements.RegularSkillMissionRequirement;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_155_061_Kosinski_Test extends AbstractAtTest {

    @Test
    public void kosinskiTest() throws DecisionResultInvalidException, CardNotFoundException, InvalidGameLogicException, InvalidGameOperationException, PlayerNotFoundException {
        /* Try to resolve Dangerous Climb with Taitt x2 + Kosinski. Kosinski's cunning should be reduced
        during the dilemma encounter, causing the encounter to fail. */
        initializeQuickMissionAttempt("Excavation");

        PhysicalCard climb = _game.addCardToGame("152_002", _cardLibrary, P1);

        climb.setZone(Zone.VOID);

        // Seed Dangerous Climb
        seedCardsUnder(Collections.singleton(climb), _mission);

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission);
        assertEquals(_outpost.getGameLocation(), _mission.getGameLocation());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard taitt1 = (PersonnelCard) newCardForGame("101_242", P1);
        PersonnelCard taitt2 = (PersonnelCard) newCardForGame("101_242", P1);
        PersonnelCard kosinski = (PersonnelCard) newCardForGame("155_061", P1);

        reportCardsToFacility(_outpost, taitt1, taitt2, kosinski);


        assertTrue(_outpost.hasCardInCrew(taitt1));
        assertTrue(_outpost.hasCardInCrew(taitt2));
        assertTrue(_outpost.hasCardInCrew(kosinski));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        Collection<ReportableCard> awayTeamPersonnel = new LinkedList<>();
        awayTeamPersonnel.add(taitt1);
        awayTeamPersonnel.add(taitt2);
        awayTeamPersonnel.add(kosinski);

        beamCards(P1, _outpost, awayTeamPersonnel, _mission);
        AwayTeam team = taitt1.getAwayTeam();
        MissionRequirement dilemmaRequirement = new AndMissionRequirement(
                new RegularSkillMissionRequirement(SkillName.GEOLOGY, 2),
                new AttributeMissionRequirement(CardAttribute.CUNNING, 20)
        );

        // Verify that, in a vacuum, the Away Team *could* meet the dilemma requirements
        assertTrue(dilemmaRequirement.canBeMetBy(team.getAttemptingPersonnel(_game), _game));

        // Verify that, in practice, the Away Team failed to resolve the dilemma
        attemptMission(P1, team, _mission);
        assertTrue(_mission.getLocationDeprecatedOnlyUseForTests().getSeedCards().contains(climb));
        assertFalse(_mission.getLocationDeprecatedOnlyUseForTests().isCompleted());
    }

}