package com.gempukku.stccg.cards.blueprints.resolver;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.ChooseActiveCardsEffect;
import com.gempukku.stccg.actions.choose.ChooseArbitraryCardsEffect;
import com.gempukku.stccg.actions.choose.ChooseCardsFromZoneEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.effect.DelayedEffectBlueprint;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class CardResolver {

    public static EffectBlueprint resolveCardsInZone(String type, FilterableSource choiceFilter,
                                                     ValueSource countSource,
                                                     String memory, PlayerSource selectingPlayer,
                                                     PlayerSource targetPlayer, String choiceText,
                                                     FilterableSource typeFilter, Zone zone, boolean showMatchingOnly,
                                                     Function<ActionContext, List<PhysicalCard>> cardSource) {

        String selectionType = (type.contains("(")) ? type.substring(0,type.indexOf("(")) : type;

        return switch (selectionType) {
            case "self", "memory", "all", "random" ->
                    finalTargetAppender(choiceFilter, choiceFilter, countSource, memory, cardSource, selectingPlayer,
                            selectionType, typeFilter);
            case "choose" -> resolveChoiceCards(typeFilter, choiceFilter, choiceFilter, countSource, cardSource,
                    createChoiceEffectSourceFromZone(selectingPlayer, targetPlayer, zone, memory, choiceText,
                            showMatchingOnly, cardSource));
            default -> throw new RuntimeException("Unable to resolve card resolver of type: " + selectionType);
        };
    }



    public static EffectBlueprint resolveCardsInPlay(String type, ValueSource countSource, String memory,
                                                     PlayerSource choicePlayer, String choiceText,
                                                     FilterableSource typeFilter) {
        final String sourceMemory =
                type.startsWith("memory(") ? type.substring(type.indexOf("(") + 1, type.lastIndexOf(")")) : null;
        Function<ActionContext, List<PhysicalCard>> cardSource =
                actionContext -> Filters.filterActive(actionContext.getGame(), sourceMemory == null ? Filters.any :
                        actionContext.getCardFromMemory(sourceMemory)).stream().toList();
        return resolveCardsInPlay(type, typeFilter, null, null, countSource, memory,
                choicePlayer, choiceText, cardSource);
    }


    public static EffectBlueprint resolveCardsInPlay(String type, FilterableSource typeFilter,
                                                     FilterableSource choiceFilter, FilterableSource playabilityFilter,
                                                     ValueSource countSource, String memory, PlayerSource choicePlayer,
                                                     String choiceText,
                                                     Function<ActionContext, List<PhysicalCard>> cardSource) {

        String selectionType = (type.contains("(")) ? type.substring(0,type.indexOf("(")) : type;

        return switch (selectionType) {
            case "self", "memory", "all", "random" ->
                    finalTargetAppender(choiceFilter, playabilityFilter, countSource, memory, cardSource, choicePlayer,
                            selectionType, typeFilter);
            case "choose" -> resolveChoiceCardsWithEffect(typeFilter, playabilityFilter, countSource, cardSource,
                    getChoiceEffectFromInPlay(choiceText, countSource, memory, choicePlayer, cardSource, typeFilter, choiceFilter));
            default -> throw new RuntimeException("Unable to resolve card resolver of type: " + selectionType);
        };
    }


    private static Function<ActionContext, Effect> getChoiceEffectFromInPlay(String choiceText, ValueSource countSource,
                                               String memory, PlayerSource choicePlayer,
                                                                             Function<ActionContext, List<PhysicalCard>> cardSource,
                                                                             FilterableSource typeFilter, FilterableSource choiceFilter) {
        return (actionContext) -> {
            List<PhysicalCard> possibleCards = (List<PhysicalCard>) Filters.filter(cardSource.apply(actionContext),
                    typeFilter.getFilterable(actionContext),
                    choiceFilter == null ? Filters.any : choiceFilter.getFilterable(actionContext));
            return new ChooseActiveCardsEffect(actionContext, choicePlayer.getPlayerId(actionContext),
                    actionContext.substituteText(choiceText),
                    countSource.getMinimum(actionContext), countSource.getMaximum(actionContext),
                    possibleCards) {

                @Override
                protected void cardsSelected(Collection<PhysicalCard> cards) {
                    actionContext.setCardMemory(memory, cards);
                }
            };
        };
    }




    private static ChoiceEffectSource createChoiceEffectSourceFromZone(PlayerSource selectingPlayer,
                                                                       PlayerSource targetPlayer, Zone zone,
                                                                       String memory, String choiceText,
                                                                       boolean showMatchingOnly,
                                                                       Function<ActionContext, List<PhysicalCard>> cardSource) {
        return (possibleCards, action, actionContext, min, max) -> {
            String choicePlayerId = selectingPlayer.getPlayerId(actionContext);
            String targetPlayerId = targetPlayer.getPlayerId(actionContext);
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
                return new ChooseArbitraryCardsEffect(actionContext.getGame().getPlayer(choicePlayerId),
                        actionContext.substituteText(choiceText),
                        cardSource.apply(actionContext), Filters.in(possibleCards),
                        min, max, showMatchingOnly) {
                    @Override
                    protected void cardsSelected(Collection<PhysicalCard> selectedCards) {
                        actionContext.setCardMemory(memory, selectedCards);
                    }
                };
            }
        };
    }


    private static DelayedEffectBlueprint finalTargetAppender(FilterableSource choiceFilter, FilterableSource playabilityFilter,
                                                              ValueSource countSource, String memory,
                                                              Function<ActionContext, List<PhysicalCard>> cardSource,
                                                              PlayerSource choicePlayer, String selectionType, FilterableSource typeFilter) {

        return new DelayedEffectBlueprint() {
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
            protected List<Action> createActions(Action action, ActionContext context) {
                List<Action> actions = new LinkedList<>();

                Effect effect = switch (selectionType) {
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
                                    TextUtils.getRandomItemsFromList(filterCards(context, playabilityFilter),
                                            countSource.evaluateExpression(context)));
                        }
                    };
                    default -> null;
                };
                if (effect != null) {
                    actions.add(new SubAction(action, effect));
                }
                return actions;
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


    private static DelayedEffectBlueprint resolveChoiceCards(FilterableSource typeFilter, FilterableSource choiceFilter,
                                                             FilterableSource playabilityFilter,
                                                             ValueSource countSource,
                                                             Function<ActionContext, List<PhysicalCard>> cardSource,
                                                             ChoiceEffectSource effectSource) {

        return new DelayedEffectBlueprint() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                int min = countSource.getMinimum(actionContext);
                return filterCards(actionContext, playabilityFilter).size() >= min;
            }

            @Override
            protected List<Action> createActions(Action action, ActionContext context) {
                List<Action> result = new LinkedList<>();
                Collection<PhysicalCard> cards = filterCards(context, choiceFilter);
                Effect effect = effectSource.createEffect(cards, action, context,
                        countSource.getMinimum(context), countSource.getMaximum(context));
                result.add(new SubAction(action, effect));
                return result;
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

    private static DelayedEffectBlueprint resolveChoiceCardsWithEffect(FilterableSource typeFilter,
                                                                       FilterableSource playabilityFilter,
                                                                       ValueSource countSource,
                                                                       Function<ActionContext, List<PhysicalCard>> cardSource,
                                                                       Function<ActionContext, Effect> choiceEffect) {
        return new DelayedEffectBlueprint() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                int min = countSource.getMinimum(actionContext);
                return filterCards(actionContext, playabilityFilter).size() >= min;
            }

            @Override
            protected List<Action> createActions(Action action, ActionContext context) {
                List<Action> result = new LinkedList<>();
                Effect effect = choiceEffect.apply(context);
                result.add(new SubAction(action, effect));
                return result;
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
        Effect createEffect(Collection<? extends PhysicalCard> possibleCards, Action action,
                            ActionContext actionContext, int min, int max);
    }
}