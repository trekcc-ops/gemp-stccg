package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActivateCardAction;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.results.EffectResult;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class ActivateResponseAbilitiesRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public ActivateResponseAbilitiesRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getOptionalBeforeActions(String playerId, DefaultGame game, Effect effect) {
                        List<Action> result = new LinkedList<>();
                        for (PhysicalCard activatableCard : Filters.filter(game.getGameState().getAllCardsInPlay(), game, getActivatableCardsFilter(playerId))) {
                            if (!game.getModifiersQuerying().hasTextRemoved(game, activatableCard)) {
                                final List<? extends ActivateCardAction> actions = activatableCard.getBlueprint().getOptionalInPlayBeforeActions(playerId, game, effect, activatableCard);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }

                        return result;
                    }

                    @Override
                    public List<? extends Action> getOptionalAfterActions(String playerId, DefaultGame game, EffectResult effectResult) {
                        List<Action> result = new LinkedList<>();
                        for (PhysicalCard activatableCard : Filters.filter(game.getGameState().getAllCardsInPlay(), game, getActivatableCardsFilter(playerId))) {
                            if (!game.getModifiersQuerying().hasTextRemoved(game, activatableCard)) {
                                final List<? extends ActivateCardAction> actions = activatableCard.getBlueprint().getOptionalInPlayAfterActions(playerId, game, effectResult, activatableCard);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }

                        return result;
                    }
                }
        );
    }

    private Filter getActivatableCardsFilter(String playerId) {
        return Filters.and(Filters.owner(playerId), Filters.active);
    }
}