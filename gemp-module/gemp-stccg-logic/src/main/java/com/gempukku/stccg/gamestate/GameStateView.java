package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.game.PlayerView;

import java.util.LinkedList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "currentPhase", "players", "playerOrder", "visibleCardsInGame", "spacelineLocations",
        "awayTeams" })
@JsonPropertyOrder({ "currentPhase", "players", "playerOrder", "visibleCardsInGame", "spacelineLocations",
        "awayTeams" })
public class GameStateView {
    private final String _requestingPlayerId;
    private final GameState _gameState;

    public GameStateView(String playerId, GameState gameState) {
        _requestingPlayerId = playerId;
        _gameState = gameState;
    }

    @JsonProperty("currentPhase")
    private Phase getCurrentPhase() {
        return _gameState.getCurrentPhase();
    }

    @JsonProperty("playerOrder")
    private PlayerOrder getPlayerOrder() {
        return _gameState.getPlayerOrder();
    }

    @JsonProperty("visibleCardsInGame")
    private List<PhysicalCard> getCardsInGame() {
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard card : _gameState.getAllCardsInGame()) {
            if (showCardInfo(card)) {
                result.add(card);
            }
        }
        return result;
    }

    @JsonProperty("awayTeams")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<AwayTeam> getAwayTeams() {
        if (_gameState instanceof ST1EGameState stGameState)
            return stGameState.getAwayTeams();
        else return null;
    }

    @JsonProperty("spacelineLocations")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<MissionLocation> getSpacelineLocations() {
        if (_gameState instanceof ST1EGameState stGameState)
            return stGameState.getSpacelineLocations();
        else return null;
    }

    @JsonProperty("players")
    private List<PlayerView> getPlayers() {
        List<PlayerView> result = new LinkedList<>();
        for (Player player : _gameState.getPlayers()) {
            result.add(new PlayerView(player, _requestingPlayerId));
        }
        return result;
    }

    private boolean showCardInfo(PhysicalCard card) {
        return card.getZone().isPublic() || card.getOwnerName().equals(_requestingPlayerId) || card.isControlledBy(_requestingPlayerId);
    }

}