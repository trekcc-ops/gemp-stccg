package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateSerializationTest extends AbstractAtTest {

    @Test
    public void gameStateSerializerTest() throws Exception {
        initializeIntroductoryTwoPlayerGame();
        autoSeedMissions();

        // There should now be 12 missions seeded
        assertEquals(12, _game.getGameState().getSpacelineLocations().size());
        for (MissionLocation location : _game.getGameState().getSpacelineLocations()) {
            System.out.println((location.getLocationZoneIndex(_game) + 1) + " - " + location.getLocationName());
        }

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
        seedDilemma(archer, homewardLocation);

        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA)
            skipDilemma();

        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());
        assertEquals(1, homeward.getLocationDeprecatedOnlyUseForTests(_game).getSeedCards().size());
        assertTrue(homeward.getLocationDeprecatedOnlyUseForTests(_game).getSeedCards().contains(archer));

        ObjectWriter gameStateMapper = new GameStateMapper().writer(true);
        String gameStateJson = gameStateMapper.writeValueAsString(_game.getGameState());
        System.out.println(gameStateJson);

        boolean deserializeDirectly = true;
        ObjectMapper readMapper = new ObjectMapper();

        if (!deserializeDirectly) {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(ST1EGameState.class, new ST1EGameStateDeserializer(_game, _cardLibrary));
            readMapper.registerModule(module);
        } else {
            // some of the Player objects aren't quite correctly created
        }

        readMapper.setInjectableValues(new InjectableValues.Std().addValue(CardBlueprintLibrary.class, _cardLibrary));
        ST1EGameState gameStateCopy = readMapper.readValue(gameStateJson, ST1EGameState.class);
        List<PhysicalCard> drawDeck = gameStateCopy.getZoneCards(P1, Zone.DRAW_DECK);
        System.out.println(drawDeck.size());
        assertTrue(gameStateCopy.getAllCardsInGame().contains(drawDeck.getFirst()));
    }
}