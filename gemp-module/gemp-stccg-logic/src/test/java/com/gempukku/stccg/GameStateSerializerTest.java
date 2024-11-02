package com.gempukku.stccg;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.ST1ELocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateSerializerTest extends AbstractAtTest {

    @Test
    public void gameStateSerializerTest() throws DecisionResultInvalidException, InvalidGameLogicException, JsonProcessingException {
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

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(PhysicalCard.class, new CardSerializer());
        module.addSerializer(GameState.class, new GameStateSerializer());
        mapper.registerModule(module);

        List<PhysicalCard> cards = new LinkedList<>();
        cards.add(archer);
        cards.add(homeward);

        String serialized = mapper.writeValueAsString(cards);

//        String serialized = mapper.writeValueAsString(archer);
        System.out.println(serialized);

        TestClass testObj = new TestClass();
        testObj.cards.add(archer);
        testObj.cards.add(homeward);
        testObj.cards.add(tarses);
        String serialized2 = mapper.writeValueAsString(testObj);
        System.out.println(serialized2);

        String serialized3 = mapper.writeValueAsString(new SerializedGameState(_game.getGameState()));
        System.out.println(serialized3);

    }

    public class SerializedGameState {

        public final Map<String, List<Integer>> seedDecks = new HashMap<>();
        public final Map<String, List<Integer>> hands = new HashMap<>();
        public final Map<String, Integer> playerScores = new HashMap<>();
        public final Map<String, Map<String, Integer>> zoneSizes = new HashMap<>();
        public final Iterable<PhysicalCard> cardsInGame;

        public SerializedGameState(GameState gameState) {
            Map<String, Map<Zone, Integer>> zones = gameState.getZoneSizes();

            for (String playerId : gameState.getGame().getPlayerIds()) {
                seedDecks.put(playerId, new LinkedList<>());
                hands.put(playerId, new LinkedList<>());
                for (PhysicalCard card : gameState.getZoneCards(playerId, Zone.SEED_DECK)) {
                    seedDecks.get(playerId).add(card.getCardId());
                }
                for (PhysicalCard card : gameState.getZoneCards(playerId, Zone.HAND)) {
                    hands.get(playerId).add(card.getCardId());
                }

                Map<Zone, Integer> sizes = zones.get(playerId);
                zoneSizes.put(playerId, new HashMap<>());

                for (Map.Entry<Zone, Integer> entry : sizes.entrySet()) {
                    zoneSizes.get(playerId).put(entry.getKey().name(), entry.getValue());
                }

            }
            playerScores.putAll(gameState.getPlayerScores());
            cardsInGame = gameState.getAllCardsInGame();
        }
    }

    public class TestClass {
        public List<PhysicalCard> cards = new LinkedList<>();
        public int randomNumber = 5;
    }


    public class CardSerializer extends StdSerializer<PhysicalCard> {

        public CardSerializer() {
            this(null);
        }

        public CardSerializer(Class<PhysicalCard> t) {
            super(t);
        }

        @Override
        public void serialize(PhysicalCard physicalCard, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("title", physicalCard.getTitle());
            jsonGenerator.writeStringField("blueprintId", physicalCard.getBlueprintId());
            jsonGenerator.writeNumberField("cardId", physicalCard.getCardId());
            jsonGenerator.writeStringField("defaultImageUrl", physicalCard.getBlueprint().getImageUrl());
            jsonGenerator.writeStringField("affiliatedImageUrl", physicalCard.getImageUrl());
            jsonGenerator.writeStringField("owner", physicalCard.getOwnerName());
            jsonGenerator.writeStringField("zone", physicalCard.getZone().name());
            if (physicalCard.getAttachedTo() != null)
                jsonGenerator.writeNumberField("attachedToCardId", physicalCard.getAttachedTo().getCardId());
            if (physicalCard.getStackedOn() != null)
                jsonGenerator.writeNumberField("stackedOnCardId", physicalCard.getStackedOn().getCardId());
            if (physicalCard.getLocationZoneIndex() >= 0)
                jsonGenerator.writeNumberField("locationZoneIndex", physicalCard.getLocationZoneIndex());

            if (!physicalCard.getCardsSeededUnderneath().isEmpty()) {
                jsonGenerator.writeFieldName("cardsSeededUnderneath");
                jsonGenerator.writeStartArray();
                for (PhysicalCard card : physicalCard.getCardsSeededUnderneath())
                    jsonGenerator.writeNumber(card.getCardId());
                jsonGenerator.writeEndArray();
            }

            if (physicalCard instanceof PersonnelCard personnel)
                jsonGenerator.writeStringField("affiliation", personnel.getAffiliation().name());
            if (physicalCard instanceof MissionCard mission)
                jsonGenerator.writeStringField("quadrant", mission.getQuadrant().name());


            jsonGenerator.writeEndObject();
            // modifierHooks
            // modifierHooksInZone
            // modifiers
            // whileInZoneData
            // currentLocation
            // cardsPreSeededUnderneath
        }
    }

    public class GameStateSerializer extends StdSerializer<GameState> {

        public GameStateSerializer() {
            this(null);
        }

        public GameStateSerializer(Class<GameState> t) {
            super(t);
        }

        @Override
        public void serialize(GameState gameState, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
        }
    }


}