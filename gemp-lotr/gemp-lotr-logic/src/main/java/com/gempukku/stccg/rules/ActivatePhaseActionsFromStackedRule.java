package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class ActivatePhaseActionsFromStackedRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public ActivatePhaseActionsFromStackedRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId, DefaultGame game) {
                        List<Action> result = new LinkedList<>();
                        for (PhysicalCard activatableCard : Filters.filter(game.getGameState().getStacked(playerId), game,
                                Filters.stackedOn(Filters.active))) {
                            List<? extends Action> list = activatableCard.getBlueprint().getPhaseActionsFromStacked(playerId, game, activatableCard);
                            if (list != null)
                                result.addAll(list);

                            final List<? extends Action> extraActions = game.getModifiersQuerying().getExtraPhaseActionsFromStacked(game, activatableCard);
                            if (extraActions != null) {
                                for (Action action : extraActions) {
                                    if (action != null)
                                        result.add(action);
                                }
                            }
                        }
                        return result;
                    }
                });
    }
}
