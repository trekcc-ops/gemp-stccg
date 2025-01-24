package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.PlayerOrder;

import java.util.LinkedList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "currentPhase", "players", "spacelineLocations", "awayTeams", "playerOrder",
        "visibleCardsInGame" })
public class GameStateView {
    // Class designed to pass game state information to the client
    private final static int ANONYMOUS_CARD_ID = -99;
    private final String _playerId;
    private final boolean _showComplete;
    private final GameState _gameState;

    public GameStateView(String playerId, boolean showComplete, GameState gameState) {
        _playerId = playerId;
        _showComplete = showComplete;
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

    @JsonProperty("awayTeams")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<MissionLocation> getSpacelineLocations() {
        if (_gameState instanceof ST1EGameState stGameState)
            return stGameState.getSpacelineLocations();
        else return null;
    }

// TODO   @JsonProperty("players") // Make private cards in cardGroups anonymous

    private boolean showCardInfo(PhysicalCard card) {
        return _showComplete || card.getZone().isPublic() || card.getOwnerName().equals(_playerId) ||
                card.isControlledBy(_playerId);
    }

    private int getSecureCardId(PhysicalCard card) {
        // Need new logic for this
        if (showCardInfo(card))
            return card.getCardId();
        else return ANONYMOUS_CARD_ID;
    }
}