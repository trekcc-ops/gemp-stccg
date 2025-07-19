package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.battle.InitiateShipBattleAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ST1EPhaseActionsRule extends ST1ERule {

    public ST1EPhaseActionsRule(ST1EGame game) {
        super(game);
    }

    @Override
    public List<TopLevelSelectableAction> getPhaseActions(Player player) {
        List<TopLevelSelectableAction> result = new LinkedList<>();
        final GameState gameState = _game.getGameState();
        final Phase currentPhase = gameState.getCurrentPhase();
        if (currentPhase == Phase.CARD_PLAY || currentPhase == Phase.EXECUTE_ORDERS) {
            Filters.filterActive(_game, CardType.MISSION).forEach(
                    card -> result.addAll(card.getRulesActionsWhileInPlay(player, _game)));
            Filters.filterYourActive(_game, player, Filters.not(CardType.MISSION)).forEach(
                    card -> result.addAll(card.getRulesActionsWhileInPlay(player, _game)));
        }

        if (currentPhase == Phase.EXECUTE_ORDERS) {
            Map<PhysicalCard, Map<String, List<PhysicalCard>>> shipBattleTargets =
                    ShipBattleRules.getTargetsForShipBattleInitiation(_game, player);
            if (!shipBattleTargets.isEmpty()) {
                result.add(new InitiateShipBattleAction(shipBattleTargets, _game, player));
            }
        }

        Filters.filterActive(_game).forEach(card -> result.addAll(card.getGameTextActionsWhileInPlay(player)));
        return result;
    }

}