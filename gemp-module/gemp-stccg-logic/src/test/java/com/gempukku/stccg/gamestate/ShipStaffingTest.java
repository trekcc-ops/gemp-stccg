package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class ShipStaffingTest extends AbstractAtTest {
    
    @Test
    public void shipStaffingTest() throws DecisionResultInvalidException, InvalidGameLogicException,
            CardNotFoundException, InvalidGameOperationException {
        initializeQuickMissionAttempt("Investigate Rogue Comet");
        assertNotNull(_mission);

        ST1EPhysicalCard friendly =
                (ST1EPhysicalCard) _game.addCardToGame("115_010", _cardLibrary, P1);

        MissionLocation missionLocation = _mission.getLocationDeprecatedOnlyUseForTests();
        seedCardsUnder(Collections.singleton(friendly), _mission);

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission.getLocationDeprecatedOnlyUseForTests());
        assertEquals(_outpost.getLocationDeprecatedOnlyUseForTests(), _mission.getLocationDeprecatedOnlyUseForTests());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard troi = (PersonnelCard) _game.addCardToGame("101_205", _cardLibrary, P1);
        PersonnelCard hobson = (PersonnelCard) _game.addCardToGame("101_202", _cardLibrary, P1);
        PersonnelCard picard = (PersonnelCard) _game.addCardToGame("101_215", _cardLibrary, P1);
        PersonnelCard data = (PersonnelCard) _game.addCardToGame("101_204", _cardLibrary, P1);
        PersonnelCard wallace = (PersonnelCard) _game.addCardToGame("101_203", _cardLibrary, P1);
        PhysicalShipCard runabout =
                (PhysicalShipCard) _game.addCardToGame("101_331", _cardLibrary, P1);

        troi.reportToFacility(_outpost);
        hobson.reportToFacility(_outpost);
        picard.reportToFacility(_outpost);
        data.reportToFacility(_outpost);
        wallace.reportToFacility(_outpost);
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
        personnelBeaming.add(wallace);

        beamCards(P1, _outpost, personnelBeaming, runabout);
        for (PersonnelCard card : personnelBeaming) {
            assertTrue(runabout.getCrew().contains(card));
            assertFalse(_outpost.getCrew().contains(card));
        }

        assertTrue(runabout.isStaffed());
    }

}