package com.gempukku.stccg;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.gamestate.ST1ELocation;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class SeedPhaseTest extends AbstractAtTest {

    @Test
    public void autoSeedTest() throws DecisionResultInvalidException {
        initializeIntroductoryTwoPlayerGame();

        // Figure out which player is going first
        String player1 = _game.getGameState().getPlayerOrder().getFirstPlayer();
        String player2 = _game.getOpponent(player1);

        autoSeedMissions();

        // There should now be 12 missions seeded
        assertEquals(12, _game.getGameState().getSpacelineLocations().size());
        for (ST1ELocation location : _game.getGameState().getSpacelineLocations()) {
            System.out.println((location.getLocationZoneIndex()+1) + " - " + location.getLocationName());
        }

        assertEquals(Phase.SEED_DILEMMA, _game.getCurrentPhase());
        skipDilemma();
        skipDilemma();
        skipDilemma();

        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());
        autoSeedFacility();

        // Verify that both facilities were seeded
        assertEquals(2, Filters.filterActive(_game, CardType.FACILITY).size());
        for (PhysicalCard card : Filters.filterActive(_game, CardType.FACILITY)) {
            System.out.println(card.getTitle() + " seeded at " + card.getLocation().getLocationName());
        }

        // Verify that the seed phase is over and both players have drawn starting hands
        assertEquals(Phase.CARD_PLAY, _game.getGameState().getCurrentPhase());
        assertEquals(7, _game.getGameState().getHand(player1).size());
        assertEquals(7, _game.getGameState().getHand(player2).size());
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
        for (ST1ELocation location : _game.getGameState().getSpacelineLocations()) {
            System.out.println((location.getLocationZoneIndex() + 1) + " - " + location.getLocationName());
        }

        assertEquals(Phase.SEED_DILEMMA, _game.getCurrentPhase());
        PhysicalCard archer = null;
        PhysicalCard anaphasicOrganism = null;
        PhysicalCard homeward = null;
        for (PhysicalCard card : _game.getGameState().getAllCardsInGame()) {
            if (Objects.equals(card.getTitle(), "Archer"))
                archer = card;
            if (Objects.equals(card.getTitle(), "Anaphasic Organism"))
                anaphasicOrganism = card;
            if (Objects.equals(card.getTitle(), "Homeward"))
                homeward = card;
        }

        assertNotEquals(null, archer);
        assertNotEquals(null, anaphasicOrganism);
        assertNotEquals(null, homeward);

        assertEquals(0, homeward.getCardsPreSeeded(archer.getOwner()).size());
        seedDilemma(archer, homeward);
        assertEquals(1, homeward.getCardsPreSeeded(archer.getOwner()).size());
        removeDilemma(archer, homeward);
        assertEquals(0, homeward.getCardsPreSeeded(archer.getOwner()).size());
        seedDilemma(archer, homeward);
        assertEquals(1, homeward.getCardsPreSeeded(archer.getOwner()).size());

        skipDilemma();
        skipDilemma();
        skipDilemma();

        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());
        assertEquals(1, homeward.getCardsSeededUnderneath().size());
        assertTrue(homeward.getCardsSeededUnderneath().contains(archer));
    }

}