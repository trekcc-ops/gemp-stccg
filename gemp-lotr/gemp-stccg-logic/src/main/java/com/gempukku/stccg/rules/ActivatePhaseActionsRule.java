package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActivateCardAction;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class ActivatePhaseActionsRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public ActivatePhaseActionsRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId, DefaultGame game) {
                        List<Action> result = new LinkedList<>();
                        for (PhysicalCard activatableCard : Filters.filter(game.getGameState().getAllCardsInPlay(), game, getActivatableCardsFilter(playerId))) {
                            if (!activatableCard.hasTextRemoved()) {
                                final List<? extends ActivateCardAction> actions = activatableCard.getBlueprint().getPhaseActionsInPlay(playerId, game, activatableCard);
                                if (actions != null)
                                    result.addAll(actions);

                                final List<? extends Action> extraActions = game.getModifiersQuerying().getExtraPhaseActions(game, activatableCard);
                                if (extraActions != null) {
                                    for (Action action : extraActions) {
                                        if (action != null)
                                            result.add(action);
                                    }
                                }
                            }
                        }

                        return result;
                    }
                });
    }

    private Filter getActivatableCardsFilter(String playerId) {
        return Filters.and(Filters.owner(playerId), Filters.active);
    }
}
