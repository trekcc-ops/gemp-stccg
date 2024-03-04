package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.RequiredType;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class OptionalTriggersRule {
    protected final ActionsEnvironment _actionsEnvironment;
    private final DefaultGame _game;

    public OptionalTriggersRule(ActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
        _game = actionsEnvironment.getGame();
    }

    public void applyRule() {
        _actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getOptionalBeforeTriggerActions(String playerId, Effect effect) {
                        Player player = _game.getGameState().getPlayer(playerId);
                        List<Action> result = new LinkedList<>();
                        for (PhysicalCard card : Filters.filterYourActive(player)) {
                            if (!card.hasTextRemoved()) {
                                final List<? extends Action> actions =
                                        card.getBeforeTriggerActions(playerId, effect, RequiredType.OPTIONAL);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }

                        return result;
                    }

                }
        );
    }

}