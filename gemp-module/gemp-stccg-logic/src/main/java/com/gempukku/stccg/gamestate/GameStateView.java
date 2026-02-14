package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
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
@JsonIncludeProperties({ "requestingPlayer", "currentPhase", "phasesInOrder", "players", "playerMap", "playerOrder", "visibleCardsInGame",
        "versionNumber",
        "spacelineLocations", "awayTeams", "lastAction", "performedActions", "playerClocks", "pendingDecision", "spacelineElements", "gameLocations"})
@JsonPropertyOrder({ "requestingPlayer", "currentPhase", "phasesInOrder", "players", "playerMap", "playerOrder", "visibleCardsInGame", "spacelineLocations",
        "versionNumber",
        "awayTeams", "actions", "lastAction", "performedActions", "playerClocks", "pendingDecision", "spacelineElements", "gameLocations" })
public class GameStateView {

    @JsonProperty("versionNumber")
    @SuppressWarnings("unused")
    private final String VERSION_NUMBER = "1.1.0";

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
        List<MissionLocation> result = new ArrayList<>();
        if (_gameState instanceof ST1EGameState stGameState) {
            for (GameLocation location : stGameState.getOrderedSpacelineLocations()) {
                if (location instanceof MissionLocation missionLocation) {
                    result.add(missionLocation);
                }
            }
            return result;
        }
        else return null;
    }

    @JsonProperty("spacelineElements")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SpacelineIndex> getSpacelineElements() {
        if (_gameState instanceof ST1EGameState stGameState) {
            return stGameState.getSpacelineElements();
        }
        else return null;
    }

    @JsonProperty("gameLocations")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, GameLocation> getGameLocations() {
        if (_gameState instanceof ST1EGameState stGameState) {
            Map<String, GameLocation> result = new HashMap<>();
            for (GameLocation location : stGameState.getOrderedSpacelineLocations()) {
                result.put(String.valueOf(location.getLocationId()), location);
            }
            return result;
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

    @JsonProperty("playerMap")
    private Map<String, PlayerView> getPlayerMap() {
        Map<String, PlayerView> result = new HashMap<>();
        for (Player player : _gameState.getPlayers()) {
            result.put(player.getPlayerId(), new PlayerView(player, _requestingPlayerId));
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
        List<Action> result = new ArrayList<>();
        for (Action action : _gameState.getActionsEnvironment().getPerformedActions()) {
            if (action.getActionType() != ActionType.SYSTEM_QUEUE) {
                result.add(action);
            }
        }
        return result;
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