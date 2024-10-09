package com.gempukku.stccg.effectappender.resolver;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.actions.choose.ChooseActiveCardsEffect;
import com.gempukku.stccg.actions.choose.ChooseArbitraryCardsEffect;
import com.gempukku.stccg.actions.choose.ChooseCardsFromZoneEffect;
import com.gempukku.stccg.actions.choose.ChooseStackedCardsEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.DefaultDelayedAppender;
import com.gempukku.stccg.effectappender.DelayedAppender;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.TextUtils;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class CardResolver {

    public static EffectAppender resolveStackedCards(String type, ValueSource countSource, FilterableSource stackedOn,
                                                     String memory, String choicePlayer, String choiceText, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        return resolveStackedCards(type, null, null, countSource, stackedOn, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveStackedCards(String type, FilterableSource choiceFilter, FilterableSource playabilityFilter, ValueSource countSource, FilterableSource stackedOn,
                                                     String memory, String choicePlayer, String choiceText, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
            final Filterable stackedOnFilter = stackedOn.getFilterable(actionContext);
            return Filters.filter(actionContext.getGameState().getAllCardsInGame(), actionContext.getGame(), Filters.stackedOn(stackedOnFilter));
        };
        final PlayerSource choicePlayerSource = PlayerResolver.resolvePlayer(choicePlayer);

        if (type.startsWith("memory(") && type.endsWith(")")) {
            return resolveMemoryCards(type, choiceFilter, playabilityFilter, countSource, memory, cardSource, choicePlayerSource);
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer);
            ChoiceEffectSource effectSource = (possibleCards, action, actionContext, min, max) -> {
                String choicePlayerId = playerSource.getPlayerId(actionContext);
                return new ChooseStackedCardsEffect(actionContext.getGame(), choicePlayerId, min, max, stackedOn.getFilterable(actionContext), Filters.in(possibleCards)) {
                    @Override
                    protected void cardsChosen(Collection<PhysicalCard> stackedCards) {
                        actionContext.setCardMemory(memory, stackedCards);
                    }

                    @Override
                    public String getText() {
                        return actionContext.substituteText(choiceText);
                    }
                };
            };

            return resolveChoiceCards(type, choiceFilter, playabilityFilter, countSource, environment, cardSource, effectSource);
        }
        throw new RuntimeException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCardsInHand(String type, ValueSource countSource, String memory, String choicePlayer, String handPlayer, String choiceText, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        return resolveCardsInHandNew(type, null, countSource, memory, choicePlayer, handPlayer, choiceText, false, environment);
    }

    public static EffectAppender resolveCardsInHand(String type, ValueSource countSource, String memory,
                                                    String choicePlayer, String handPlayer, String choiceText,
                                                    boolean showMatchingOnly, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        return resolveCardsInHandNew(type, null, countSource, memory, choicePlayer, handPlayer, choiceText,
                showMatchingOnly, environment);
    }


    public static EffectAppender simpleResolve(String type, FilterableSource choiceFilter, FilterableSource playabilityFilter, ValueSource countSource,
                                               String memory, PlayerSource choicePlayerSource,
                                               Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource,
                                               CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {

        if (type.equals("self"))
            return resolveSelf(choiceFilter, playabilityFilter, countSource, memory, cardSource, choicePlayerSource);
        if (type.startsWith("memory(") && type.endsWith(")"))
            return resolveMemoryCards(type, choiceFilter, playabilityFilter, countSource, memory, cardSource, choicePlayerSource);
        if (type.startsWith("all(") && type.endsWith(")"))
            return resolveAllCards(type, choiceFilter, memory, environment, cardSource);
        else throw new RuntimeException("Unable to resolve card resolver of type: " + type);
    }


    private static ChoiceEffectSource createChoiceEffectSource(PlayerSource choicePlayerSource, PlayerSource targetPlayerSource, Zone zone,
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
            } else {    // THIS IS HAND-SPECIFIC!
                return new ChooseArbitraryCardsEffect(actionContext.getGame(), choicePlayerId,
                        actionContext.substituteText(choiceText),
                        actionContext.getGameState().getHand(targetPlayerId), Filters.in(possibleCards),
                        min, max, showMatchingOnly) {
                    @Override
                    protected void cardsSelected(Collection<PhysicalCard> selectedCards) {
                        actionContext.setCardMemory(memory, selectedCards);
                    }
                };

            }
        };
    }

    private static EffectAppender resolveRandomCards(String type, PlayerSource targetPlayerSource, String memory) {
        final int count = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.lastIndexOf(")")));
        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String handPlayer = targetPlayerSource.getPlayerId(actionContext);
                return actionContext.getGameState().getHand(handPlayer).size() >= count;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String handPlayer = targetPlayerSource.getPlayerId(context);
                return new UnrespondableEffect(context) {
                    @Override
                    protected void doPlayEffect() {
                        List<? extends PhysicalCard> hand = context.getGameState().getHand(handPlayer);
                        List<PhysicalCard> randomCardsFromHand = TextUtils.getRandomFromList(hand, 2);
                        context.setCardMemory(memory, randomCardsFromHand);
                    }
                };
            }
        };
    }

    public static EffectAppender resolveCardsInHand(String type, FilterableSource additionalFilter, ValueSource countSource, String memory, String choicePlayer, String handPlayer, String choiceText, boolean showMatchingOnly, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        final PlayerSource handSource = PlayerResolver.resolvePlayer(handPlayer);
        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
            String handPlayer1 = handSource.getPlayerId(actionContext);
            return actionContext.getGame().getGameState().getHand(handPlayer1);
        };

        if (type.startsWith("random(") && type.endsWith(")")) {
            final int count = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.lastIndexOf(")")));
            return new DelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    final String handPlayer = handSource.getPlayerId(actionContext);
                    return actionContext.getGame().getGameState().getHand(handPlayer).size() >= count;
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    final String handPlayer = handSource.getPlayerId(actionContext);
                    return new UnrespondableEffect(actionContext) {
                        @Override
                        protected void doPlayEffect() {
                            List<? extends PhysicalCard> hand = actionContext.getGame().getGameState().getHand(handPlayer);
                            List<PhysicalCard> randomCardsFromHand = TextUtils.getRandomFromList(hand, 2);
                            actionContext.setCardMemory(memory, randomCardsFromHand);
                        }
                    };
                }
            };
/*        } else if (type.equals("self")) {
            return resolveSelf(additionalFilter, additionalFilter, countSource, memory, cardSource);
        } else if (type.startsWith("memory(") && type.endsWith(")")) {
            return resolveMemoryCards(type, additionalFilter, additionalFilter, countSource, memory, cardSource);
        } else if (type.startsWith("all(") && type.endsWith(")")) {
            return resolveAllCards(type, additionalFilter, memory, environment, cardSource); */
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer);
            ChoiceEffectSource effectSource = (possibleCards, action, actionContext, min, max) -> {
                String handId = handSource.getPlayerId(actionContext);
                String choicePlayerId = playerSource.getPlayerId(actionContext);
                    return new ChooseCardsFromZoneEffect(actionContext.getGame(), Zone.HAND, choicePlayerId, min, max, Filters.in(possibleCards)) {
                        @Override
                        protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards) {
                            actionContext.setCardMemory(memory, cards);
                        }

                        @Override
                        public String getText() {
                            return actionContext.substituteText(choiceText);
                        }
                    };
            };

            return resolveChoiceCards(type, additionalFilter, additionalFilter, countSource, environment, cardSource, effectSource);
        }
        throw new RuntimeException("Unable to resolve card resolver of type: " + type);
    }



    public static EffectAppender resolveCardsInHandNew(String type, FilterableSource additionalFilter,
                                                       ValueSource countSource, String memory, String choicePlayer,
                                                       String handPlayer, String choiceText, boolean showMatchingOnly,
                                                       CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        final PlayerSource targetPlayerSource = PlayerResolver.resolvePlayer(handPlayer);
        final PlayerSource choicePlayerSource = PlayerResolver.resolvePlayer(choicePlayer);
        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
            String handPlayer1 = targetPlayerSource.getPlayerId(actionContext);
            return actionContext.getGameState().getHand(handPlayer1);
        };

        if (type.equals("self") ||
                (type.startsWith("memory(") && type.endsWith(")")) ||
                (type.startsWith("all(") && type.endsWith(")")))
            return simpleResolve(type, additionalFilter, additionalFilter, countSource, memory, choicePlayerSource, cardSource, environment);
        if (type.startsWith("random(") && type.endsWith(")"))
            return resolveRandomCards(type, targetPlayerSource, memory);
        if (type.startsWith("choose(") && type.endsWith(")")) {
            ChoiceEffectSource effectSource = createChoiceEffectSource(choicePlayerSource, targetPlayerSource,
                    Zone.HAND, memory, choiceText, showMatchingOnly);
            return resolveChoiceCards(type, additionalFilter, additionalFilter, countSource, environment, cardSource, effectSource);
        }
        throw new RuntimeException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCardsInDiscard(String type, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        return resolveCardsInDiscard(type, null, null, countSource, memory, choicePlayer, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCardsInDiscard(String type, ValueSource countSource, String memory,
                                                       String choicePlayer, String targetPlayerDiscard,
                                                       String choiceText, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        return resolveCardsInDiscard(type, null, null, countSource, memory, choicePlayer, targetPlayerDiscard, choiceText, environment);
    }


    public static EffectAppender resolveCardsInDiscard(String type, FilterableSource choiceFilter,
                                                       FilterableSource playabilityFilter, ValueSource countSource,
                                                       String memory, String choicePlayer, String targetPlayerDiscard,
                                                       String choiceText, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        final PlayerSource choicePlayerSource = PlayerResolver.resolvePlayer(choicePlayer);
        final PlayerSource targetPlayerSource = PlayerResolver.resolvePlayer(targetPlayerDiscard);

        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
            String targetPlayerId = targetPlayerSource.getPlayerId(actionContext);
            return actionContext.getGameState().getDiscard(targetPlayerId);
        };

        if (type.equals("self") ||
                (type.startsWith("memory(") && type.endsWith(")")) ||
                (type.startsWith("all(") && type.endsWith(")")))
            return simpleResolve(type, choiceFilter, playabilityFilter, countSource, memory, choicePlayerSource, cardSource, environment);
        else if (type.startsWith("choose(") && type.endsWith(")")) {
            ChoiceEffectSource effectSource = createChoiceEffectSource(choicePlayerSource, targetPlayerSource,
                    Zone.DISCARD, memory, choiceText, false);
            return resolveChoiceCards(type, choiceFilter, playabilityFilter, countSource, environment, cardSource, effectSource);
        }
        throw new RuntimeException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCardsInDeck(String type, ValueSource countSource, String memory,
                                                    String choicePlayer, String choiceText,
                                                    CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        return resolveCardsInZone(type, null, countSource, memory, PlayerResolver.resolvePlayer(choicePlayer),
                PlayerResolver.resolvePlayer(choicePlayer), choiceText, environment, Zone.DRAW_DECK);
    }

    public static EffectAppender resolveCardsInZone(String type, FilterableSource choiceFilter, ValueSource countSource,
                                                    String memory, PlayerSource playerSource, String choiceText,
                                                    CardBlueprintFactory blueprintFactory, Zone zone)
            throws InvalidCardDefinitionException {
        return resolveCardsInZone(type, choiceFilter, countSource, memory, playerSource, playerSource,
                choiceText, blueprintFactory, zone);
    }

    public static EffectAppender resolveCardsInZone(String type, FilterableSource choiceFilter, ValueSource countSource,
                                                    String memory, PlayerSource choicePlayerSource,
                                                    PlayerSource targetPlayerSource, String choiceText,
                                                    CardBlueprintFactory blueprintFactory, Zone zone) throws InvalidCardDefinitionException {
        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
            String targetPlayerId = targetPlayerSource.getPlayerId(actionContext);
            return actionContext.getGameState().getDrawDeck(targetPlayerId);
        };

        if ((type.startsWith("memory(") && type.endsWith(")")) ||
                (type.startsWith("all(") && type.endsWith(")")))
            return simpleResolve(type, choiceFilter, choiceFilter, countSource, memory, choicePlayerSource, cardSource, blueprintFactory);
        else if (type.startsWith("choose(") && type.endsWith(")")) {
            ChoiceEffectSource effectSource = createChoiceEffectSource(choicePlayerSource, targetPlayerSource, zone,
                    memory, choiceText, false);
            return resolveChoiceCards(type, choiceFilter, choiceFilter, countSource, blueprintFactory, cardSource, effectSource);
        }
        throw new RuntimeException("Unable to resolve card resolver of type: " + type);
    }


    public static EffectAppender resolveCard(String type, FilterableSource additionalFilter, String memory, String choicePlayer, String choiceText, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        return resolveCards(type, additionalFilter, new ConstantValueSource(1), memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCard(String type, String memory, String choicePlayer, String choiceText, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        return resolveCard(type, null, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCards(String type, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        return resolveCards(type, null, countSource, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCards(String type, FilterableSource additionalFilter, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        return resolveCards(type, additionalFilter, additionalFilter, countSource, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCards(String type, FilterableSource additionalFilter,
                                              FilterableSource playabilityFilter, ValueSource countSource,
                                              String memory, String choicePlayer, String choiceText,
                                              CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext ->
                Filters.filterActive(actionContext.getGame(), Filters.any);
        final PlayerSource choicePlayerSource = PlayerResolver.resolvePlayer(choicePlayer);


        if (type.equals("self")) {
            return resolveSelf(additionalFilter, playabilityFilter, countSource, memory, cardSource, choicePlayerSource);
        } else if (type.startsWith("memory(") && type.endsWith(")")) {
            return resolveMemoryCards(type, additionalFilter, playabilityFilter, countSource, memory, cardSource, choicePlayerSource);
        } else if (type.startsWith("all(") && type.endsWith(")")) {
            return resolveAllCards(type, additionalFilter, memory, environment, cardSource);
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer);
            ChoiceEffectSource effectSource = (possibleCards, action, actionContext, min, max) -> {
                String choicePlayerId = playerSource.getPlayerId(actionContext);
                return new ChooseActiveCardsEffect(actionContext, choicePlayerId,
                        actionContext.substituteText(choiceText), min, max, Filters.in(possibleCards)) {
                    @Override
                    protected void cardsSelected(Collection<PhysicalCard> cards) {
                        actionContext.setCardMemory(memory, cards);
                    }
                };
            };

            return resolveChoiceCards(type, additionalFilter, playabilityFilter, countSource, environment, cardSource, effectSource);
        }
        throw new InvalidCardDefinitionException("Unable to resolve card resolver of type: " + type);
    }

    private static DefaultDelayedAppender resolveSelf(FilterableSource choiceFilter, FilterableSource playabilityFilter,
                                                      ValueSource countSource, String memory,
                                                      Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource,
                                                      PlayerSource choicePlayer) {
        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                int min = countSource.getMinimum(actionContext);
                return filterCards(actionContext, playabilityFilter).size() >= min;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                Collection<PhysicalCard> result = filterCards(context, choiceFilter);
                return new DefaultEffect(context.getGame(), choicePlayer.getPlayerId(context)) {
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

            private Collection<PhysicalCard> filterCards(ActionContext actionContext, FilterableSource filter) {
                PhysicalCard source = actionContext.getSource();
                Filterable additionalFilterable = Filters.any;
                if (filter != null)
                    additionalFilterable = filter.getFilterable(actionContext);
                return Filters.filter(cardSource.apply(actionContext), actionContext.getGame(), source, additionalFilterable);
            }
        };
    }

    private static DefaultDelayedAppender resolveMemoryCards(String type, FilterableSource choiceFilter, FilterableSource playabilityFilter,
                                                             ValueSource countSource, String memory,
                                                             Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource,
                                                             PlayerSource choicePlayerSource) throws InvalidCardDefinitionException {
        String sourceMemory = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
        if (sourceMemory.contains("(") || sourceMemory.contains(")"))
            throw new InvalidCardDefinitionException("Memory name cannot contain parenthesis");

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                if (playabilityFilter != null) {
                    int min = countSource.getMinimum(actionContext);
                    return filterCards(actionContext, playabilityFilter).size() >= min;
                } else {
                    return true;
                }
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                Collection<PhysicalCard> result = filterCards(context, choiceFilter);
                return new DefaultEffect(context.getGame(), choicePlayerSource.getPlayerId(context)) {
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

            private Collection<PhysicalCard> filterCards(ActionContext actionContext, FilterableSource filter) {
                Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(sourceMemory);
                Filterable additionalFilterable = Filters.any;
                if (filter != null)
                    additionalFilterable = filter.getFilterable(actionContext);
                return Filters.filter(cardSource.apply(actionContext), actionContext.getGame(), Filters.in(cardsFromMemory), additionalFilterable);
            }
        };
    }


    private static DefaultDelayedAppender resolveChoiceCards(String type, FilterableSource choiceFilter,
                                                             FilterableSource playabilityFilter,
                                                             ValueSource countSource,
                                                             CardBlueprintFactory environment,
                                                             Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource,
                                                             ChoiceEffectSource effectSource)
            throws InvalidCardDefinitionException {
        final String filter = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
                // TODO - The line below was changed for ST1E implementation, but Tribbles assumes the old code
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);
//        final FilterableSource source = environment.getFilterFactory().parseSTCCGFilter(filter);

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
                return effectSource.createEffect(filterCards(context, choiceFilter), action, context, min, max);
            }

            private Collection<PhysicalCard> filterCards(ActionContext actionContext, FilterableSource filter) {
                Filterable filterable = filterableSource.getFilterable(actionContext);
                Filterable additionalFilterable = Filters.any;
                if (filter != null)
                    additionalFilterable = filter.getFilterable(actionContext);
                return Filters.filter(cardSource.apply(actionContext), actionContext.getGame(), filterable, additionalFilterable);
            }
        };
    }

    private static DefaultDelayedAppender resolveAllCards(String type, FilterableSource additionalFilter, String memory, CardBlueprintFactory environment, Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource) throws InvalidCardDefinitionException {
        final String filter = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);
        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new UnrespondableEffect(context) {
                    @Override
                    protected void doPlayEffect() {
                        context.setCardMemory(memory, filterCards(context, additionalFilter));
                    }

                    private Collection<PhysicalCard> filterCards(ActionContext actionContext, FilterableSource filter) {
                        final Filterable filterable = filterableSource.getFilterable(actionContext);
                        Filterable additionalFilterable = Filters.any;
                        if (filter != null)
                            additionalFilterable = filter.getFilterable(actionContext);
                        return Filters.filter(cardSource.apply(actionContext), actionContext.getGame(), filterable, additionalFilterable);
                    }
                };
            }
        };
    }

    private interface ChoiceEffectSource {
        Effect createEffect(Collection<? extends PhysicalCard> possibleCards, CostToEffectAction action, ActionContext actionContext,
                            int min, int max);
    }
}
