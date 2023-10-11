package com.gempukku.stccg.effectappender.resolver;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.DefaultDelayedAppender;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.choose.*;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class CardResolver {
    public static EffectAppender resolveStackedCards(String type, ValueSource countSource, FilterableSource stackedOn,
                                                     String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveStackedCards(type, null, countSource, stackedOn, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveStackedCards(String type, FilterableSource additionalFilter, ValueSource countSource, FilterableSource stackedOn,
                                                     String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveStackedCards(type, additionalFilter, additionalFilter, countSource, stackedOn, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveStackedCards(String type, FilterableSource choiceFilter, FilterableSource playabilityFilter, ValueSource countSource, FilterableSource stackedOn,
                                                     String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
            final Filterable stackedOnFilter = stackedOn.getFilterable(actionContext);
            return Filters.filter(actionContext.getGame().getGameState().getAllCardsInGame(), actionContext.getGame(), Filters.stackedOn(stackedOnFilter));
        };

        if (type.startsWith("memory(") && type.endsWith(")")) {
            return resolveMemoryCards(type, choiceFilter, playabilityFilter, countSource, memory, cardSource);
        }
        else if (type.startsWith("choose(") && type.endsWith(")")) {
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer);
            ChoiceEffectSource effectSource = (possibleCards, action, actionContext, min, max) -> {
                String choicePlayerId = playerSource.getPlayer(actionContext);
                return new ChooseStackedCardsEffect(actionContext.getGame(), choicePlayerId, min, max, stackedOn.getFilterable(actionContext), Filters.in(possibleCards)) {
                    @Override
                    protected void cardsChosen(DefaultGame game, Collection<PhysicalCard> stackedCards) {
                        actionContext.setCardMemory(memory, stackedCards);
                    }

                    @Override
                    public String getText() {
                        return GameUtils.SubstituteText(choiceText, actionContext);
                    }
                };
            };

            return resolveChoiceCards(type, choiceFilter, playabilityFilter, countSource, environment, cardSource, effectSource);
        }
        throw new RuntimeException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCardsInHand(String type, ValueSource countSource, String memory, String choicePlayer, String handPlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInHand(type, null, countSource, memory, choicePlayer, handPlayer, choiceText, false, environment);
    }

    public static EffectAppender resolveCardsInHand(String type, ValueSource countSource, String memory, String choicePlayer, String handPlayer, String choiceText, boolean showMatchingOnly, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInHand(type, null, countSource, memory, choicePlayer, handPlayer, choiceText, showMatchingOnly, environment);
    }

    public static EffectAppender resolveCardsInHand(String type, FilterableSource additionalFilter, ValueSource countSource, String memory, String choicePlayer, String handPlayer, String choiceText, boolean showMatchingOnly, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final PlayerSource handSource = PlayerResolver.resolvePlayer(handPlayer);
        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
            String handPlayer1 = handSource.getPlayer(actionContext);
            return actionContext.getGame().getGameState().getHand(handPlayer1);
        };

        if (type.startsWith("random(") && type.endsWith(")")) {
            final int count = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.lastIndexOf(")")));
            return new DefaultDelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    final String handPlayer = handSource.getPlayer(actionContext);
                    return actionContext.getGame().getGameState().getHand(handPlayer).size() >= count;
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    final String handPlayer = handSource.getPlayer(actionContext);
                    return new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect() {
                            List<? extends PhysicalCard> hand = actionContext.getGame().getGameState().getHand(handPlayer);
                            List<PhysicalCard> randomCardsFromHand = GameUtils.getRandomCards(hand, 2);
                            actionContext.setCardMemory(memory, randomCardsFromHand);
                        }
                    };
                }
            };
        } else if (type.equals("self")) {
            return resolveSelf(additionalFilter, additionalFilter, countSource, memory, cardSource);
        } else if (type.startsWith("memory(") && type.endsWith(")")) {
            return resolveMemoryCards(type, additionalFilter, additionalFilter, countSource, memory, cardSource);
        } else if (type.startsWith("all(") && type.endsWith(")")) {
            return resolveAllCards(type, additionalFilter, memory, environment, cardSource);
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer);
            ChoiceEffectSource effectSource = (possibleCards, action, actionContext, min, max) -> {
                String handId = handSource.getPlayer(actionContext);
                String choicePlayerId = playerSource.getPlayer(actionContext);
                if (handId.equals(choicePlayerId)) {
                    return new ChooseCardsFromZoneEffect(actionContext.getGame(), Zone.HAND, choicePlayerId, min, max, Filters.in(possibleCards)) {
                        @Override
                        protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards) {
                            actionContext.setCardMemory(memory, cards);
                        }

                        @Override
                        public String getText() {
                            return GameUtils.SubstituteText(choiceText, actionContext);
                        }
                    };
                } else {
                    List<? extends PhysicalCard> cardsInHand = actionContext.getGame().getGameState().getHand(handId);
                    return new ChooseArbitraryCardsEffect(actionContext.getGame(), choicePlayerId, GameUtils.SubstituteText(choiceText, actionContext), cardsInHand, Filters.in(possibleCards), min, max, showMatchingOnly) {
                        @Override
                        protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> selectedCards) {
                            actionContext.setCardMemory(memory, selectedCards);
                        }
                    };
                }
            };

            return resolveChoiceCards(type, additionalFilter, additionalFilter, countSource, environment, cardSource, effectSource);
        }
        throw new RuntimeException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCardsInDiscard(String type, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInDiscard(type, null, countSource, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCardsInDiscard(String type, ValueSource countSource, String memory, String choicePlayer, String targetPlayerDiscard, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInDiscard(type, null, countSource, memory, choicePlayer, targetPlayerDiscard, choiceText, environment);
    }

    public static EffectAppender resolveCardsInDiscard(String type, FilterableSource additionalFilter, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInDiscard(type, additionalFilter, additionalFilter, countSource, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCardsInDiscard(String type, FilterableSource additionalFilter, ValueSource countSource, String memory, String choicePlayer, String targetPlayerDiscard, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInDiscard(type, additionalFilter, additionalFilter, countSource, memory, choicePlayer,  targetPlayerDiscard, choiceText, environment);
    }


    public static EffectAppender resolveCardsInDiscard(String type, FilterableSource choiceFilter, FilterableSource playabilityFilter, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInDiscard(type, choiceFilter, playabilityFilter, countSource, memory, choicePlayer, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCardsInDiscard(String type, FilterableSource choiceFilter, FilterableSource playabilityFilter, ValueSource countSource, String memory, String choicePlayer, String targetPlayerDiscard, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer);
        final PlayerSource targetPlayerDiscardSource = PlayerResolver.resolvePlayer(targetPlayerDiscard);

        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
            String targetPlayerId = targetPlayerDiscardSource.getPlayer(actionContext);
            return actionContext.getGame().getGameState().getDiscard(targetPlayerId);
        };

        if (type.equals("self")) {
            return resolveSelf(choiceFilter, playabilityFilter, countSource, memory, cardSource);
        } else if (type.startsWith("memory(") && type.endsWith(")")) {
            return resolveMemoryCards(type, choiceFilter, playabilityFilter, countSource, memory, cardSource);
        } else if (type.startsWith("all(") && type.endsWith(")")) {
            return resolveAllCards(type, choiceFilter, memory, environment, cardSource);
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            ChoiceEffectSource effectSource = (possibleCards, action, actionContext, min, max) -> {
                String choicePlayerId = playerSource.getPlayer(actionContext);
                String targetPlayerDiscardId = targetPlayerDiscardSource.getPlayer(actionContext);
                return new ChooseCardsFromZoneEffect(actionContext.getGame(), Zone.DISCARD, choicePlayerId, targetPlayerDiscardId, min, max, Filters.in(possibleCards)) {
                    @Override
                    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards) {
                        actionContext.setCardMemory(memory, cards);
                    }

                    @Override
                    public String getText() {
                        return GameUtils.SubstituteText(choiceText, actionContext);
                    }
                };
            };

            return resolveChoiceCards(type, choiceFilter, playabilityFilter, countSource, environment, cardSource, effectSource);
        }
        throw new RuntimeException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCardsInDeck(String type, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInDeck(type, null, countSource, memory, choicePlayer, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCardsInDeck(String type, FilterableSource choiceFilter, ValueSource countSource, String memory, String choicePlayer, String targetDeck, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer);
        final PlayerSource deckSource = PlayerResolver.resolvePlayer(targetDeck);

        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
            String deckId = deckSource.getPlayer(actionContext);
            return actionContext.getGame().getGameState().getDrawDeck(deckId);
        };

        if (type.startsWith("memory(") && type.endsWith(")")) {
            return resolveMemoryCards(type, choiceFilter, choiceFilter, countSource, memory, cardSource);
        } else if (type.startsWith("all(") && type.endsWith(")")) {
            return resolveAllCards(type, choiceFilter, memory, environment, cardSource);
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            ChoiceEffectSource effectSource = (possibleCards, action, actionContext, min, max) -> {
                String choicePlayerId = playerSource.getPlayer(actionContext);
                String targetDeckId = deckSource.getPlayer(actionContext);
                return new ChooseCardsFromZoneEffect(actionContext.getGame(), Zone.DRAW_DECK, choicePlayerId,
                        targetDeckId, min, max, Filters.in(possibleCards)) {
                    @Override
                    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards) {
                        actionContext.setCardMemory(memory, cards);
                    }

                    @Override
                    public String getText() {
                        return GameUtils.SubstituteText(choiceText, actionContext);
                    }
                };
            };

            return resolveChoiceCards(type, choiceFilter, choiceFilter, countSource, environment, cardSource, effectSource);
        }
        throw new RuntimeException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCard(String type, FilterableSource additionalFilter, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCards(type, additionalFilter, new ConstantEvaluator(1), memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCard(String type, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCard(type, null, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCards(String type, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCards(type, null, countSource, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCards(String type, FilterableSource additionalFilter, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCards(type, additionalFilter, additionalFilter, countSource, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCards(String type, FilterableSource additionalFilter, FilterableSource playabilityFilter, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext ->
                Filters.filterActive(actionContext.getGame(), Filters.any);

        if (type.equals("self")) {
            return resolveSelf(additionalFilter, playabilityFilter, countSource, memory, cardSource);
        } else if (type.equals("bearer")) {
            return new DefaultDelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    int min = countSource.getMinimum(actionContext);
                    return filterCards(actionContext, playabilityFilter).size() >= min;
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    Collection<PhysicalCard> result = filterCards(actionContext, additionalFilter);
                    return new DefaultEffect() {
                        @Override
                        public boolean isPlayableInFull() {
                            int min = countSource.getMinimum(actionContext);
                            return result.size() >= min;
                        }

                        @Override
                        protected FullEffectResult playEffectReturningResult() {
                            actionContext.setCardMemory(memory, result);
                            int min = countSource.getMinimum(actionContext);
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
                    PhysicalCard attachedTo = source.getAttachedTo();
                    if (attachedTo == null)
                        return Collections.emptySet();
                    
                    Filterable additionalFilterable = Filters.any;
                    if (filter != null)
                        additionalFilterable = filter.getFilterable(actionContext);
                    return Filters.filter(cardSource.apply(actionContext), actionContext.getGame(), attachedTo, additionalFilterable);
                }
            };
        } else if (type.startsWith("memory(") && type.endsWith(")")) {
            return resolveMemoryCards(type, additionalFilter, playabilityFilter, countSource, memory, cardSource);
        } else if (type.startsWith("all(") && type.endsWith(")")) {
            return resolveAllCards(type, additionalFilter, memory, environment, cardSource);
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer);
            ChoiceEffectSource effectSource = (possibleCards, action, actionContext, min, max) -> {
                String choicePlayerId = playerSource.getPlayer(actionContext);
                return new ChooseActiveCardsEffect(actionContext, choicePlayerId, GameUtils.SubstituteText(choiceText, actionContext), min, max, Filters.in(possibleCards)) {
                    @Override
                    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards) {
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
                                               Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource) {
        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                int min = countSource.getMinimum(actionContext);
                return filterCards(actionContext, playabilityFilter).size() >= min;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                Collection<PhysicalCard> result = filterCards(actionContext, choiceFilter);
                return new DefaultEffect() {
                    @Override
                    public boolean isPlayableInFull() {
                        int min = countSource.getMinimum(actionContext);
                        return result.size() >= min;
                    }

                    @Override
                    protected FullEffectResult playEffectReturningResult() {
                        actionContext.setCardMemory(memory, result);
                        int min = countSource.getMinimum(actionContext);
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
                                                      Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource) throws InvalidCardDefinitionException {
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
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                Collection<PhysicalCard> result = filterCards(actionContext, choiceFilter);
                return new DefaultEffect() {
                    @Override
                    public boolean isPlayableInFull() {
                        int min = countSource.getMinimum(actionContext);
                        return result.size() >= min;
                    }

                    @Override
                    protected FullEffectResult playEffectReturningResult() {
                        actionContext.setCardMemory(memory, result);
                        int min = countSource.getMinimum(actionContext);
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

    private static DefaultDelayedAppender resolveChoiceCards(String type, FilterableSource choiceFilter, FilterableSource playabilityFilter,
                                                      ValueSource countSource, CardGenerationEnvironment environment, Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource, ChoiceEffectSource effectSource) throws InvalidCardDefinitionException {
        final String filter = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                int min = countSource.getMinimum(actionContext);
                return filterCards(actionContext, playabilityFilter).size() >= min;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                int min = countSource.getMinimum(actionContext);
                int max = countSource.getMaximum(actionContext);
                return effectSource.createEffect(filterCards(actionContext, choiceFilter), action, actionContext, min, max);
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

    private static DefaultDelayedAppender resolveAllCards(String type, FilterableSource additionalFilter, String memory, CardGenerationEnvironment environment, Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource) throws InvalidCardDefinitionException {
        final String filter = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect() {
                        actionContext.setCardMemory(memory, filterCards(actionContext, additionalFilter));
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
