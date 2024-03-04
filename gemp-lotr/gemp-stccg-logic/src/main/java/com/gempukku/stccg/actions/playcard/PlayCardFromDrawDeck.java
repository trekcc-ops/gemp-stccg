package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.ModifierFlag;

public class PlayCardFromDrawDeck extends PlayCardEffectAppenderProducer {

    protected EffectAppender resolveCardsAppender(String filter, ValueSource costModifierSource,
                                                  FilterableSource onFilterableSource, ValueSource countSource,
                                                  String memorize, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        PlayerSource you = PlayerResolver.resolvePlayer("you");
        return CardResolver.resolveCardsInZone(filter,
                (actionContext) -> {
                    final DefaultGame game = actionContext.getGame();
                    final int costModifier = costModifierSource.evaluateExpression(actionContext);
                    if (onFilterableSource != null) {
                        final Filterable onFilterable = onFilterableSource.getFilterable(actionContext);
                        return Filters.and(Filters.playable(game, costModifier), Filters.attachableTo(game, onFilterable));
                    }
                    return Filters.playable(costModifier, false, false, true);
                },
                countSource, memorize, you, you, "Choose card to play", environment, Zone.DRAW_DECK);
    }

    @Override
    protected boolean isAppendedEffectPlayableInFull(ActionContext actionContext) {
        return !actionContext.getGame().getModifiersQuerying().hasFlagActive(ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK);
    }
}
