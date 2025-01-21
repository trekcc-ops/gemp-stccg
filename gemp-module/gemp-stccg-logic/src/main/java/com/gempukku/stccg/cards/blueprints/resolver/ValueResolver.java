package com.gempukku.stccg.cards.blueprints.resolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.ConstantValueSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.blueprints.requirement.RequirementFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.evaluator.*;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class ValueResolver {

    public static ValueSource resolveEvaluator(JsonNode value)
            throws InvalidCardDefinitionException {
        return resolveEvaluator(value, null);
    }

    public static ValueSource resolveEvaluator(String stringValue) throws InvalidCardDefinitionException {
        if (stringValue.contains("-")) {
            final String[] split = stringValue.split("-", 2);
            final int min = Integer.parseInt(split[0]);
            final int max = Integer.parseInt(split[1]);
            if (min > max || min < 0 || max < 1)
                throw new InvalidCardDefinitionException("Unable to resolve count: " + stringValue);
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
        } else
            return new ConstantValueSource(Integer.parseInt(stringValue));
    }

    public static ValueSource resolveEvaluator(JsonNode value, Integer defaultValue)
            throws InvalidCardDefinitionException {
        if (value == null && defaultValue == null)
            throw new InvalidCardDefinitionException("Value not defined");
        if (value == null)
            return new ConstantValueSource(defaultValue);
        if (value.isInt())
            return new ConstantValueSource(value.asInt());
        if (value.isTextual())
            return resolveEvaluator(value.textValue());
        if (value instanceof JsonNode object) {
            final String type = BlueprintUtils.getString(object, "type");
            if (type == null)
                throw new InvalidCardDefinitionException("ValueResolver type not defined");
            if (type.equalsIgnoreCase("range")) {
                BlueprintUtils.validateAllowedFields(object, "from", "to");
                ValueSource fromValue = resolveEvaluator(object.get("from"));
                ValueSource toValue = resolveEvaluator(object.get("to"));
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
            } else if (type.equalsIgnoreCase("countCardsInPlayPile")) {
                BlueprintUtils.validateAllowedFields(object, "owner");
                final PlayerSource player =
                        PlayerResolver.resolvePlayer(BlueprintUtils.getString(object, "owner", "you"));
                return actionContext -> (Evaluator) new Evaluator() {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        return actionContext.getZoneCards(player, Zone.PLAY_PILE).size();
                    }
                };
            } else if (type.equalsIgnoreCase("requires")) {
                BlueprintUtils.validateAllowedFields(object, "requires", "true", "false");
                final Requirement[] conditions = RequirementFactory.getRequirements(object);
                ValueSource trueValue = resolveEvaluator(object.get("true"));
                ValueSource falseValue = resolveEvaluator(object.get("false"));
                return actionContext -> (Evaluator) new Evaluator() {
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
                BlueprintUtils.validateAllowedFields(object, "memory", "limit");
                final String memory = object.get("memory").textValue();
                final int limit = BlueprintUtils.getInteger(object, "limit", Integer.MAX_VALUE);
                return (actionContext) -> {
                    final int count = actionContext.getCardsFromMemory(memory).size();
                    return new ConstantEvaluator(actionContext, Math.min(limit, count));
                };
            } else if (type.equalsIgnoreCase("forEachMatchingInMemory")) {
                BlueprintUtils.validateAllowedFields(object, "memory", "filter", "limit");
                final String memory = object.get("memory").textValue();
                final int limit = BlueprintUtils.getInteger(object, "limit", Integer.MAX_VALUE);
                final FilterableSource filterableSource = BlueprintUtils.getFilterable(object);
                return (actionContext) -> {
                    final int count = Filters.filter(actionContext.getCardsFromMemory(memory), actionContext.getGame(),
                            filterableSource.getFilterable(actionContext)).size();
                    return new ConstantEvaluator(actionContext, Math.min(limit, count));
                };
            } else if (type.equalsIgnoreCase("limit")) {
                BlueprintUtils.validateAllowedFields(object, "limit", "value");
                ValueSource limitSource = resolveEvaluator(object.get("limit"), 1);
                ValueSource valueSource = resolveEvaluator(object.get("value"), 0);
                return (actionContext) -> new LimitEvaluator(actionContext, valueSource, limitSource);
            } else if (type.equalsIgnoreCase("countStacked")) {
                BlueprintUtils.validateAllowedFields(object, "on", "filter");
                final FilterableSource filterableSource = BlueprintUtils.getFilterable(object, "any");
                final FilterableSource onFilter =
                        new FilterFactory().generateFilter(object.get("on").textValue());
                return (actionContext) ->
                        new CountStackedEvaluator(onFilter.getFilterable(actionContext), filterableSource.getFilterable(actionContext));
            } else if (type.equalsIgnoreCase("forEachInDiscard")) {
                BlueprintUtils.validateAllowedFields(object, "filter", "multiplier", "limit", "player");
                final int multiplier = BlueprintUtils.getInteger(object, "multiplier", 1);
                final int limit = BlueprintUtils.getInteger(object, "limit", Integer.MAX_VALUE);
                final String playerInput = BlueprintUtils.getString(object, "player", "you");
                final PlayerSource playerSrc = PlayerResolver.resolvePlayer(playerInput);
                final FilterableSource filterableSource = BlueprintUtils.getFilterable(object, "any");
                return actionContext -> new MultiplyEvaluator(actionContext, multiplier, new Evaluator() {
                    final String player = playerSrc.getPlayerId(actionContext);
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        final Filterable filterable = filterableSource.getFilterable(actionContext);
                        int count = Filters.filter(actionContext.getGame().getGameState().getDiscard(player),
                                actionContext.getGame(), filterable).size();
                        return Math.min(limit, count);
                    }
                });
            } else if (type.equalsIgnoreCase("forEachInHand")) {
                BlueprintUtils.validateAllowedFields(object, "filter", "hand");
                final PlayerSource player =
                        PlayerResolver.resolvePlayer(BlueprintUtils.getString(object, "hand", "you"));
                final FilterableSource filterableSource = BlueprintUtils.getFilterable(object, "any");
                return actionContext -> (Evaluator) new Evaluator() {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        return Filters.filter(actionContext.getGame().getGameState().getHand(player.getPlayerId(actionContext)),
                                actionContext.getGame(), filterableSource.getFilterable(actionContext)).size();
                    }
                };
            } else if (type.equalsIgnoreCase("forEachInPlayPile")) {
                BlueprintUtils.validateAllowedFields(object, "filter", "owner");
                final String owner = BlueprintUtils.getString(object, "owner", "you");
                final PlayerSource player = PlayerResolver.resolvePlayer(owner);
                final FilterableSource filterableSource = BlueprintUtils.getFilterable(object, "any");
                return actionContext -> new Evaluator() {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        return Filters.filter(
                                actionContext.getGame().getGameState()
                                        .getZoneCards(player.getPlayerId(actionContext),
                                                Zone.PLAY_PILE),
                                actionContext.getGame(), filterableSource.getFilterable(actionContext)).size();
                    }
                };
            } else if (type.equalsIgnoreCase("fromMemory")) {
                BlueprintUtils.validateAllowedFields(object, "memory", "multiplier", "limit");
                String memory = object.get("memory").textValue();
                final int multiplier = BlueprintUtils.getInteger(object, "multiplier", 1);
                final int limit = BlueprintUtils.getInteger(object, "limit", Integer.MAX_VALUE);
                return (actionContext) -> {
                    int value1 = Integer.parseInt(actionContext.getValueFromMemory(memory));
                    return new ConstantEvaluator(actionContext, Math.min(limit, multiplier * value1));
                };
            } else if (type.equalsIgnoreCase("multiply")) {
                BlueprintUtils.validateAllowedFields(object, "multiplier", "source");
                final ValueSource multiplier = ValueResolver.resolveEvaluator(object.get("multiplier"));
                final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("source"), 0);
                return (actionContext) -> new MultiplyEvaluator(actionContext, multiplier.getEvaluator(actionContext), valueSource.getEvaluator(actionContext));
            } else if (type.equalsIgnoreCase("forEachStrength")) {
                BlueprintUtils.validateAllowedFields(object, "multiplier", "over", "filter");
                final int multiplier = BlueprintUtils.getInteger(object, "multiplier", 1);
                final int over = BlueprintUtils.getInteger(object, "over", 0);
                final String filter = BlueprintUtils.getString(object, "filter", "any");
                final FilterableSource strengthSource = BlueprintUtils.getFilterable(object, "any");

                return (actionContext) -> {
                    if (filter.equals("any")) {
                        return new MultiplyEvaluator(actionContext, multiplier,
                                new Evaluator() {
                                    @Override
                                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                                        return Math.max(0, actionContext.getGame().getModifiersQuerying().getStrength(cardAffected) - over);
                                    }
                                });
                    } else {
                        return new MultiplyEvaluator(actionContext, multiplier,
                                new Evaluator() {
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
                BlueprintUtils.validateAllowedFields(object, "memory");

                return actionContext -> (Evaluator) new Evaluator() {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        int result = 0;
                        for (PhysicalCard physicalCard :
                                actionContext.getCardsFromMemory(object.get("memory").textValue())) {
                            result += physicalCard.getBlueprint().getStrength();
                        }
                        return result;
                    }
                };
            } else if (type.equalsIgnoreCase("strengthFromMemory")) {
                BlueprintUtils.validateAllowedFields(object, "memory");
                final String memory = object.get("memory").textValue();

                return actionContext -> (Evaluator) new Evaluator() {
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
                BlueprintUtils.validateAllowedFields(object, "memory");
                final String memory = object.get("memory").textValue();

                return actionContext -> (Evaluator) new Evaluator() {
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
                BlueprintUtils.validateAllowedFields(object, "firstNumber", "secondNumber");
                final ValueSource firstNumber =
                        ValueResolver.resolveEvaluator(object.get("firstNumber"), 0);
                final ValueSource secondNumber =
                        ValueResolver.resolveEvaluator(object.get("secondNumber"), 0);
                return actionContext -> (Evaluator) new Evaluator() {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        final int first = firstNumber.evaluateExpression(actionContext, null);
                        final int second = secondNumber.evaluateExpression(actionContext, null);
                        return first - second;
                    }
                };
            } else if (type.equalsIgnoreCase("max")) {
                BlueprintUtils.validateAllowedFields(object, "first", "second");
                ValueSource first = resolveEvaluator(object.get("first"));
                ValueSource second = resolveEvaluator(object.get("second"));

                return actionContext -> new Evaluator() {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        return Math.max(
                                first.evaluateExpression(actionContext, null),
                                second.evaluateExpression(actionContext, null)
                        );
                    }
                };
            } else if (type.equalsIgnoreCase("min")) {
                BlueprintUtils.validateAllowedFields(object, "first", "second");
                ValueSource first = resolveEvaluator(object.get("first"));
                ValueSource second = resolveEvaluator(object.get("second"));

                return actionContext -> new Evaluator() {
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