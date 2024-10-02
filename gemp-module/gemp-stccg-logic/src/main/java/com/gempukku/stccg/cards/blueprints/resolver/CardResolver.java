package com.gempukku.stccg.cards.blueprints.resolver;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.actions.choose.ChooseActiveCardsEffect;
import com.gempukku.stccg.actions.choose.ChooseArbitraryCardsEffect;
import com.gempukku.stccg.actions.choose.ChooseCardsFromZoneEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.blueprints.effectappender.DefaultDelayedAppender;
import com.gempukku.stccg.cards.blueprints.effectappender.EffectAppender;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.TextUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class CardResolver {

    public static EffectAppender resolveCardsInHand(String type, FilterableSource additionalFilter,
                                                    ValueSource countSource, String memory, PlayerSource handSource,
                                                    String choiceText, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        FilterableSource typeFilter = environment.getCardFilterableIfChooseOrAll(type);
        return resolveCardsInZone(type, additionalFilter, additionalFilter, countSource, memory, handSource, handSource, choiceText,
                typeFilter, Zone.HAND, false);
    }

    public static EffectAppender resolveCardsInDiscard(String type, FilterableSource choiceFilter,
                                                       FilterableSource playabilityFilter, ValueSource countSource,
                                                       String memory, PlayerSource choicePlayerSource,
                                                       PlayerSource targetPlayerSource,
                                                       String choiceText, FilterableSource typeFilter)
            throws InvalidCardDefinitionException {
        return resolveCardsInZone(type, choiceFilter, playabilityFilter, countSource, memory, choicePlayerSource,
                targetPlayerSource, choiceText, typeFilter, Zone.DISCARD, false);
    }


    public static EffectAppender resolveCardsInZone(String type, ValueSource countSource, String memory,
                                                    PlayerSource selectingPlayer, PlayerSource targetPlayer,
                                                    String choiceText, Zone zone,
                                                    FilterableSource typeFilter, boolean showMatchingOnly)
            throws InvalidCardDefinitionException {
        return resolveCardsInZone(type, null, null, countSource, memory, selectingPlayer,
                targetPlayer, choiceText, typeFilter, zone, showMatchingOnly);
    }




    public static EffectAppender resolveCardsInZone(String type, FilterableSource choiceFilter,
                                                    FilterableSource playabilityFilter, ValueSource countSource,
                                                    String memory, PlayerSource choicePlayer,
                                                    PlayerSource targetPlayerSource, String choiceText,
                                                    FilterableSource typeFilter, Zone zone, boolean showMatchingOnly)
            throws InvalidCardDefinitionException {

        String sourceMemory = null;
        if (type.startsWith("memory("))
            sourceMemory = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
        Function<ActionContext, List<PhysicalCard>> cardSource = getCardSourceFromZone(targetPlayerSource, zone, sourceMemory);

        String selectionType = (type.contains("(")) ? type.substring(0,type.indexOf("(")) : type;

        return switch (selectionType) {
            case "self", "memory", "all", "random" ->
                    finalTargetAppender(choiceFilter, playabilityFilter, countSource, memory, cardSource, choicePlayer,
                            selectionType, typeFilter);
            case "choose" -> resolveChoiceCards(typeFilter, choiceFilter, playabilityFilter, countSource, cardSource,
                    createChoiceEffectSourceFromZone(choicePlayer, targetPlayerSource, zone, memory, choiceText,
                            showMatchingOnly));
            default -> throw new RuntimeException("Unable to resolve card resolver of type: " + selectionType);
        };
    }

    public static EffectAppender resolveCardsInZone(String type, FilterableSource choiceFilter, ValueSource countSource,
                                                    String memory, PlayerSource choicePlayerSource,
                                                    PlayerSource targetPlayerSource, String choiceText,
                                                    CardBlueprintFactory environment, Zone zone)
            throws InvalidCardDefinitionException {
        FilterableSource typeFilter = environment.getCardFilterableIfChooseOrAll(type);
        return resolveCardsInZone(type, choiceFilter, choiceFilter, countSource, memory, choicePlayerSource,
                targetPlayerSource, choiceText, typeFilter, zone, false);
    }


    public static EffectAppender resolveCardInPlay(String type, FilterableSource additionalFilter, String memory,
                                                   PlayerSource choicePlayer, String choiceText,
                                                   FilterableSource typeFilter) throws InvalidCardDefinitionException {
        Function<ActionContext, List<PhysicalCard>> cardSource =
                actionContext -> Filters.filterActive(actionContext.getGame(), Filters.any).stream().toList();
        return resolveCardsInPlay(type, typeFilter, additionalFilter, additionalFilter, new ConstantValueSource(1),
                memory, choicePlayer, choiceText, cardSource);
    }


    public static EffectAppender resolveCardsInPlay(String type, ValueSource countSource, String memory,
                                                    PlayerSource choicePlayer, String choiceText,
                                                    FilterableSource typeFilter)
            throws InvalidCardDefinitionException {

        final String sourceMemory =
                type.startsWith("memory(") ? type.substring(type.indexOf("(") + 1, type.lastIndexOf(")")) : null;
        Function<ActionContext, List<PhysicalCard>> cardSource =
                actionContext -> Filters.filterActive(actionContext.getGame(), sourceMemory == null ? Filters.any :
                        actionContext.getCardFromMemory(sourceMemory)).stream().toList();
        return resolveCardsInPlay(type, typeFilter, null, null, countSource, memory,
                choicePlayer, choiceText, cardSource);
    }


    // Only one filter provided
    public static EffectAppender resolveCardsInPlay(String type, FilterableSource additionalFilter, ValueSource countSource,
                                                    String memory, PlayerSource choicePlayer, String choiceText,
                                                    FilterableSource typeFilter,
                                                    Function<ActionContext, List<PhysicalCard>> cardSource)
            throws InvalidCardDefinitionException {
        return resolveCardsInPlay(type, typeFilter, additionalFilter, additionalFilter, countSource, memory, choicePlayer,
                choiceText, cardSource);
    }


    public static EffectAppender resolveCardsInPlay(String type, FilterableSource typeFilter, FilterableSource choiceFilter,
                                                    FilterableSource playabilityFilter, ValueSource countSource,
                                                    String memory, PlayerSource choicePlayer, String choiceText,
                                                    Function<ActionContext, List<PhysicalCard>> cardSource)
            throws InvalidCardDefinitionException {

        String selectionType = (type.contains("(")) ? type.substring(0,type.indexOf("(")) : type;

        return switch (selectionType) {
            case "self", "memory", "all", "random" ->
                    finalTargetAppender(choiceFilter, playabilityFilter, countSource, memory, cardSource, choicePlayer,
                            selectionType, typeFilter);
            case "choose" -> resolveChoiceCards(typeFilter, choiceFilter, playabilityFilter, countSource, cardSource,
                    createChoiceEffectSourceFromInPlay(choicePlayer, memory, choiceText));
            default -> throw new RuntimeException("Unable to resolve card resolver of type: " + selectionType);
        };
    }

    private static ChoiceEffectSource createChoiceEffectSourceFromInPlay(PlayerSource choicePlayerSource,
                                                                         String memory, String choiceText) {
        return (possibleCards, action, actionContext, min, max) -> {
            String choicePlayerId = choicePlayerSource.getPlayerId(actionContext);
            return new ChooseActiveCardsEffect(actionContext, choicePlayerId,
                    actionContext.substituteText(choiceText), min, max, Filters.in(possibleCards)) {
                @Override
                protected void cardsSelected(Collection<PhysicalCard> cards) {
                    actionContext.setCardMemory(memory, cards);
                }
            };
        };
    }



    private static ChoiceEffectSource createChoiceEffectSourceFromZone(PlayerSource choicePlayerSource, PlayerSource targetPlayerSource, Zone zone,
                                                                       String memory, String choiceText, boolean showMatchingOnly) {
        return (possibleCards, action, actionContext, min, max) -> {
            String choicePlayerId = choicePlayerSource.getPlayerId(actionContext);
            String targetPlayerId = targetPlayerSource.getPlayerId(actionContext);
            if (targetPlayerId.equals(choicePlayerId)) {
                return new ChooseCardsFromZoneEffect(actionContext.getGame(), zone, choicePlayerId,
                        targetPlayerId, min, max, Filters.in(possibleCards)) {
                    @Override
                    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards) {
                        actionContext.setCardMemory(memory, cards);
                    }

                    @Override
                    public String getText() {
                        return actionContext.substituteText(choiceText);
                    }
                };
            } else {
                return new ChooseArbitraryCardsEffect(actionContext.getGame(), choicePlayerId,
                        actionContext.substituteText(choiceText),
                        actionContext.getGameState().getHand(targetPlayerId), Filters.in(possibleCards),
                        min, max, showMatchingOnly) {
                    @Override
                    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> selectedCards) {
                        actionContext.setCardMemory(memory, selectedCards);
                    }
                };
            }
        };
    }

    private static Function<ActionContext, List<PhysicalCard>> getCardSourceFromZone(PlayerSource player, Zone zone, String sourceMemory)
            throws InvalidCardDefinitionException {
        return switch (zone) {
            case HAND -> actionContext -> Filters.filter(actionContext.getGameState().getHand(player.getPlayerId(actionContext)),
                    sourceMemory == null ? Filters.any : Filters.in(actionContext.getCardsFromMemory(sourceMemory))).stream().toList();
            case DISCARD -> actionContext -> Filters.filter(actionContext.getGameState().getDiscard(player.getPlayerId(actionContext)),
                    sourceMemory == null ? Filters.any : Filters.in(actionContext.getCardsFromMemory(sourceMemory))).stream().toList();
            case DRAW_DECK -> actionContext -> Filters.filter(actionContext.getGameState().getDrawDeck(player.getPlayerId(actionContext)),
                    sourceMemory == null ? Filters.any : Filters.in(actionContext.getCardsFromMemory(sourceMemory))).stream().toList();
            default -> throw new InvalidCardDefinitionException(
                    "getCardSource function not defined for zone " + zone.getHumanReadable());
        };
    }


    private static DefaultDelayedAppender finalTargetAppender(FilterableSource choiceFilter, FilterableSource playabilityFilter,
                                                      ValueSource countSource, String memory,
                                                              Function<ActionContext, List<PhysicalCard>> cardSource,
                                                      PlayerSource choicePlayer, String selectionType, FilterableSource typeFilter) {

        // TODO - "choose" not fully implemented in this method

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                switch(selectionType) {
                    case "self", "random", "choose":
                        return filterCards(actionContext, playabilityFilter).size() >= countSource.getMinimum(actionContext);
                    case "memory":
                        if (playabilityFilter != null) {
                            return filterCards(actionContext, playabilityFilter).size() >= countSource.getMinimum(actionContext);
                        } else {
                            return true;
                        }
                    case "all":
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {

                return switch (selectionType) {
                    case "self", "memory" -> {
                        Collection<PhysicalCard> result = filterCards(context, choiceFilter);
                        yield new DefaultEffect(context.getGame(), choicePlayer.getPlayerId(context)) {
                            @Override
                            public boolean isPlayableInFull() {
                                int min = countSource.getMinimum(context);
                                return result.size() >= min;
                            }

                            @Override
                            protected FullEffectResult playEffectReturningResult() {
                                context.setCardMemory(memory, result);
                                int min = countSource.getMinimum(context);
                                if (result.size() >= min) {
                                    return new FullEffectResult(true);
                                } else {
                                    return new FullEffectResult(false);
                                }
                            }
                        };
                    }
                    case "all" -> new UnrespondableEffect(context) {
                        @Override
                        protected void doPlayEffect() {
                            context.setCardMemory(memory, filterCards(context, choiceFilter));
                        }
                    };
                    case "random" -> new UnrespondableEffect(context) {
                        @Override
                        protected void doPlayEffect() {
                            context.setCardMemory(memory,
                                    TextUtils.getRandomFromList(filterCards(context, playabilityFilter),
                                            countSource.evaluateExpression(context)));
                        }
                    };
                    default -> null;
                };
            }

            private Collection<PhysicalCard> filterCards(ActionContext actionContext, FilterableSource filter) {
                Filterable additionalFilterable = (filter == null) ? Filters.any : filter.getFilterable(actionContext);
                return switch (selectionType) {
                    case "self" ->
                            Filters.filter(cardSource.apply(actionContext), actionContext.getSource(), additionalFilterable);
                    case "memory" ->
                            Filters.filter(cardSource.apply(actionContext), additionalFilterable);
                    case "all" ->
                            Filters.filter(cardSource.apply(actionContext), typeFilter.getFilterable(actionContext), additionalFilterable);
                    case "random" -> Filters.filter(cardSource.apply(actionContext));
                    case "choose" -> Filters.filter(cardSource.apply(actionContext), actionContext.getGame(),
                            typeFilter.getFilterable(actionContext), additionalFilterable);

                    default -> new LinkedList<>();
                };
            }
        };
    }


    private static DefaultDelayedAppender resolveChoiceCards(FilterableSource typeFilter, FilterableSource choiceFilter,
                                                             FilterableSource playabilityFilter,
                                                             ValueSource countSource,
                                                             Function<ActionContext, List<PhysicalCard>> cardSource,
                                                             ChoiceEffectSource effectSource) {

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                int min = countSource.getMinimum(actionContext);
                return filterCards(actionContext, playabilityFilter).size() >= min;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                int min = countSource.getMinimum(context);
                int max = countSource.getMaximum(context);
                Collection<PhysicalCard> cards = filterCards(context, choiceFilter);
                return effectSource.createEffect(cards, action, context, min, max);
            }

            private Collection<PhysicalCard> filterCards(ActionContext actionContext, FilterableSource filter) {
                Filterable filterable = typeFilter.getFilterable(actionContext);
                Filterable additionalFilterable = Filters.any;
                if (filter != null)
                    additionalFilterable = filter.getFilterable(actionContext);
                return Filters.filter(cardSource.apply(actionContext), actionContext.getGame(), filterable,
                        additionalFilterable);
            }
        };
    }


    private interface ChoiceEffectSource {
        Effect createEffect(Collection<? extends PhysicalCard> possibleCards, CostToEffectAction action, ActionContext actionContext,
                            int min, int max);
    }
}
