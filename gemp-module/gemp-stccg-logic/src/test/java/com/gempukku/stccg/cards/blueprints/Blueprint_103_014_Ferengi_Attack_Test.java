package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.MissionLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_103_014_Ferengi_Attack_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Ferengi Attack
    private FacilityCard outpost;
    private PersonnelCard picard;
    private MissionCard _mission;
    private PhysicalCard _ferengiAttack;
    private PersonnelCard troi;
    private PersonnelCard hobson;
    private PersonnelCard data;
    private ShipCard runabout;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_154", "Excavation", P1);
        outpost = builder.addFacility("101_104", P1); // Federation Outpost
        _ferengiAttack = builder.addSeedCard("103_014", "Ferengi Attack", P2, _mission);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        runabout = builder.addCardInHand("101_331", "Runabout", P1, ShipCard.class);
        troi = builder.addCardInHand("101_205", "Deanna Troi", P1, PersonnelCard.class);
        hobson = builder.addCardInHand("101_202", "Christopher Hobson", P1, PersonnelCard.class);
        picard = builder.addCardInHand("101_215", "Jean-Luc Picard", P1, PersonnelCard.class);
        data = builder.addCardInHand("101_204", "Data", P1, PersonnelCard.class);
    }


    @Test
    public void ferengiAttackFailedTest() throws Exception {
        initializeGame();

        reportCardsToFacility(List.of(troi, hobson, picard, data, runabout), outpost);

        assertTrue(outpost.hasCardInCrew(troi));
        assertTrue(outpost.hasCardInCrew(hobson));
        assertTrue(outpost.hasCardInCrew(picard));
        assertTrue(outpost.hasCardInCrew(data));
        assertFalse(outpost.hasCardInCrew(runabout));
        assertEquals(outpost, runabout.getDockedAtCard(_game));
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());
        _game.startGame();

        List<PersonnelCard> personnelBeaming = List.of(troi, hobson, picard);

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
        assertTrue(_mission.getGameLocation(_game) instanceof MissionLocation missionLocation
                && missionLocation.isCompleted());
        assertEquals(Zone.REMOVED, _ferengiAttack.getZone());
    }

}