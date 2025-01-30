package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.discard.DiscardCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.evaluator.ConstantValueSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.FilterFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class DiscardActionBlueprint extends MultiSubActionBlueprint {

    private final String _memoryId;
    private static final String SELECTION_TEXT = "Choose cards to discard";
    private final ValueSource _countSource;

    @JsonCreator
    public DiscardActionBlueprint(@JsonProperty(value = "count")
                           ValueSource count,
                           @JsonProperty(value = "filter", required = true)
                           String filter,
                           @JsonProperty(value = "memorize")
                           String memoryId) throws InvalidCardDefinitionException {
        _memoryId = Objects.requireNonNullElse(memoryId, "_temp");
        _countSource = Objects.requireNonNullElse(count, new ConstantValueSource(1));
        final PlayerSource selectingPlayer = ActionContext::getPerformingPlayerId;
        final PlayerSource targetPlayerSource = ActionContext::getPerformingPlayerId;

        FilterableSource cardFilter = (filter.startsWith("all(") || filter.startsWith("choose(")) ?
                new FilterFactory().generateFilter(filter.substring(filter.indexOf("(") + 1, filter.lastIndexOf(")"))) :
                null;

        FilterableSource choiceFilter = (actionContext) ->
                Filters.canBeDiscarded(actionContext.getPerformingPlayerId(), actionContext.getSource());

        Function<ActionContext, List<PhysicalCard>> cardSource = getCardSource(filter);


        addEffectBlueprint(CardResolver.resolveCardsInPlay(filter, cardFilter, choiceFilter, choiceFilter,
                _countSource, _memoryId, selectingPlayer, SELECTION_TEXT, cardSource));

        addEffectBlueprint(
                new DelayedEffectBlueprint() {
                    @Override
                    protected List<Action> createActions(CardPerformedAction parentAction, ActionContext context)
                            throws PlayerNotFoundException {

                        final Collection<PhysicalCard> cardsFromMemory = context.getCardsFromMemory(_memoryId);
                        String targetPlayerId = targetPlayerSource.getPlayerId(context);
                        Player targetPlayer = context.getGame().getPlayer(targetPlayerId);
                        List<Action> subActions = new LinkedList<>();
                        cardsFromMemory.forEach(card ->
                                subActions.add(new DiscardCardAction(context.getSource(), targetPlayer, card)));
                        return subActions;
                    }
                });
    }

    private static Function<ActionContext, List<PhysicalCard>> getCardSource(String type) {
        final String sourceMemory = type.startsWith("memory(") ?
                type.substring(type.indexOf("(") + 1, type.lastIndexOf(")")) : null;
        return actionContext -> Filters.filterActive(actionContext.getGame(), sourceMemory == null ? Filters.any :
                actionContext.getCardFromMemory(sourceMemory)).stream().toList();
    }
}