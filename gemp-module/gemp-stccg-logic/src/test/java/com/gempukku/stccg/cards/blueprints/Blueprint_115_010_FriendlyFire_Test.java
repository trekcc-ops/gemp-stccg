package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameLogicException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_115_010_FriendlyFire_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Maglock

    @Test
    public void placeOnMissionTest() throws DecisionResultInvalidException, InvalidGameLogicException {
        initializeQuickMissionAttempt("Investigate Rogue Comet");
        assertNotNull(_mission);

        ST1EPhysicalCard friendly =
                new ST1EPhysicalCard(_game, 901, _game.getPlayer(P1), _cardLibrary.get("115_010"));
        friendly.setZone(Zone.VOID);
        _game.getGameState().seedCardsUnder(Collections.singleton(friendly), _mission);

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission.getLocation());
        assertEquals(_outpost.getLocation(), _mission.getLocation());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard troi = new PersonnelCard(_game, 902, _game.getPlayer(P1), _cardLibrary.get("101_205"));
        PersonnelCard hobson = new PersonnelCard(_game, 903, _game.getPlayer(P1), _cardLibrary.get("101_202"));
        PersonnelCard picard = new PersonnelCard(_game, 904, _game.getPlayer(P1), _cardLibrary.get("101_215"));
        PersonnelCard data = new PersonnelCard(_game, 905, _game.getPlayer(P1), _cardLibrary.get("101_204"));
        PhysicalShipCard runabout =
                new PhysicalShipCard(_game, 906, _game.getPlayer(P1), _cardLibrary.get("101_331"));

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
        assertEquals(_outpost, runabout.getDockedAtCard());
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        List<PersonnelCard> personnelBeaming = new ArrayList<>();
        personnelBeaming.add(troi);
        personnelBeaming.add(hobson);
        personnelBeaming.add(picard);

        beamCards(P1, _outpost, personnelBeaming, runabout);
        for (PersonnelCard card : personnelBeaming) {
            assertTrue(runabout.getCrew().contains(card));
            assertFalse(_outpost.getCrew().contains(card));
        }

        undockShip(P1, runabout);
        assertFalse(friendly.isPlacedOnMission());

        attemptMission(P1, runabout, _mission);
        assertTrue(friendly.isPlacedOnMission());
    }

}