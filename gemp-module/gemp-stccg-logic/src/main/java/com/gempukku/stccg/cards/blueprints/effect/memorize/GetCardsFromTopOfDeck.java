package com.gempukku.stccg.cards.blueprints.effect.memorize;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.effect.DefaultDelayedAppender;
import com.gempukku.stccg.cards.blueprints.effect.EffectAppenderProducer;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class GetCardsFromTopOfDeck implements EffectAppenderProducer {
    @Override
    public EffectBlueprint createEffectAppender(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "filter", "memorize");

        final FilterableSource filterableSource = BlueprintUtils.getFilterable(node);
        final String memorize = node.get("memorize").textValue();

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                DefaultGame game = context.getGame();
                final Filterable filterable = filterableSource.getFilterable(context);
                final Filter acceptFilter = Filters.and(filterable);
                return new UnrespondableEffect(context) {
                    @Override
                    protected void doPlayEffect() {
                        final List<? extends PhysicalCard> deck =
                                game.getGameState().getDrawDeck(context.getPerformingPlayerId());
                        List<PhysicalCard> result = new LinkedList<>();
                        for (PhysicalCard physicalCard : deck) {
                            if (acceptFilter.accepts(game, physicalCard))
                                result.add(physicalCard);
                            else
                                break;
                        }

                        context.setCardMemory(memorize, result);
                    }
                };
            }
        };
    }
}