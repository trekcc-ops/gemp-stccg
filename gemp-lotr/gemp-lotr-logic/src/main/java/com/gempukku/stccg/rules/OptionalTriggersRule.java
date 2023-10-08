package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.OptionalTriggerAction;
import com.gempukku.stccg.common.CardType;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.EffectResult;

import java.util.LinkedList;
import java.util.List;

public class OptionalTriggersRule {
    protected final DefaultActionsEnvironment actionsEnvironment;

    public OptionalTriggersRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalBeforeTriggers(String playerId, DefaultGame game, Effect effect) {
                        List<OptionalTriggerAction> result = new LinkedList<>();
                        for (PhysicalCard activatableCard : Filters.filter(game.getGameState().getInPlay(), game, getActivatableCardsFilter(playerId))) {
                            if (!game.getModifiersQuerying().hasTextRemoved(game, activatableCard)) {
                                final List<? extends OptionalTriggerAction> actions = activatableCard.getBlueprint().getOptionalBeforeTriggers(playerId, game, effect, activatableCard);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }

                        return result;
                    }

                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggerActions(String playerId, DefaultGame game, EffectResult effectResult) {
                        List<OptionalTriggerAction> result = new LinkedList<>();
                        for (PhysicalCard activatableCard : Filters.filter(game.getGameState().getInPlay(), game, getActivatableCardsFilter(playerId))) {
                            if (!game.getModifiersQuerying().hasTextRemoved(game, activatableCard)) {
                                final List<? extends OptionalTriggerAction> actions =
                                        activatableCard.getOptionalAfterTriggerActions(playerId, game, effectResult,
                                                activatableCard);
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