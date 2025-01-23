package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.gamestate.ST1EGameStateDeserializer;
import com.gempukku.stccg.gamestate.MissionLocation;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateSerializerTest extends AbstractAtTest {

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

        assertNotEquals(null, archer);
        assertNotNull(homeward);
        MissionLocation homewardLocation = homeward.getLocation();
        assertNotNull(homewardLocation);

        assertEquals(0, homewardLocation.getCardsPreSeeded(archer.getOwner()).size());
        seedDilemma(archer, homeward);
        assertEquals(1, homewardLocation.getCardsPreSeeded(archer.getOwner()).size());
        removeDilemma(archer, homeward);
        assertEquals(0, homewardLocation.getCardsPreSeeded(archer.getOwner()).size());
        seedDilemma(archer, homeward);
        assertEquals(1, homewardLocation.getCardsPreSeeded(archer.getOwner()).size());

        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA)
            skipDilemma();

        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());
        assertEquals(1, homeward.getLocation().getCardsSeededUnderneath().size());
        assertTrue(homeward.getLocation().getCardsSeededUnderneath().contains(archer));

        ObjectMapper mapper = new ObjectMapper();

        List<PhysicalCard> cards = new LinkedList<>();
        cards.add(archer);
        cards.add(homeward);

        String serialized4 = mapper.writeValueAsString(_game.getGameState());
        System.out.println(serialized4.replace(",",",\n"));

        ST1EGameState oldGameState = _game.getGameState();
        ST1EGameState newGameState = ST1EGameStateDeserializer.deserialize(_game, mapper.readTree(serialized4));

        System.out.println("\nP1 card group sizes using getZoneCards\n");
        for (Zone zone : Zone.values())
            compareUsingGetZoneCards(oldGameState, newGameState, zone);

        Iterable<PhysicalCard> allOldCards = oldGameState.getAllCardsInGame();
        Iterable<PhysicalCard> allNewCards = newGameState.getAllCardsInGame();

        System.out.println("\n" + P1 + " zone sizes using PhysicalCard getZone\n");
        for (Zone zone : Zone.values()) {
            compareUsingCardZones(allOldCards, allNewCards, zone, P1);
        }

        System.out.println("\n" + P2 + " zone sizes using PhysicalCard getZone\n");
        for (Zone zone : Zone.values()) {
            compareUsingCardZones(allOldCards, allNewCards, zone, P2);
        }

    }

    private void compareUsingGetZoneCards(GameState oldGameState, GameState newGameState, Zone zone) {
        List<PhysicalCard> oldCards = oldGameState.getZoneCards(P1, zone);
        List<PhysicalCard> newCards = newGameState.getZoneCards(P1, zone);

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