package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.*;

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
        for (MissionLocation location : _game.getGameState().getSpacelineLocations()) {
            System.out.println((location.getLocationZoneIndex(_game) + 1) + " - " + location.getLocationName());
        }

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
        MissionLocation homewardLocation = homeward.getLocationDeprecatedOnlyUseForTests();
        assertNotNull(homewardLocation);
        seedDilemma(archer, homeward);

        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA)
            skipDilemma();

        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());
        assertEquals(1, homeward.getLocationDeprecatedOnlyUseForTests().getSeedCards().size());
        assertTrue(homeward.getLocationDeprecatedOnlyUseForTests().getSeedCards().contains(archer));

        String serialized4 = _game.getGameState().serializeForPlayer(P1);
        System.out.println(serialized4.replace(",",",\n"));
    }

    private void compareUsingGetZoneCards(GameState oldGameState, GameState newGameState, Zone zone)
            throws PlayerNotFoundException {
        List<PhysicalCard> oldCards = oldGameState.getZoneCards(_game.getPlayer(P1), zone);
        List<PhysicalCard> newCards = newGameState.getZoneCards(_game.getPlayer(P1), zone);

        String oldCount = (oldCards == null) ? "null" : String.valueOf(oldCards.size());
        String newCount = (newCards == null) ? "null" : String.valueOf(newCards.size());

        if (oldCount.equals(newCount))
            System.out.println(zone.name() + ": " + oldCount);
        else
            System.out.println(zone.name() + ": " + oldCount + " (old), " + newCount + " (new)");
    }

    private void compareUsingCardZones(Iterable<PhysicalCard> allOldCards, Iterable<PhysicalCard> allNewCards,
                                       Zone zone, String playerId) {
        Collection<PhysicalCard> oldCards = new LinkedList<>();
        Collection<PhysicalCard> newCards = new LinkedList<>();

        for (PhysicalCard card : allOldCards)
            if (card.getZone() == zone && Objects.equals(card.getOwnerName(), playerId))
                oldCards.add(card);
        for (PhysicalCard card : allNewCards)
            if (card.getZone() == zone && Objects.equals(card.getOwnerName(), playerId))
                newCards.add(card);

        String oldCount = String.valueOf(oldCards.size());
        String newCount = String.valueOf(newCards.size());

        if (oldCount.equals(newCount))
            System.out.println(zone.name() + ": " + oldCount);
        else
            System.out.println(zone.name() + ": " + oldCount + " (old), " + newCount + " (new)");

    }

}