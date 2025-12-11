package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateViewTest extends AbstractAtTest {

    @Test
    public void gameStateSerializerTest() throws Exception {
        initializeIntroductoryTwoPlayerGame();

        // Figure out which player is going first
        String player1 = _game.getGameState().getPlayerOrder().getFirstPlayer();
        String player2 = _game.getOpponent(player1);

        autoSeedMissions();

        // There should now be 12 missions seeded
        assertEquals(12, _game.getGameState().getSpacelineLocations().size());

        assertEquals(Phase.SEED_DILEMMA, _game.getCurrentPhase());
        PhysicalCard archer = null;
        PhysicalCard homeward = null;
        PhysicalCard tarses = null;
        for (PhysicalCard card : _game.getGameState().getAllCardsInGame()) {
            if (Objects.equals(card.getTitle(), "Archer"))
                archer = card;
            if (Objects.equals(card.getTitle(), "Homeward"))
                homeward = card;
            if (Objects.equals(card.getTitle(), "Simon Tarses"))
                tarses = card;
        }

        assertNotNull(archer);
        assertNotNull(homeward);
        MissionLocation homewardLocation = homeward.getLocationDeprecatedOnlyUseForTests(_game);
        assertNotNull(homewardLocation);
        seedDilemma(archer, homewardLocation);

        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA)
            skipDilemma();

        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());
        assertEquals(1, homeward.getLocationDeprecatedOnlyUseForTests(_game).getSeedCards().size());
        assertTrue(homeward.getLocationDeprecatedOnlyUseForTests(_game).getSeedCards().contains(archer));

        String serialized4 = _game.getGameState().serializeForPlayer(P1);
        assertNotNull(serialized4);
    }
}