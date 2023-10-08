package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.CardType;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.ActivateCardAction;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.EffectResult;

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
                        for (PhysicalCard activatableCard : Filters.filter(game.getGameState().getInPlay(), game, getActivatableCardsFilter(playerId))) {
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
                        for (PhysicalCard activatableCard : Filters.filter(game.getGameState().getInPlay(), game, getActivatableCardsFilter(playerId))) {
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