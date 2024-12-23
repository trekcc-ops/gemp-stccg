package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.condition.missionrequirements.AndMissionRequirement;
import com.gempukku.stccg.condition.missionrequirements.AttributeMissionRequirement;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.condition.missionrequirements.RegularSkillMissionRequirement;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_155_061_Kosinski_Test extends AbstractAtTest {

    @Test
    public void kosinskiTest() throws DecisionResultInvalidException, CardNotFoundException {
        /* Try to resolve Dangerous Climb with Taitt x2 + Kosinski. Kosinski's cunning should be reduced
        during the dilemma encounter, causing the encounter to fail. */
        initializeQuickMissionAttempt("Excavation");

        PhysicalCard climb = new ST1EPhysicalCard(_game, 901, _game.getPlayer(P1), _cardLibrary.get("152_002"));
        climb.setZone(Zone.VOID);

        // Seed Maglock
        _game.getGameState().seedCardsUnder(Collections.singleton(climb), _mission);

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission.getLocation());
        assertEquals(_outpost.getLocation(), _mission.getLocation());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard taitt1 = (PersonnelCard) newCardForGame("101_242", P1);
        PersonnelCard taitt2 = (PersonnelCard) newCardForGame("101_242", P1);
        PersonnelCard kosinski = (PersonnelCard) newCardForGame("155_061", P1);

        taitt1.reportToFacility(_outpost);
        taitt2.reportToFacility(_outpost);
        kosinski.reportToFacility(_outpost);


        assertTrue(_outpost.getCrew().contains(taitt1));
        assertTrue(_outpost.getCrew().contains(taitt2));
        assertTrue(_outpost.getCrew().contains(kosinski));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        Collection<PhysicalReportableCard1E> awayTeamPersonnel = new LinkedList<>();
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
        assertTrue(dilemmaRequirement.canBeMetBy(team));

        // Verify that, in practice, the Away Team failed to resolve the dilemma
        attemptMission(P1, team, _mission);
        assertTrue(_mission.getLocation().getCardsSeededUnderneath().contains(climb));
        assertFalse(_mission.getLocation().isCompleted());
    }

}