package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.common.JsonViews;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.processes.GameProcess;

import java.io.IOException;
import java.util.Objects;

public class GameStateSerializer extends StdSerializer<GameState> {

    private final String _userId;
    private final boolean _showComplete;
    private final ObjectMapper _mapper = new ObjectMapper();

    public GameStateSerializer() {
        this(null, null, false);
    }

    public GameStateSerializer(Class<GameState> t) {
        this(t, null, false);
    }

    public GameStateSerializer(Class<GameState> t, String userId, boolean showComplete) {
        super(t);
        _userId = userId;
        _showComplete = showComplete;
    }

    @Override
    public void serialize(GameState gameState, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
/*        @JsonIncludeProperties({ "currentPhase", "players", "spacelineLocations", "awayTeams", "playerOrder",
                "currentProcess", "cardsInGame" }) */

        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("currentPhase", gameState.getCurrentPhase());

        jsonGenerator.writeArrayFieldStart("players");
        for (Player player : gameState.getPlayers()) {
            if (Objects.equals(_userId, player.getPlayerId()) || _showComplete) {
                _mapper.writerWithView(JsonViews.Public.class).writeValue(jsonGenerator, player);
            } else {
                _mapper.writerWithView(JsonViews.Private.class).writeValue(jsonGenerator, player);
            }
        }
        jsonGenerator.writeEndArray();


        // TODO - spacelineLocations, awayTeams
        if (gameState instanceof ST1EGameState gameState1e) {
            JsonUtils.writeArray("spacelineLocations", gameState1e._spacelineLocations, jsonGenerator);
            JsonUtils.writeArray("awayTeams", gameState1e._awayTeams, jsonGenerator);
        }

        jsonGenerator.writeObjectField("playerOrder", gameState.getPlayerOrder());

        // TODO - Only include currentProcess for "see everything" version
        jsonGenerator.writeObjectField("currentProcess", gameState.getCurrentProcess());

        // TODO - Only show cardsInGame for showComplete
        jsonGenerator.writeArrayFieldStart("cardsInGame");
        for (PhysicalCard card : gameState._allCards.values())
            jsonGenerator.writeObject(card);
        jsonGenerator.writeEndArray();

        // TODO - visibleCards if !showComplete

        jsonGenerator.writeEndObject();
    }

}