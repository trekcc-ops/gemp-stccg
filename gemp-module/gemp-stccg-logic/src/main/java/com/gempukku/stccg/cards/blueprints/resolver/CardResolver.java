package com.gempukku.stccg.cards.blueprints.resolver;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.blueprints.SubActionBlueprint;
import com.gempukku.stccg.actions.choose.SelectCardsFromDialogAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class CardResolver {

    public static SubActionBlueprint resolveCardsInZone(String type, FilterBlueprint choiceFilter,
                                                        ValueSource countSource,
                                                        String memory, PlayerSource selectingPlayer,
                                                        PlayerSource targetPlayer, String choiceText,
                                                        FilterBlueprint typeFilter, Zone zone,
                                                        Function<ActionContext, List<PhysicalCard>> cardSource) {

        String selectionType = (type.contains("(")) ? type.substring(0,type.indexOf("(")) : type;

        return switch (selectionType) {
            case "self", "memory", "all", "random" ->
                    finalTargetAppender(choiceFilter, choiceFilter, countSource, memory, cardSource,
                            selectionType, typeFilter);
            case "choose" -> resolveChoiceCards(typeFilter, choiceFilter, choiceFilter, countSource, cardSource,
                    createChoiceActionSourceFromZone(selectingPlayer, targetPlayer, zone, memory, choiceText,
                            cardSource));
            default -> throw new RuntimeException("Unable to resolve card resolver of type: " + selectionType);
        };
    }


    public static Function<ActionContext, Action> getChoiceEffectFromInPlay(String choiceText, ValueSource countSource,
                                                                             String memory,
                                                                             PlayerSource choicePlayer,
                                                                             Function<ActionContext, List<PhysicalCard>> cardSource,
                                                                             FilterBlueprint typeFilter, FilterBlueprint choiceFilter) {
        return (actionContext) -> {
            try {
                List<PhysicalCard> possibleCards = (List<PhysicalCard>) Filters.filter(cardSource.apply(actionContext),
                        typeFilter.getFilterable(actionContext),
                        choiceFilter == null ? Filters.any : choiceFilter.getFilterable(actionContext));

                String selectingPlayerId = choicePlayer.getPlayerId(actionContext);
                Player selectingPlayer = actionContext.getGame().getPlayer(selectingPlayerId);

                return new SelectVisibleCardsAction(selectingPlayer,
                        actionContext.substituteText(choiceText), Filters.in(possibleCards),
                        countSource.getMinimum(actionContext), countSource.getMaximum(actionContext),
                        actionContext, memory);
            } catch(PlayerNotFoundException exp) {
                actionContext.getGame().sendErrorMessage(exp);
                actionContext.getGame().cancelGame();
                return null;
            }
        };
    }




    private static ChoiceActionSource createChoiceActionSourceFromZone(PlayerSource selectingPlayer,
                                                                       PlayerSource targetPlayer, Zone zone,
                                                                       String memory, String choiceText,
                                                                       Function<ActionContext, List<PhysicalCard>> cardSource) {
        return (possibleCards, action, actionContext, min, max) -> {
            String choicePlayerId = selectingPlayer.getPlayerId(actionContext);
            Player choicePlayer = actionContext.getGame().getPlayer(choicePlayerId);
            String targetPlayerId = targetPlayer.getPlayerId(actionContext);
            if (targetPlayerId.equals(choicePlayerId) && zone == Zone.HAND) {
                return new SelectVisibleCardsAction(choicePlayer, actionContext.substituteText(choiceText),
                        Filters.in(possibleCards), min, max, actionContext, memory);
            } else {
                return new SelectCardsFromDialogAction(choicePlayer,
                        actionContext.substituteText(choiceText),
                        Filters.and(Filters.in(cardSource.apply(actionContext)), Filters.in(possibleCards)),
                        min, max, actionContext, memory);
            }
        };
    }


    public static SubActionBlueprint finalTargetAppender(FilterBlueprint choiceFilter,
                                                              FilterBlueprint playabilityFilter,
                                                              ValueSource countSource, String memory,
                                                              Function<ActionContext, List<PhysicalCard>> cardSource,
                                                              String selectionType, FilterBlueprint typeFilter) {

        return new SubActionBlueprint() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                switch(selectionType) {
                    case "self", "random", "choose":
                        return filterCards(actionContext, playabilityFilter).size() >=
                                countSource.getMinimum(actionContext);
                    case "memory":
                        if (playabilityFilter != null) {
                            return filterCards(actionContext, playabilityFilter).size() >=
                                    countSource.getMinimum(actionContext);
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
            public List<Action> createActions(CardPerformedAction parentAction, ActionContext context) throws PlayerNotFoundException {
                Action action = switch (selectionType) {
                    case "self", "memory" -> {
                        Collection<PhysicalCard> result = filterCards(context, choiceFilter);
                        yield new SubAction(parentAction, context) {
                            @Override
                            public boolean requirementsAreMet(DefaultGame cardGame) {
                                int min = countSource.getMinimum(context);
                                return result.size() >= min;
                            }

                            @Override
                            public Action nextAction(DefaultGame cardGame) {
                                context.setCardMemory(memory, result);
                                return getNextAction();
                            }
                        };
                    }
                    case "all" -> new SubAction(parentAction, context) {
                        @Override
                        public Action nextAction(DefaultGame cardGame) {
                            context.setCardMemory(memory, filterCards(context, choiceFilter));
                            return getNextAction();
                        }
                    };
                    case "random" -> new SubAction(parentAction, context) {
                        @Override
                        public Action nextAction(DefaultGame cardGame) {
                            context.setCardMemory(memory,
                                    TextUtils.getRandomItemsFromList(filterCards(context, playabilityFilter),
                                            countSource.evaluateExpression(context)));
                            return getNextAction();
                        }
                    };
                    default -> null;
                };
                if (action != null) {
                    return List.of(action);
                } else {
                    return new LinkedList<>();
                }
            }

            private Collection<PhysicalCard> filterCards(ActionContext actionContext, FilterBlueprint filter) {
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


    private static SubActionBlueprint resolveChoiceCards(FilterBlueprint typeFilter, FilterBlueprint choiceFilter,
                                                             FilterBlueprint playabilityFilter,
                                                             ValueSource countSource,
                                                             Function<ActionContext, List<PhysicalCard>> cardSource,
                                                             ChoiceActionSource effectSource) {

        return new SubActionBlueprint() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                int min = countSource.getMinimum(actionContext);
                return filterCards(actionContext, playabilityFilter).size() >= min;
            }

            @Override
            public List<Action> createActions(CardPerformedAction action, ActionContext context) throws PlayerNotFoundException {
                List<Action> result = new LinkedList<>();
                Collection<PhysicalCard> cards = filterCards(context, choiceFilter);
                Action selectionAction = effectSource.createAction(cards, action, context,
                        countSource.getMinimum(context), countSource.getMaximum(context));
                result.add(selectionAction);
                return result;
            }

            private Collection<PhysicalCard> filterCards(ActionContext actionContext, FilterBlueprint filter) {
                Filterable filterable = typeFilter.getFilterable(actionContext);
                Filterable additionalFilterable = Filters.any;
                if (filter != null)
                    additionalFilterable = filter.getFilterable(actionContext);
                return Filters.filter(cardSource.apply(actionContext), actionContext.getGame(), filterable,
                        additionalFilterable);
            }
        };
    }

    public static SubActionBlueprint resolveChoiceCardsWithEffect(FilterBlueprint typeFilter,
                                                                       FilterBlueprint playabilityFilter,
                                                                       ValueSource countSource,
                                                                       Function<ActionContext, List<PhysicalCard>> cardSource,
                                                                       Function<ActionContext, Action> choiceAction) {
        return new SubActionBlueprint() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                int min = countSource.getMinimum(actionContext);
                return filterCards(actionContext, playabilityFilter).size() >= min;
            }

            @Override
            public List<Action> createActions(CardPerformedAction action, ActionContext context) {
                List<Action> result = new LinkedList<>();
                Action selectionAction = choiceAction.apply(context);
                result.add(selectionAction);
                return result;
            }

            private Collection<PhysicalCard> filterCards(ActionContext actionContext, FilterBlueprint filter) {
                Filterable filterable = typeFilter.getFilterable(actionContext);
                Filterable additionalFilterable = Filters.any;
                if (filter != null)
                    additionalFilterable = filter.getFilterable(actionContext);
                return Filters.filter(cardSource.apply(actionContext), actionContext.getGame(), filterable,
                        additionalFilterable);
            }
        };
    }

    private interface ChoiceActionSource {
        Action createAction(Collection<? extends PhysicalCard> possibleCards, Action action,
                            ActionContext actionContext, int min, int max) throws PlayerNotFoundException;
    }


}