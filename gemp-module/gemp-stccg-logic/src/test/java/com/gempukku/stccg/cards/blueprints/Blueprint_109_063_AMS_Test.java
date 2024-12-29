package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Blueprint_109_063_AMS_Test extends AbstractAtTest {

    @Test
    public void assignMissionSpecialistsTest() throws DecisionResultInvalidException {
        initializeGameToTestAMS();
        autoSeedMissions();

        PhysicalCard ams = null;
        PhysicalCard fedOutpost = null;

        for (PhysicalCard card : _game.getGameState().getAllCardsInGame()) {
            if (Objects.equals(card.getTitle(), "Assign Mission Specialists"))
                ams = card;
            if (Objects.equals(card.getTitle(), "Federation Outpost"))
                fedOutpost = card;
        }

        assertNotNull(ams);
        assertNotNull(fedOutpost);
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA) {
            skipDilemma();
        }

        seedCard(P1, fedOutpost);
        seedCard(P1, ams);
        assertNotNull(_userFeedback.getAwaitingDecision(P1));
    }

}