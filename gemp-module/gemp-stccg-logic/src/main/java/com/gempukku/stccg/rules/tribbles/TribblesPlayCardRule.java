package com.gempukku.stccg.rules.tribbles;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.TribblesGame;

import java.util.LinkedList;
import java.util.List;

public class TribblesPlayCardRule extends TribblesRule {
    public TribblesPlayCardRule(TribblesGame game) {
        super(game);
    }

    @Override
    public List<? extends Action> getPhaseActions(String playerId) {
        if (_game.getGameState().getCurrentPlayerId().equals(playerId)) {
            List<Action> result = new LinkedList<>();
            for (PhysicalCard card : Filters.filter(_game.getGameState().getHand(playerId), _game)) {
                if (card.canBePlayed()) {
                    Action action = card.getPlayCardAction();
                    if (action.canBeInitiated())
                        result.add(action);
                }
            }
            return result;
        }
        return null;
    }
}