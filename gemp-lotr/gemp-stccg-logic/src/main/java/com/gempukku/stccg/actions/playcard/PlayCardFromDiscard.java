package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.ModifierFlag;

public class PlayCardFromDiscard extends PlayCardEffectAppenderProducer {

    protected EffectAppender resolveCardsAppender(String filter, ValueSource costModifierSource,
                                                  FilterableSource onFilterableSource, ValueSource countSource,
                                                  String memorize, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {

        return CardResolver.resolveCardsInDiscard(filter,
                (actionContext) -> {
                    final DefaultGame game = actionContext.getGame();
                    final int costModifier = costModifierSource.evaluateExpression(actionContext, actionContext.getSource());
                    if (onFilterableSource != null) {
                        final Filterable onFilterable = onFilterableSource.getFilterable(actionContext);
                        return Filters.and(Filters.playable(game, costModifier), Filters.attachableTo(game, onFilterable));
                    }

                    return Filters.playable(game, costModifier);
                },
                (actionContext) -> {
                    final DefaultGame game = actionContext.getGame();
                    final int costModifier = costModifierSource.evaluateExpression(actionContext, actionContext.getSource());
                    if (onFilterableSource != null) {
                        final Filterable onFilterable = onFilterableSource.getFilterable(actionContext);
                        return Filters.and(Filters.playable(game, costModifier), Filters.attachableTo(actionContext.getGame(), onFilterable));
                    }

                    return Filters.playable(costModifier, false, false, true);
                },
                countSource, memorize, "you", "you","Choose card to play", environment);
    }

    protected boolean isAppendedEffectPlayableInFull(ActionContext actionContext) {
        return actionContext.getGame().getModifiersQuerying().hasFlagActive(ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK);
    }

}
