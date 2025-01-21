package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
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
    public List<TopLevelSelectableAction> getPhaseActions(String playerId) {
        final GameState gameState = _game.getGameState();
        final Player player = gameState.getPlayer(playerId);
        final Phase currentPhase = gameState.getCurrentPhase();
        List<TopLevelSelectableAction> result = new LinkedList<>();
        if (currentPhase == Phase.CARD_PLAY || currentPhase == Phase.EXECUTE_ORDERS) {
            Filters.filterActive(gameState.getGame(), CardType.MISSION).forEach(
                    card -> result.addAll(card.getRulesActionsWhileInPlay(player, _game)));
            Filters.filterYourActive(_game, player, Filters.not(CardType.MISSION)).forEach(
                    card -> result.addAll(card.getRulesActionsWhileInPlay(player, _game)));
        }
        Filters.filterActive(_game).forEach(card -> result.addAll(card.getGameTextActionsWhileInPlay(player)));
        return result;
    }
}