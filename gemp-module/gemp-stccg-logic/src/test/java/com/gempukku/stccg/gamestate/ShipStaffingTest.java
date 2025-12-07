package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class ShipStaffingTest extends AbstractAtTest {
    
    @Test
    public void shipStaffingTest() throws DecisionResultInvalidException, InvalidGameLogicException,
            CardNotFoundException, InvalidGameOperationException, PlayerNotFoundException {
        initializeQuickMissionAttempt("Investigate Rogue Comet");
        assertNotNull(_mission);

        ST1EPhysicalCard friendly =
                (ST1EPhysicalCard) _game.addCardToGame("115_010", P1);

        MissionLocation missionLocation = _mission.getLocationDeprecatedOnlyUseForTests();
        seedCardsUnder(Collections.singleton(friendly), _mission);

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission);
        assertEquals(_outpost.getLocationDeprecatedOnlyUseForTests(), _mission.getLocationDeprecatedOnlyUseForTests());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard troi = (PersonnelCard) _game.addCardToGame("101_205", P1);
        PersonnelCard hobson = (PersonnelCard) _game.addCardToGame("101_202", P1);
        PersonnelCard picard = (PersonnelCard) _game.addCardToGame("101_215", P1);
        PersonnelCard data = (PersonnelCard) _game.addCardToGame("101_204", P1);
        PersonnelCard wallace = (PersonnelCard) _game.addCardToGame("101_203", P1);
        ShipCard runabout =
                (ShipCard) _game.addCardToGame("101_331", P1);

        reportCardsToFacility(List.of(troi, hobson, picard, data, wallace, runabout), _outpost);

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
        personnelBeaming.add(wallace);

        beamCards(P1, _outpost, personnelBeaming, runabout);
        for (PersonnelCard card : personnelBeaming) {
            assertTrue(runabout.hasCardInCrew(card));
            assertFalse(_outpost.hasCardInCrew(card));
        }

        assertTrue(runabout.isStaffed(_game));
    }

}