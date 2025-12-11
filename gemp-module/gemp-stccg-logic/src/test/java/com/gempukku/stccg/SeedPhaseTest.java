package com.gempukku.stccg;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class SeedPhaseTest extends AbstractAtTest {

    @Test
    public void autoSeedTest() throws DecisionResultInvalidException, PlayerNotFoundException, InvalidGameOperationException {
        initializeIntroductoryTwoPlayerGame();

        // Figure out which player is going first
        String playerId1 = _game.getGameState().getPlayerOrder().getFirstPlayer();
        String playerId2 = _game.getOpponent(playerId1);
        Player player1 = _game.getPlayer(playerId1);
        Player player2 = _game.getPlayer(playerId2);

        autoSeedMissions();

        // There should now be 12 missions seeded
        assertEquals(12, _game.getGameState().getSpacelineLocations().size());

        assertEquals(Phase.SEED_DILEMMA, _game.getCurrentPhase());
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA)
            skipDilemma();

        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());
        autoSeedFacility();

        // Verify that both facilities were seeded
        assertEquals(2, Filters.filterCardsInPlay(_game, CardType.FACILITY).size());

        // Verify that the seed phase is over and both players have drawn starting hands
        assertEquals(Phase.CARD_PLAY, _game.getGameState().getCurrentPhase());
        assertEquals(7, player1.getCardsInHand().size());
        assertEquals(7, player2.getCardsInHand().size());
    }

    @Test
    public void seedDilemmasTest() throws Exception {
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
        for (PhysicalCard card : _game.getGameState().getAllCardsInGame()) {
            if (Objects.equals(card.getTitle(), "Archer"))
                archer = card;
            if (Objects.equals(card.getTitle(), "Homeward"))
                homeward = card;
        }

        assertNotNull(archer);
        assertNotNull(homeward);
        MissionLocation homewardLocation = homeward.getLocationDeprecatedOnlyUseForTests(_game);
        assertNotNull(homewardLocation);
        assertNotEquals(homeward.getOwnerName(), archer.getOwnerName());

        Player archerOwner = _game.getPlayer(archer.getOwnerName());

        assertEquals(0, homewardLocation.getPreSeedCardCountForPlayer(archerOwner));
        seedDilemma(archer, homewardLocation);
        assertEquals(1, homewardLocation.getPreSeedCardCountForPlayer(archerOwner));
        removeDilemma(archer, homewardLocation);
        assertEquals(0, homewardLocation.getPreSeedCardCountForPlayer(archerOwner));
        seedDilemma(archer, homewardLocation);
        assertEquals(1, homewardLocation.getPreSeedCardCountForPlayer(archerOwner));

        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA)
            skipDilemma();

        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());
        assertEquals(1, homewardLocation.getSeedCards().size());
        assertTrue(homewardLocation.getSeedCards().contains(archer));
    }

}