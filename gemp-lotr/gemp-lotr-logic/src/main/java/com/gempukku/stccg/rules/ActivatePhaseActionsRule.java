package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.common.CardType;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.ActivateCardAction;
import com.gempukku.stccg.actions.Action;

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
                        for (PhysicalCard activatableCard : Filters.filter(game.getGameState().getInPlay(), game, getActivatableCardsFilter(playerId))) {
                            if (!game.getModifiersQuerying().hasTextRemoved(game, activatableCard)) {
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
        return Filters.or(
                Filters.and(CardType.SITE,
                        (Filter) (game, physicalCard) -> {
                            if (game.getGameState().getCurrentPhase().isRealPhase())
                                return Filters.currentSite.accepts(game, physicalCard);
                            return false;
                        }),
                Filters.and(Filters.not(CardType.SITE), Filters.owner(playerId), Filters.active));
    }
}
