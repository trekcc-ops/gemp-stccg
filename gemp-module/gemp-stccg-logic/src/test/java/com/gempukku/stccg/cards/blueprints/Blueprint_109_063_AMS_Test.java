package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.discard.DiscardSingleCardAction;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_109_063_AMS_Test extends AbstractAtTest {

    @Test
    public void assignMissionSpecialistsTest()
            throws DecisionResultInvalidException, InvalidGameOperationException, PlayerNotFoundException {
        initializeGameToTestAMS();
        autoSeedMissions();

        PhysicalCard ams = null;
        FacilityCard fedOutpost = null;
        PhysicalCard wallace = null;
        PhysicalCard tarses = null;

        for (PhysicalCard card : _game.getGameState().getAllCardsInGame()) {
            if (Objects.equals(card.getTitle(), "Assign Mission Specialists"))
                ams = card;
            if (Objects.equals(card.getTitle(), "Federation Outpost"))
                fedOutpost = (FacilityCard) card;
            if (Objects.equals(card.getTitle(), "Darian Wallace"))
                wallace = card;
            if (Objects.equals(card.getTitle(), "Simon Tarses"))
                tarses = card;
        }

        assertNotNull(ams);
        assertNotNull(fedOutpost);
        assertNotNull(tarses);
        assertNotNull(wallace);
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA) {
            skipDilemma();
        }

        seedCard(P1, fedOutpost);
        seedCard(P1, ams);
        selectFirstAction(P1);
        assertNotNull(_userFeedback.getAwaitingDecision(P1));

        List<PhysicalCard> specialists = List.of(tarses, wallace);

        selectCards(P1, specialists);
        for (PhysicalCard specialist : specialists) {
            assertTrue(specialist.isInPlay());
            assertTrue(fedOutpost.hasCardInCrew(specialist));
        }

        while (_game.getCurrentPhase() == Phase.SEED_FACILITY) {
            skipFacility();
        }

        // Try to discard card at start of turn
        assertEquals(Phase.START_OF_TURN, _game.getCurrentPhase());
        performAction(P1, DiscardSingleCardAction.class, ams);

        assertEquals(Zone.DISCARD, ams.getZone());
        assertTrue(_game.getPlayer(P1).getDiscardPile().contains(ams));
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());
    }

}