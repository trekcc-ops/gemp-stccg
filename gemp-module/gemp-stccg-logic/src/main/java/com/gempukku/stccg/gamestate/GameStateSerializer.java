package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.Player;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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
        jsonGenerator.writeStringField("currentPhase", gameState.getCurrentPhase().name());

        jsonGenerator.writeArrayFieldStart("players");
        Set<String> playerIds = gameState.getGame().getPlayerIds();
        for (String playerId : playerIds) {
            Player player = gameState.getPlayer(playerId);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("playerId", playerId);
            jsonGenerator.writeNumberField("score", gameState.getPlayerScore(playerId));
            jsonGenerator.writeNumberField("turnNumber", gameState.getPlayersLatestTurnNumber(playerId));
            for (Zone zone : gameState._cardGroups.keySet()) {
                List<PhysicalCard> zoneCards = gameState.getZoneCards(playerId, zone);
                if (zoneCards != null)
                    writeCardIdArray(zone.name(), zoneCards, jsonGenerator);
            }
            if (gameState instanceof ST1EGameState gameState1e) {
                writeCardIdArray("SEED_DECK", gameState1e._seedDecks.get(playerId), jsonGenerator);
            }
            if (gameState instanceof TribblesGameState)
                jsonGenerator.writeBooleanField("decked", player.getDecked());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        if (gameState instanceof ST1EGameState gameState1e) {
            JsonUtils.writeArray("spacelineLocations", gameState1e._spacelineLocations, jsonGenerator);
            JsonUtils.writeArray("awayTeams", gameState1e._awayTeams, jsonGenerator);
        }

        jsonGenerator.writeObjectField("playerOrder", gameState.getPlayerOrder());

        jsonGenerator.writeArrayFieldStart("cardsInGame");
        for (PhysicalCard card : gameState._allCards.values())
            jsonGenerator.writeObject(card);
        jsonGenerator.writeEndArray();

        jsonGenerator.writeObjectField("currentProcess", gameState.getGame().getTurnProcedure().getCurrentProcess());

        jsonGenerator.writeObjectField("modifiers", gameState.getModifiersLogic().getModifiers());

        jsonGenerator.writeEndObject();

        // TODO - do we need inPlay?
    }

    private void writeCardIdArray(String fieldName, Iterable<? extends PhysicalCard> cards,
                                  JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeArrayFieldStart(fieldName);
        for (PhysicalCard card : cards) {
            int cardId = card.getCardId();
            jsonGenerator.writeNumber(cardId);
        }
        jsonGenerator.writeEndArray();
    }
}