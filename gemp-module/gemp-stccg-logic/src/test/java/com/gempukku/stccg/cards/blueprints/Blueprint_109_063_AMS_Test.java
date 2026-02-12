package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.actions.turn.UseGameTextAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_109_063_AMS_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private PhysicalCard ams;
    private PhysicalCard tarses;
    private PhysicalCard wallace;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        ams = builder.addSeedDeckCard("109_063", "Assign Mission Specialists", P1);
        tarses = builder.addDrawDeckCard("101_236", "Simon Tarses", P1);
        wallace = builder.addDrawDeckCard("101_203", "Darian Wallace", P1);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1); // Federation Outpost
        builder.setPhase(Phase.SEED_FACILITY);
        builder.startGame();
    }

    @Test
    public void assignMissionSpecialistsTest()
            throws DecisionResultInvalidException, InvalidGameOperationException, CardNotFoundException {
        initializeGame();

        seedCard(P1, ams);
        assertTrue(ams.isInPlay());
        selectFirstAction(P1);
        assertNotNull(_game.getAwaitingDecision(P1));

        List<PhysicalCard> specialists = List.of(tarses, wallace);

        selectCards(P1, specialists);
        for (PhysicalCard specialist : specialists) {
            assertTrue(specialist.isInPlay());
            assertTrue(outpost.hasCardInCrew(specialist));
        }

        while (_game.getCurrentPhase() == Phase.SEED_FACILITY) {
            skipFacility();
        }

        // Try to discard card at start of turn
        assertEquals(Phase.START_OF_TURN, _game.getCurrentPhase());
        performAction(P1, UseGameTextAction.class, ams);

        assertEquals(Zone.DISCARD, ams.getZone());
        assertTrue(_game.getPlayer(P1).getDiscardPile().contains(ams));
    }

}