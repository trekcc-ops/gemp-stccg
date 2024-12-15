package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameState;

import java.util.LinkedList;
import java.util.List;

public class ST1EPhaseActionsRule extends ST1ERule {

    public ST1EPhaseActionsRule(ST1EGame game) {
        super(game);
    }

    @Override
    public List<? extends Action> getPhaseActions(String playerId) {
        final GameState gameState = _game.getGameState();
        final Player player = gameState.getPlayer(playerId);
        final Phase currentPhase = gameState.getCurrentPhase();
        List<Action> result = new LinkedList<>();
        if (currentPhase == Phase.CARD_PLAY || currentPhase == Phase.EXECUTE_ORDERS) {
            Filters.filterActive(gameState.getGame(), CardType.MISSION).forEach(
                    card -> result.addAll(card.getRulesActionsWhileInPlay(player)));
            Filters.filterYourActive(player, Filters.not(CardType.MISSION)).forEach(
                    card -> result.addAll(card.getRulesActionsWhileInPlay(player)));
        } else
            Filters.filterActive(_game).forEach(card -> result.addAll(card.getGameTextActionsWhileInPlay(player)));
        return result;
    }
}