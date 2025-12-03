package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Region;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ST1EGameStateDeserializer {

    public static ST1EGameState deserialize(ST1EGame game, JsonNode node) throws CardNotFoundException,
            InvalidGameLogicException, JsonProcessingException, PlayerNotFoundException {

        ObjectMapper mapper = new ObjectMapper();

        List<String> playerIds = new ArrayList<>();

            // TODO - Pull timer from serialized game state
        ST1EGameState gameState = new ST1EGameState(playerIds, game, GameTimer.GLACIAL_TIMER);

        gameState.setCurrentPhase(mapper.treeToValue(node.get("currentPhase"), Phase.class));
        gameState.loadPlayerOrder(mapper.treeToValue(node.get("playerOrder"), PlayerOrder.class));

        Map<MissionLocation, List<Integer>> seededUnderMap = new HashMap<>();

        for (JsonNode locationNode : node.get("spacelineLocations")) {
            Quadrant quadrant = Quadrant.valueOf(locationNode.get("quadrant").textValue());
            JsonNode regionNode = locationNode.get("region");
            Region region = (regionNode == null) ? null : Region.valueOf(regionNode.textValue());
            String locationName = locationNode.get("locationName").textValue();
            int locationId = locationNode.get("locationId").intValue();
            MissionLocation location = new MissionLocation(quadrant, region, locationName, locationId);
            gameState._spacelineLocations.add(location);
            if (locationNode.has("cardsSeededUnderneath")) {
                seededUnderMap.put(location, new ArrayList<>());
                for (JsonNode seedCardNode : locationNode.get("cardsSeededUnderneath"))
                    seededUnderMap.get(location).add(seedCardNode.intValue());
            }
            if (locationNode.has("isCompleted") && locationNode.get("isCompleted") != null) {
                boolean completed = locationNode.get("isCompleted").booleanValue();
                location.setCompleted(completed);
            }
        }

        deserializeCardsInGame(node, gameState, game);

        JsonNode players = node.get("players");
        for (JsonNode playerNode : players) {
            String playerId = playerNode.get("playerId").asText();
            Player player = game.getPlayer(playerId);
            player.setScore(playerNode.get("score").asInt());
            if (playerNode.has("decked"))
                player.setDecked(playerNode.get("decked").asBoolean());
            for (Zone zone : player.getCardGroupZones())
                readCardIdList(zone, player, playerNode, gameState);
        }

        GameProcess currentProcess = mapper.treeToValue(node.get("currentProcess"), GameProcess.class);
        gameState.setCurrentProcess(currentProcess);

        gameState.setModifiersLogic(game);

        for (Map.Entry<MissionLocation, List<Integer>> entry : seededUnderMap.entrySet()) {
            MissionLocation location = entry.getKey();
            for (int cardId : entry.getValue()) {
                PhysicalCard card = game.getCardFromCardId(cardId);
                location.seedCardUnderMission(game, card);
            }
        }

        return gameState;
    }

    private static void readCardIdList(Zone zone, Player player, JsonNode playerNode, GameState gameState) throws InvalidGameLogicException {
        if (playerNode.has(zone.name()) && !playerNode.get(zone.name()).isEmpty()) {
            for (JsonNode cardNode : playerNode.get(zone.name())) {
                int cardId = cardNode.asInt();
                PhysicalCard card = gameState._allCards.get(cardId);
                player.addCardToGroup(zone, card);
            }
        }
    }


    private static void deserializeCardsInGame(JsonNode node, ST1EGameState gameState, ST1EGame game)
            throws CardNotFoundException, PlayerNotFoundException {
        int maxCardId = 0;
        Map<PhysicalCard, Integer> attachedMap = new HashMap<>();
        Map<PhysicalCard, Integer> stackedMap = new HashMap<>();
        Map<PhysicalShipCard, Integer> dockedMap = new HashMap<>();

        for (JsonNode cardNode : node.get("cardsInGame")) {
            PhysicalCard card = game.getBlueprintLibrary().createST1EPhysicalCard(game, cardNode);
            gameState._allCards.put(card.getCardId(), card);
            if (card.getZone().isInPlay())
                gameState._inPlay.add(card);
            if (cardNode.has("attachedToCardId"))
                attachedMap.put(card, cardNode.get("attachedToCardId").intValue());
            if (cardNode.has("stackedOnCardId"))
                stackedMap.put(card, cardNode.get("stackedOnCardId").intValue());
            if (cardNode.has("dockedAtCardId") && card instanceof PhysicalShipCard ship)
                dockedMap.put(ship, cardNode.get("dockedAtCardId").intValue());
            maxCardId = Math.max(cardNode.get("cardId").intValue(), maxCardId);
        }
        gameState.setNextCardId(maxCardId + 1);

        for (Map.Entry<PhysicalCard, Integer> entry : attachedMap.entrySet())
            entry.getKey().attachTo(game.getCardFromCardId(entry.getValue()));
        for (Map.Entry<PhysicalCard, Integer> entry : stackedMap.entrySet())
            entry.getKey().stackOn(game.getCardFromCardId(entry.getValue()));
        for (Map.Entry<PhysicalShipCard, Integer> entry : dockedMap.entrySet())
            entry.getKey().dockAtFacility((FacilityCard) game.getCardFromCardId(entry.getValue()));
    }

}