package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_103_014_Ferengi_Attack_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Ferengi Attack
    private FacilityCard outpost;
    private EquipmentCard padd;
    private PersonnelCard picard;
    private MissionCard _mission;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.setPhase(Phase.EXECUTE_ORDERS);
        _mission = builder.addMission("101_154", "Excavation", P1);
        outpost = builder.addFacility("101_104", P1); // Federation Outpost
        /*
        padd = builder.addCardInHand("101_057", "Federation PADD", P1, EquipmentCard.class);
        picard = builder.addCardInHand("101_215", "Jean-Luc Picard", P1, PersonnelCard.class);
         */
    }


    @Test
    public void ferengiAttackFailedTest() throws Exception {
        initializeGame();

        ST1EPhysicalCard ferengiAttack =
                (ST1EPhysicalCard) _game.addCardToGame("103_014", P1);
        ferengiAttack.setZone(Zone.VOID);

        // Seed Ferengi Attack
        seedCardsUnder(Collections.singleton(ferengiAttack), _mission);

        PersonnelCard troi = (PersonnelCard) _game.addCardToGame("101_205", P1);
        PersonnelCard hobson = (PersonnelCard) _game.addCardToGame("101_202", P1);
        PersonnelCard picard = (PersonnelCard) _game.addCardToGame("101_215", P1);
        PersonnelCard data = (PersonnelCard) _game.addCardToGame("101_204", P1);
        ShipCard runabout =
                (ShipCard) _game.addCardToGame("101_331", P1);

        reportCardsToFacility(List.of(troi, hobson, picard, data, runabout), outpost);

        assertTrue(outpost.hasCardInCrew(troi));
        assertTrue(outpost.hasCardInCrew(hobson));
        assertTrue(outpost.hasCardInCrew(picard));
        assertTrue(outpost.hasCardInCrew(data));
        assertFalse(outpost.hasCardInCrew(runabout));
        assertEquals(outpost, runabout.getDockedAtCard(_game));
//        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());
        _game.startGame();

        List<PersonnelCard> personnelBeaming = new ArrayList<>();
        personnelBeaming.add(troi);
        personnelBeaming.add(hobson);
        personnelBeaming.add(picard);

        beamCards(P1, outpost, personnelBeaming, _mission);
        for (PersonnelCard card : personnelBeaming) {
            assertFalse(outpost.hasCardInCrew(card));
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