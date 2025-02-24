package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerClock;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.player.PlayerView;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "requestingPlayer", "currentPhase", "phasesInOrder", "players", "playerOrder", "visibleCardsInGame",
        "spacelineLocations", "awayTeams", "lastAction", "performedActions", "playerClocks", "pendingDecision" })
@JsonPropertyOrder({ "requestingPlayer", "currentPhase", "phasesInOrder", "players", "playerOrder", "visibleCardsInGame", "spacelineLocations",
        "awayTeams", "actions", "lastAction", "performedActions", "playerClocks", "pendingDecision" })
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
    private Map<String, PhysicalCard> getCardsInGame() {
        Map<String, PhysicalCard> result = new HashMap<>();
        for (PhysicalCard card : _gameState.getAllCardsInGame()) {
            if (showCardInfo(card)) {
                result.put(String.valueOf(card.getCardId()), card);
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

    @JsonProperty("phasesInOrder")
    private List<Phase> getPhasesInOrder() {
        return _gameState.getPhasesInOrder();
    }

    private boolean showCardInfo(PhysicalCard card) {
        return card.isKnownToPlayer(_requestingPlayerId);
    }

    @JsonProperty("performedActions")
    private List<Action> performedActions() {
        return _gameState.getActionsEnvironment().getPerformedActions();
    }

    @JsonProperty("playerClocks")
    private Collection<PlayerClock> playerClocks() {
        return _gameState.getPlayerClocks().values();
    }

    @JsonProperty("pendingDecision")
    private AwaitingDecision decision() {
        return _gameState.getDecision(_requestingPlayerId);
    }

}