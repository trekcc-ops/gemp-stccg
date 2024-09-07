package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class PlayCardFromHandNew extends PlayCardEffectAppenderProducer {

    protected EffectAppender resolveCardsAppender(String filter, ValueSource costModifierSource,
                                                  FilterableSource onFilterableSource, ValueSource countSource,
                                                  String memorize, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        return CardResolver.resolveCardsInHandNEw(filter,
                (actionContext) -> {
                    final DefaultGame game = actionContext.getGame();
                    final int costModifier = costModifierSource.evaluateExpression(actionContext);
                    if (onFilterableSource != null) {
                        final Filterable onFilterable = onFilterableSource.getFilterable(actionContext);
                        return Filters.and(
                                Filters.playable(game, costModifier),
                                Filters.attachableTo(game, onFilterable)
                        );
                    }
                    return Filters.playable(game, costModifier);
                },
                countSource, memorize, "you", "you",
                "Choose card to play from hand", false, environment);
    }

    protected boolean isAppendedEffectPlayableInFull(ActionContext actionContext) {
        return true;
    }
}
