package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_109_063_AMS_Test extends AbstractAtTest {

    @Test
    public void assignMissionSpecialistsTest() throws DecisionResultInvalidException, InvalidGameOperationException {
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
        playerDecided(P1, "0");
        assertNotNull(_userFeedback.getAwaitingDecision(P1));

        List<PhysicalCard> specialists = List.of(tarses, wallace);

        selectCards(P1, specialists);
        for (PhysicalCard specialist : specialists) {
            assertTrue(specialist.isInPlay());
            assertTrue(fedOutpost.getCrew().contains(specialist));
        }
    }

}