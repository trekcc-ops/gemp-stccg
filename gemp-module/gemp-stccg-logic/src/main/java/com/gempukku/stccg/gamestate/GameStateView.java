package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.JsonViews;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.game.PlayerView;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "requestingPlayer", "currentPhase", "players", "playerOrder", "visibleCardsInGame",
        "spacelineLocations", "awayTeams", "lastAction" })
@JsonPropertyOrder({ "requestingPlayer", "currentPhase", "players", "playerOrder", "visibleCardsInGame", "spacelineLocations",
        "awayTeams", "actions", "lastAction" })
@JsonView(JsonViews.Public.class)
public class GameStateView {
    @JsonProperty("requestingPlayer")
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
        if (_gameState instanceof ST1EGameState stGameState) {
            return new LinkedList<>(stGameState.getSpacelineLocations());
        }
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

    @JsonProperty("lastAction")
    private Action getLastAction() {
        return _gameState.getActionsEnvironment().getPerformedActions().getLast();
    }

}