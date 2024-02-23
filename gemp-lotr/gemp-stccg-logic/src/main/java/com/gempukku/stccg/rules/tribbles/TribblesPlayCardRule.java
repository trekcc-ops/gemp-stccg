package com.gempukku.stccg.rules.tribbles;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.TribblesPhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.TribblesGame;

import java.util.LinkedList;
import java.util.List;

public class TribblesPlayCardRule {
    private final DefaultActionsEnvironment actionsEnvironment;
    private final TribblesGame _game;

    public TribblesPlayCardRule(DefaultActionsEnvironment actionsEnvironment, TribblesGame game) {
        this.actionsEnvironment = actionsEnvironment;
        _game = game;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId) {
                        if (_game.getGameState().getCurrentPlayerId().equals(playerId)) {
                            List<Action> result = new LinkedList<>();
                            for (PhysicalCard card : Filters.filter(_game.getGameState().getHand(playerId), _game)) {
                                if (card.canBePlayed()) {
                                    result.add(((TribblesPhysicalCard) card).createPlayCardAction());
                                }
                            }
                            return result;
                        }
                        return null;
                    }
                }
        );
    }
}
