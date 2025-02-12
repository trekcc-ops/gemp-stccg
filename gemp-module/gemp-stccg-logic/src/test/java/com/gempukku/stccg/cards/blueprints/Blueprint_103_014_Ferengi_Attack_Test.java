package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
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
    public void ferengiAttackFailedTest() throws DecisionResultInvalidException, InvalidGameLogicException,
            CardNotFoundException, InvalidGameOperationException {
        initializeQuickMissionAttempt("Excavation");

        ST1EPhysicalCard ferengiAttack =
                (ST1EPhysicalCard) _game.addCardToGame("103_014", _cardLibrary, P1);
        ferengiAttack.setZone(Zone.VOID);

        // Seed Ferengi Attack
        MissionLocation missionLocation = _mission.getLocationDeprecatedOnlyUseForTests();
        seedCardsUnder(Collections.singleton(ferengiAttack), _mission);

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission.getLocationDeprecatedOnlyUseForTests());
        assertEquals(_outpost.getLocationDeprecatedOnlyUseForTests(), _mission.getLocationDeprecatedOnlyUseForTests());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard troi = (PersonnelCard) _game.addCardToGame("101_205", _cardLibrary, P1);
        PersonnelCard hobson = (PersonnelCard) _game.addCardToGame("101_202", _cardLibrary, P1);
        PersonnelCard picard = (PersonnelCard) _game.addCardToGame("101_215", _cardLibrary, P1);
        PersonnelCard data = (PersonnelCard) _game.addCardToGame("101_204", _cardLibrary, P1);
        PhysicalShipCard runabout =
                (PhysicalShipCard) _game.addCardToGame("101_331", _cardLibrary, P1);

        troi.reportToFacility(_outpost);
        hobson.reportToFacility(_outpost);
        picard.reportToFacility(_outpost);
        data.reportToFacility(_outpost);
        runabout.reportToFacility(_outpost);

        assertTrue(_outpost.getCrew().contains(troi));
        assertTrue(_outpost.getCrew().contains(hobson));
        assertTrue(_outpost.getCrew().contains(picard));
        assertTrue(_outpost.getCrew().contains(data));
        assertFalse(_outpost.getCrew().contains(runabout));
        assertEquals(_outpost, runabout.getDockedAtCard(_game));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        List<PersonnelCard> personnelBeaming = new ArrayList<>();
        personnelBeaming.add(troi);
        personnelBeaming.add(hobson);
        personnelBeaming.add(picard);

        beamCards(P1, _outpost, personnelBeaming, _mission);
        for (PersonnelCard card : personnelBeaming) {
            assertFalse(_outpost.getCrew().contains(card));
        }
        assertEquals(troi.getAwayTeam(), hobson.getAwayTeam());

        attemptMission(P1, troi.getAwayTeam(), _mission);
        assertNotNull(_userFeedback.getAwaitingDecision(P2));
        assertInstanceOf(ArbitraryCardsSelectionDecision.class, _userFeedback.getAwaitingDecision(P2));

        CardFilter inAwayTeamFilter = Filters.personnelInAttemptingUnit(troi.getAwayTeam());
        assertTrue(inAwayTeamFilter.accepts(_game, hobson));

        selectCard(P2, hobson);
        assertEquals(Zone.DISCARD, hobson.getZone());
        assertTrue(_mission.getLocationDeprecatedOnlyUseForTests().isCompleted());
    }

}