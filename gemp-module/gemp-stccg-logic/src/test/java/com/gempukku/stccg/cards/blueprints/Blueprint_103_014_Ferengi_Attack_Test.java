package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.gamestate.MissionLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_103_014_Ferengi_Attack_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Ferengi Attack

    @Test
    public void ferengiAttackFailedTest() throws Exception {
        initializeQuickMissionAttempt("Excavation");

        ST1EPhysicalCard ferengiAttack =
                (ST1EPhysicalCard) _game.addCardToGame("103_014", P1);
        ferengiAttack.setZone(Zone.VOID);

        // Seed Ferengi Attack
        MissionLocation missionLocation = _mission.getLocationDeprecatedOnlyUseForTests(_game);
        seedCardsUnder(Collections.singleton(ferengiAttack), _mission);

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission);
        assertEquals(_outpost.getLocationDeprecatedOnlyUseForTests(_game), _mission.getLocationDeprecatedOnlyUseForTests(_game));
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard troi = (PersonnelCard) _game.addCardToGame("101_205", P1);
        PersonnelCard hobson = (PersonnelCard) _game.addCardToGame("101_202", P1);
        PersonnelCard picard = (PersonnelCard) _game.addCardToGame("101_215", P1);
        PersonnelCard data = (PersonnelCard) _game.addCardToGame("101_204", P1);
        ShipCard runabout =
                (ShipCard) _game.addCardToGame("101_331", P1);

        reportCardsToFacility(List.of(troi, hobson, picard, data, runabout), _outpost);

        assertTrue(_outpost.hasCardInCrew(troi));
        assertTrue(_outpost.hasCardInCrew(hobson));
        assertTrue(_outpost.hasCardInCrew(picard));
        assertTrue(_outpost.hasCardInCrew(data));
        assertFalse(_outpost.hasCardInCrew(runabout));
        assertEquals(_outpost, runabout.getDockedAtCard(_game));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        List<PersonnelCard> personnelBeaming = new ArrayList<>();
        personnelBeaming.add(troi);
        personnelBeaming.add(hobson);
        personnelBeaming.add(picard);

        beamCards(P1, _outpost, personnelBeaming, _mission);
        for (PersonnelCard card : personnelBeaming) {
            assertFalse(_outpost.hasCardInCrew(card));
        }

        attemptMission(P1, _game.getGameState().getAwayTeamForCard(troi), _mission);
        assertNotNull(_game.getAwaitingDecision(P2));
        assertInstanceOf(ArbitraryCardsSelectionDecision.class, _game.getAwaitingDecision(P2));

        assertTrue(_game.getGameState().getAwayTeamForCard(troi).getAttemptingPersonnel(_game).contains(hobson));

        selectCard(P2, hobson);
        assertEquals(Zone.DISCARD, hobson.getZone());
        assertTrue(_mission.getLocationDeprecatedOnlyUseForTests(_game).isCompleted());
    }

}