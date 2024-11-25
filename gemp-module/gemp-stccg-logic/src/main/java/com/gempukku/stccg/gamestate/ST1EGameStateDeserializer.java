package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Region;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcessDeserializer;

import java.util.*;

public class ST1EGameStateDeserializer {

    public static ST1EGameState deserialize(ST1EGame game, JsonNode node) throws CardNotFoundException {

        ST1EGameState gameState = new ST1EGameState(game);
        gameState.setCurrentPhase(Phase.valueOf(node.get("currentPhase").textValue()));

        PlayerOrder playerOrder = new PlayerOrder(node.get("playerOrder"));
        gameState.loadPlayerOrder(playerOrder);

        Map<MissionLocation, List<Integer>> seededUnderMap = new HashMap<>();

        for (JsonNode locationNode : node.get("spacelineLocations")) {
            Quadrant quadrant = Quadrant.valueOf(locationNode.get("quadrant").textValue());
            JsonNode regionNode = locationNode.get("region");
            Region region = (regionNode == null) ? null : Region.valueOf(regionNode.textValue());
            String locationName = locationNode.get("locationName").textValue();
            MissionLocation location = new MissionLocation(quadrant, region, locationName, game);
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

        deserializeCardsInGame(node, gameState);

        JsonNode players = node.get("players");
        for (JsonNode playerNode : players) {
            String playerId = playerNode.get("playerId").asText();
            gameState._playerScores.put(playerId, playerNode.get("score").asInt());
            gameState._turnNumbers.put(playerId, playerNode.get("turnNumber").asInt());
            Player player = game.getPlayer(playerId);
            if (playerNode.has("decked"))
                player.setDecked(playerNode.get("decked").asBoolean());

            readCardIdList(Zone.SEED_DECK, playerId, playerNode, gameState, gameState._seedDecks);
            readCardIdList(Zone.STACKED, playerId, playerNode, gameState, gameState._stacked);

            for (Zone zone : gameState._cardGroups.keySet())
                readCardIdList(zone, playerId, playerNode, gameState, gameState._cardGroups.get(zone));
        }

        game.setCurrentProcess(GameProcessDeserializer.deserialize(game, node.get("currentProcess")));

        gameState.setModifiersLogic(node.get("modifiers"));

        for (JsonNode awayTeamNode : node.get("awayTeams")) {
            PhysicalCard parentCard = game.getCardFromCardId(awayTeamNode.get("parentCard").intValue());
            Player player = game.getPlayer(awayTeamNode.get("playerId").textValue());
            AwayTeam awayTeam = gameState.createNewAwayTeam(player, parentCard);
            for (JsonNode cardInAwayTeamNode : awayTeamNode.get("cardsInAwayTeam")) {
                awayTeam.add((PhysicalReportableCard1E) (game.getCardFromCardId(cardInAwayTeamNode.intValue())));
            }
        }

        for (Map.Entry<MissionLocation, List<Integer>> entry : seededUnderMap.entrySet()) {
            for (int cardId : entry.getValue())
                entry.getKey().addCardToSeededUnder(game.getCardFromCardId(cardId));
        }

        return gameState;
    }

    private static void readCardIdList(Zone zone, String playerId, JsonNode playerNode, GameState gameState,
                                       Map<String, ? extends List<PhysicalCard>> cardGroups) {
        if (playerNode.has(zone.name()) && !playerNode.get(zone.name()).isEmpty()) {
            for (JsonNode cardNode : playerNode.get(zone.name())) {
                int cardId = cardNode.asInt();
                PhysicalCard card = gameState._allCards.get(cardId);
                cardGroups.get(playerId).add(card);
            }
        }
    }

    private static void deserializeCardsInGame(JsonNode node, ST1EGameState gameState) throws CardNotFoundException {
        ST1EGame game = gameState.getGame();
        int maxCardId = 0;
        Map<PhysicalCard, Integer> attachedMap = new HashMap<>();
        Map<PhysicalCard, Integer> stackedMap = new HashMap<>();
        Map<PhysicalShipCard, Integer> dockedMap = new HashMap<>();

        for (JsonNode cardNode : node.get("cardsInGame")) {
            PhysicalCard card = gameState.getGame().getBlueprintLibrary().createST1EPhysicalCard(game, cardNode);
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