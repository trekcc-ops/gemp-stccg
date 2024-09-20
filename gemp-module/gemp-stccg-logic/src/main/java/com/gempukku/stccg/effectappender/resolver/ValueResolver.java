package com.gempukku.stccg.effectappender.resolver;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.common.filterable.lotr.Keyword;
import com.gempukku.stccg.evaluator.*;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collection;

public class ValueResolver {
    public static ValueSource resolveEvaluator(Object value, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        return resolveEvaluator(value, null, environment);
    }

    public static ValueSource resolveEvaluator(Object value, Integer defaultValue, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        if (value == null && defaultValue == null)
            throw new InvalidCardDefinitionException("Value not defined");
        if (value == null)
            return new ConstantValueSource(defaultValue);
        if (value instanceof Number numValue)
            return new ConstantValueSource(numValue.intValue());
        if (value instanceof String stringValue) {
            if (stringValue.contains("-")) {
                final String[] split = stringValue.split("-", 2);
                final int min = Integer.parseInt(split[0]);
                final int max = Integer.parseInt(split[1]);
                if (min > max || min < 0 || max < 1)
                    throw new InvalidCardDefinitionException("Unable to resolve count: " + value);
                return new ValueSource() {
                    @Override
                    public Evaluator getEvaluator(ActionContext actionContext) {
                        throw new RuntimeException("Evaluator has resolved to range");
                    }

                    @Override
                    public int getMinimum(ActionContext actionContext) {
                        return min;
                    }

                    @Override
                    public int getMaximum(ActionContext actionContext) {
                        return max;
                    }
                };
            } else {
                int v = Integer.parseInt(stringValue);
                return new ConstantValueSource(v);
            }
        }
        if (value instanceof JSONObject object) {
            final String type = environment.getString(object.get("type"), "type");
            if (type.equalsIgnoreCase("range")) {
                environment.validateAllowedFields(object, "from", "to");
                ValueSource fromValue = resolveEvaluator(object.get("from"), environment);
                ValueSource toValue = resolveEvaluator(object.get("to"), environment);
                return new ValueSource() {
                    @Override
                    public Evaluator getEvaluator(ActionContext actionContext) {
                        throw new RuntimeException("Evaluator has resolved to range");
                    }

                    @Override
                    public int getMinimum(ActionContext actionContext) {
                        return fromValue.evaluateExpression(actionContext, null);
                    }

                    @Override
                    public int getMaximum(ActionContext actionContext) {
                        return toValue.evaluateExpression(actionContext, null);
                    }
                };
            } else if (type.equalsIgnoreCase("requires")) {
                environment.validateAllowedFields(object, "requires", "true", "false");
                final Requirement[] conditions = environment.getRequirementsFromJSON(object);
                ValueSource trueValue = resolveEvaluator(object.get("true"), environment);
                ValueSource falseValue = resolveEvaluator(object.get("false"), environment);
                return actionContext -> (Evaluator) new Evaluator(actionContext) {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        if (actionContext.acceptsAllRequirements(conditions)) {
                            return trueValue.evaluateExpression(actionContext, cardAffected);
                        } else {
                            return falseValue.evaluateExpression(actionContext, cardAffected);
                        }
                    }
                };

            } else if (type.equalsIgnoreCase("forEachInMemory")) {
                environment.validateAllowedFields(object, "memory", "limit");
                final String memory = environment.getString(object.get("memory"), "memory");
                final int limit = environment.getInteger(object.get("limit"), "limit", Integer.MAX_VALUE);
                return (actionContext) -> {
                    final int count = actionContext.getCardsFromMemory(memory).size();
                    return new ConstantEvaluator(actionContext, Math.min(limit, count));
                };
            } else if (type.equalsIgnoreCase("forEachMatchingInMemory")) {
                environment.validateAllowedFields(object, "memory", "filter", "limit");
                final String memory = environment.getString(object.get("memory"), "memory");
                final String filter = environment.getString(object.get("filter"), "filter");
                final int limit = environment.getInteger(object.get("limit"), "limit", Integer.MAX_VALUE);
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);
                return (actionContext) -> {
                    final int count = Filters.filter(actionContext.getCardsFromMemory(memory), actionContext.getGame(),
                            filterableSource.getFilterable(actionContext)).size();
                    return new ConstantEvaluator(actionContext, Math.min(limit, count));
                };
            } else if (type.equalsIgnoreCase("forEachKeyword")) {
                environment.validateAllowedFields(object, "filter", "keyword");
                final String filter = environment.getString(object.get("filter"), "filter");
                final Keyword keyword = environment.getEnum(Keyword.class, object.get("keyword"), "keyword");
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);
                return (actionContext) -> new Evaluator(actionContext) {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        int count = 0;
                        for (PhysicalCard physicalCard : Filters.filterActive(actionContext.getGame(), filterableSource.getFilterable(actionContext))) {
                            count += actionContext.getGame().getModifiersQuerying().getKeywordCount(physicalCard, keyword);
                        }
                        return count;
                    }
                };

            } else if (type.equalsIgnoreCase("forEachKeywordOnCardInMemory")) {
                environment.validateAllowedFields(object, "memory", "keyword");
                final String memory = environment.getString(object.get("memory"), "memory");
                final Keyword keyword = environment.getEnum(Keyword.class, object.get("keyword"), "keyword");
                if (keyword == null)
                    throw new InvalidCardDefinitionException("Keyword cannot be null");
                return (actionContext) -> {
                    final DefaultGame game = actionContext.getGame();
                    int count = 0;
                    final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                    for (PhysicalCard cardFromMemory : cardsFromMemory) {
                        count += game.getModifiersQuerying().getKeywordCount(cardFromMemory, keyword);
                    }
                    return new ConstantEvaluator(actionContext, count);
                };
            } else if (type.equalsIgnoreCase("limit")) {
                environment.validateAllowedFields(object, "limit", "value");
                ValueSource limitSource = resolveEvaluator(object.get("limit"), 1, environment);
                ValueSource valueSource = resolveEvaluator(object.get("value"), 0, environment);
                return (actionContext) -> new LimitEvaluator(actionContext, valueSource, limitSource);
            } else if (type.equalsIgnoreCase("cardphaselimit")) {
                environment.validateAllowedFields(object, "limit", "amount");
                ValueSource limitSource = resolveEvaluator(object.get("limit"), 0, environment);
                ValueSource valueSource = resolveEvaluator(object.get("amount"), 0, environment);
                return (actionContext) -> new CardPhaseLimitEvaluator(actionContext, limitSource, valueSource);
            } else if (type.equalsIgnoreCase("countStacked")) {
                environment.validateAllowedFields(object, "on", "filter");
                final String on = environment.getString(object.get("on"), "on");
                final String filter = environment.getString(object.get("filter"), "filter", "any");

                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);
                final FilterableSource onFilter = environment.getFilterFactory().generateFilter(on);

                return (actionContext) -> {
                    final Filterable on1 = onFilter.getFilterable(actionContext);
                    return new CountStackedEvaluator(actionContext.getGame(), on1, filterableSource.getFilterable(actionContext));
                };
            } else if (type.equalsIgnoreCase("forEachInDiscard")) {
                environment.validateAllowedFields(object, "filter", "multiplier", "limit", "player");
                final String filter = environment.getString(object.get("filter"), "filter", "any");
                final int multiplier = environment.getInteger(object.get("multiplier"), "multiplier", 1);
                final int limit = environment.getInteger(object.get("limit"), "limit", Integer.MAX_VALUE);
                final String playerInput = environment.getString(object.get("player"), "player", "you");
                final PlayerSource playerSrc = PlayerResolver.resolvePlayer(playerInput);

                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);
                return actionContext -> new MultiplyEvaluator(actionContext, multiplier, new Evaluator(actionContext) {
                    final String player = playerSrc.getPlayerId(actionContext);
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        final Filterable filterable = filterableSource.getFilterable(actionContext);
                                // Lines below commented out since this code originally counted ALL discard piles
//                        int count = 0;
//                        for (String player : game.getGameState().getPlayerOrder().getAllPlayers())
                        int count = Filters.filter(actionContext.getGame().getGameState().getDiscard(player),
                                actionContext.getGame(), filterable).size();
                        return Math.min(limit, count);
                    }
                });
            } else if (type.equalsIgnoreCase("forEachInHand")) {
                environment.validateAllowedFields(object, "filter", "hand");
                final String filter = environment.getString(object.get("filter"), "filter", "any");
                final String hand = environment.getString(object.get("hand"), "hand", "you");
                final PlayerSource player = PlayerResolver.resolvePlayer(hand);
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);
                return actionContext -> (Evaluator) new Evaluator(actionContext) {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        return Filters.filter(actionContext.getGame().getGameState().getHand(player.getPlayerId(actionContext)),
                                actionContext.getGame(), filterableSource.getFilterable(actionContext)).size();
                    }
                };
            } else if (type.equalsIgnoreCase("forEachInPlayPile")) {
                environment.validateAllowedFields(object, "filter", "owner");
                final String filter = environment.getString(object.get("filter"), "filter", "any");
                final String owner = environment.getString(object.get("owner"), "owner", "you");
                final PlayerSource player = PlayerResolver.resolvePlayer(owner);
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);
                return actionContext -> new Evaluator(actionContext) {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        return Filters.filter(
                                actionContext.getGame().getGameState()
                                        .getZoneCards(player.getPlayerId(actionContext),
                                                Zone.PLAY_PILE),
                                actionContext.getGame(), filterableSource.getFilterable(actionContext)).size();
                    }
                };
            } else if (type.equalsIgnoreCase("countCardsInPlayPile")) {
                environment.validateAllowedFields(object, "owner");
                final String owner = environment.getString(object.get("owner"), "owner", "you");
                final PlayerSource player = PlayerResolver.resolvePlayer(owner);
                return actionContext -> (Evaluator) new Evaluator(actionContext) {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        return actionContext.getZoneCards(player, Zone.PLAY_PILE).size();
                    }
                };
            } else if (type.equalsIgnoreCase("fromMemory")) {
                environment.validateAllowedFields(object, "memory", "multiplier", "limit");
                String memory = environment.getString(object.get("memory"), "memory");
                final int multiplier = environment.getInteger(object.get("multiplier"), "multiplier", 1);
                final int limit = environment.getInteger(object.get("limit"), "limit", Integer.MAX_VALUE);
                return (actionContext) -> {
                    int value1 = Integer.parseInt(actionContext.getValueFromMemory(memory));
                    return new ConstantEvaluator(actionContext, Math.min(limit, multiplier * value1));
                };
            } else if (type.equalsIgnoreCase("multiply")) {
                environment.validateAllowedFields(object, "multiplier", "source");
                final ValueSource multiplier = ValueResolver.resolveEvaluator(object.get("multiplier"), environment);
                final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("source"), 0, environment);
                return (actionContext) -> new MultiplyEvaluator(actionContext, multiplier.getEvaluator(actionContext), valueSource.getEvaluator(actionContext));
            } else if (type.equalsIgnoreCase("cardAffectedLimitPerPhase")) {
                environment.validateAllowedFields(object, "limit", "source", "prefix");
                final int limit = environment.getInteger(object.get("limit"), "limit");
                final String prefix = environment.getString(object.get("prefix"), "prefix", "");
                final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("source"), 0, environment);
                return (actionContext -> new CardAffectedPhaseLimitEvaluator(
                        actionContext, limit, prefix, valueSource.getEvaluator(actionContext)));
            } else if (type.equalsIgnoreCase("forEachStrength")) {
                environment.validateAllowedFields(object, "multiplier", "over", "filter");
                final int multiplier = environment.getInteger(object.get("multiplier"), "multiplier", 1);
                final int over = environment.getInteger(object.get("over"), "over", 0);
                final String filter = environment.getString(object.get("filter"), "filter", "any");

                final FilterableSource strengthSource = environment.getFilterFactory().generateFilter(filter);

                return (actionContext) -> {
                    if (filter.equals("any")) {
                        return new MultiplyEvaluator(actionContext, multiplier,
                                new Evaluator(actionContext) {
                                    @Override
                                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                                        return Math.max(0, actionContext.getGame().getModifiersQuerying().getStrength(cardAffected) - over);
                                    }
                                });
                    } else {
                        return new MultiplyEvaluator(actionContext, multiplier,
                                new Evaluator(actionContext) {
                                    @Override
                                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                                        final Filterable filterable = strengthSource.getFilterable(actionContext);
                                        int strength = 0;
                                        for (PhysicalCard physicalCard : Filters.filterActive(actionContext.getGame(), filterable)) {
                                            strength += actionContext.getGame().getModifiersQuerying().getStrength(physicalCard);
                                        }

                                        return Math.max(0, strength - over);
                                    }
                                });
                    }
                };
            } else if (type.equalsIgnoreCase("printedStrengthFromMemory")) {
                environment.validateAllowedFields(object, "memory");
                final String memory = environment.getString(object.get("memory"), "memory");

                return actionContext -> (Evaluator) new Evaluator(actionContext) {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        int result = 0;
                        for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(memory)) {
                            result += physicalCard.getBlueprint().getAttribute(CardAttribute.STRENGTH);
                        }
                        return result;
                    }
                };
            } else if (type.equalsIgnoreCase("strengthFromMemory")) {
                environment.validateAllowedFields(object, "memory");
                final String memory = environment.getString(object.get("memory"), "memory");

                return actionContext -> (Evaluator) new Evaluator(actionContext) {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        int result = 0;
                        for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(memory)) {
                            result += actionContext.getGame().getModifiersQuerying().getStrength(physicalCard);
                        }
                        return result;
                    }
                };
            } else if (type.equalsIgnoreCase("tribbleValueFromMemory")) {
                environment.validateAllowedFields(object, "memory");
                final String memory = environment.getString(object.get("memory"), "memory");

                return actionContext -> (Evaluator) new Evaluator(actionContext) {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        int result = 0;
                        for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(memory)) {
                            result += physicalCard.getBlueprint().getTribbleValue();
                        }
                        return result;
                    }
                };
            }
            else if (type.equalsIgnoreCase("subtract")) {
                environment.validateAllowedFields(object, "firstNumber", "secondNumber");
                final ValueSource firstNumber = ValueResolver.resolveEvaluator(object.get("firstNumber"), 0, environment);
                final ValueSource secondNumber = ValueResolver.resolveEvaluator(object.get("secondNumber"), 0, environment);
                return actionContext -> (Evaluator) new Evaluator(actionContext) {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        final int first = firstNumber.evaluateExpression(actionContext, null);
                        final int second = secondNumber.evaluateExpression(actionContext, null);
                        return first - second;
                    }
                };
            } else if (type.equalsIgnoreCase("sum")) {
                final JSONArray sourceArray;
                environment.validateAllowedFields(object, "source");

                if (object.get("source") == null)
                    sourceArray = new JSONArray();
                else if (object.get("source") instanceof JSONObject) {
                    sourceArray = new JSONArray();
                    sourceArray.add(object.get("source"));
                } else if (object.get("source") instanceof JSONArray)
                    sourceArray = (JSONArray) object.get("source");
                else throw new InvalidCardDefinitionException("Unknown type in source field");

                ValueSource[] sources = new ValueSource[sourceArray.size()];
                for (int i = 0; i < sources.length; i++)
                    sources[i] = ValueResolver.resolveEvaluator(sourceArray.get(i), 0, environment);

                return actionContext -> new Evaluator(actionContext) {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        int sum = 0;
                        for (ValueSource source : sources)
                            sum += source.evaluateExpression(actionContext, cardAffected);

                        return sum;
                    }
                };
            } else if (type.equalsIgnoreCase("twilightCostInMemory")) {
                environment.validateAllowedFields(object, "multiplier", "memory");
                final int multiplier = environment.getInteger(object.get("multiplier"), "multiplier", 1);
                final String memory = environment.getString(object.get("memory"), "memory");
                return actionContext -> (Evaluator) new Evaluator(actionContext) {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        int total = 0;
                        for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(memory)) {
                            total += physicalCard.getBlueprint().getTwilightCost();
                        }
                        return multiplier * total;
                    }
                };
            } else if (type.equalsIgnoreCase("maxOfSpecies")) {
                environment.validateAllowedFields(object, "filter");
                final String filter = environment.getString(object.get("filter"), "filter");

                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);

                return actionContext -> new Evaluator(actionContext) {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        int result = 0;
                        final Filterable filterable = filterableSource.getFilterable(actionContext);
                        for (Species species : Species.values())
                            result = Math.max(result, Filters.countSpottable(actionContext.getGame(), species, filterable));

                        return result;
                    }
                };
            } else if (type.equalsIgnoreCase("max")) {
                environment.validateAllowedFields(object, "first", "second");
                ValueSource first = resolveEvaluator(object.get("first"), environment);
                ValueSource second = resolveEvaluator(object.get("second"), environment);

                return actionContext -> new Evaluator(actionContext) {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        return Math.max(
                                first.evaluateExpression(actionContext, null),
                                second.evaluateExpression(actionContext, null)
                        );
                    }
                };
            } else if (type.equalsIgnoreCase("min")) {
                environment.validateAllowedFields(object, "first", "second");
                ValueSource first = resolveEvaluator(object.get("first"), environment);
                ValueSource second = resolveEvaluator(object.get("second"), environment);

                return actionContext -> new Evaluator(actionContext) {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        return Math.min(
                                first.evaluateExpression(actionContext, null),
                                second.evaluateExpression(actionContext, null)
                        );
                    }
                };
            }
            throw new InvalidCardDefinitionException("Unrecognized type of an evaluator " + type);
        }
        throw new InvalidCardDefinitionException("Unable to resolve an evaluator");
    }
}
