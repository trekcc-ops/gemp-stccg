package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprint;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.ModifierFlag;

public class PlayCardFromDrawDeck extends PlayCardEffectAppenderProducer {

    protected EffectBlueprint resolveCardsAppender(String filter, ValueSource costModifierSource,
                                                   FilterableSource onFilterableSource, ValueSource countSource,
                                                   String memorize, CardBlueprintFactory environment) throws InvalidCardDefinitionException {

        FilterableSource filterable = (actionContext) -> {
            final DefaultGame game = actionContext.getGame();
            final int costModifier = costModifierSource.evaluateExpression(actionContext);
            if (onFilterableSource != null) {
                final Filterable onFilterable = onFilterableSource.getFilterable(actionContext);
                return Filters.and(Filters.playable(costModifier), Filters.attachableTo(game, onFilterable));
            }
            return Filters.playable(costModifier, false, false, true);
        };

        PlayerSource you = ActionContext::getPerformingPlayerId;

        return CardResolver.resolveCardsInZone(filter, filterable, countSource, memorize, you, you,
                "Choose card to play", environment.getCardFilterableIfChooseOrAll(filter), Zone.DRAW_DECK,
                false,
                environment.getCardSourceFromZone(you, Zone.DRAW_DECK, filter));
    }

    @Override
    protected boolean isAppendedEffectPlayableInFull(ActionContext actionContext) {
        return !actionContext.getGame().getModifiersQuerying().hasFlagActive(ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK);
    }
}