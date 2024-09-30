package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.cards.blueprints.effectappender.EffectAppender;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class PlayCardFromHandNew extends PlayCardEffectAppenderProducer {

    protected EffectAppender resolveCardsAppender(String filter, ValueSource costModifierSource,
                                                  FilterableSource onFilterableSource, ValueSource countSource,
                                                  String memorize, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {

        PlayerSource you = PlayerResolver.resolvePlayer("you");

        FilterableSource filterableSource = (actionContext) -> {
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
        };

        return CardResolver.resolveCardsInHand(filter, filterableSource, countSource, memorize, you,
                "Choose card to play from hand", environment);
    }

    protected boolean isAppendedEffectPlayableInFull(ActionContext actionContext) {
        return true;
    }
}
