package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.filters.FilterFactory;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.requirement.Requirement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ValueSourceDeserializer extends StdDeserializer<ValueSource> {

    public ValueSourceDeserializer() {
        this(null);
    }

    public ValueSourceDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ValueSource deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode object = jp.getCodec().readTree(jp);
        if (object != null)
            return resolveEvaluator(ctxt, object);
        else throw new InvalidCardDefinitionException("Null value source");
    }

    private static ValueSource resolveEvaluator(DeserializationContext ctxt, JsonNode node, int defaultValue)
            throws IOException {
        return Objects.requireNonNullElse(resolveEvaluator(ctxt, node), new ConstantValueSource(defaultValue));
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

    public static ValueSource resolveEvaluator(DeserializationContext ctxt, JsonNode value)
            throws IOException {
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
                ValueSource fromValue = resolveEvaluator(ctxt, object.get("from"));
                ValueSource toValue = resolveEvaluator(ctxt, object.get("to"));
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
                        try {
                            String playerId = player.getPlayerId(actionContext);
                            Player playerObj = game.getPlayer(playerId);
                            return actionContext.getZoneCards(playerObj, Zone.PLAY_PILE).size();
                        } catch(PlayerNotFoundException exp) {
                            game.sendErrorMessage(exp);
                            return 0;
                        }
                    }
                };
            } else if (type.equalsIgnoreCase("requires")) {
                BlueprintUtils.validateAllowedFields(object, "requires", "true", "false");
                JsonNode requiresArray = object.get("requires");
                List<Requirement> conditions = new ArrayList<>();
                if (requiresArray.isArray()) {
                    for (JsonNode requirement : requiresArray) {
                        conditions.add(ctxt.readTreeAsValue(requirement, Requirement.class));
                    }
                } else {
                    conditions.add(ctxt.readTreeAsValue(requiresArray, Requirement.class));
                }
                ValueSource trueValue = resolveEvaluator(ctxt, object.get("true"));
                ValueSource falseValue = resolveEvaluator(ctxt, object.get("false"));
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
                final FilterBlueprint filterBlueprint = BlueprintUtils.getFilterable(object);
                return (actionContext) -> {
                    final int count = Filters.filter(actionContext.getCardsFromMemory(memory), actionContext.getGame(),
                            filterBlueprint.getFilterable(actionContext)).size();
                    return new ConstantEvaluator(actionContext, Math.min(limit, count));
                };
            } else if (type.equalsIgnoreCase("limit")) {
                BlueprintUtils.validateAllowedFields(object, "limit", "value");
                ValueSource limitSource = resolveEvaluator(ctxt, object.get("limit"), 1);
                ValueSource valueSource = resolveEvaluator(ctxt, object.get("value"), 0);
                return (actionContext) -> new LimitEvaluator(actionContext, valueSource, limitSource);
            } else if (type.equalsIgnoreCase("countStacked")) {
                BlueprintUtils.validateAllowedFields(object, "on", "filter");
                final FilterBlueprint filterBlueprint = BlueprintUtils.getFilterable(object, "any");
                final FilterBlueprint onFilter =
                        new FilterFactory().generateFilter(object.get("on").textValue());
                return (actionContext) ->
                        new CountStackedEvaluator(onFilter.getFilterable(actionContext),
                                filterBlueprint.getFilterable(actionContext));
            } else if (type.equalsIgnoreCase("forEachInDiscard")) {
                return ctxt.readTreeAsValue(object, CountDiscardEvaluator.class);
            } else if (type.equalsIgnoreCase("forEachInHand")) {
                BlueprintUtils.validateAllowedFields(object, "filter", "hand");
                final PlayerSource player =
                        PlayerResolver.resolvePlayer(BlueprintUtils.getString(object, "hand", "you"));
                final FilterBlueprint filterBlueprint = BlueprintUtils.getFilterable(object, "any");
                return actionContext -> (Evaluator) new Evaluator() {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        try {
                            String playerId = player.getPlayerId(actionContext);
                            Player playerObj = game.getPlayer(playerId);
                            return Filters.filter(playerObj.getCardsInHand(),
                                    actionContext.getGame(), filterBlueprint.getFilterable(actionContext)).size();
                        } catch(PlayerNotFoundException exp) {
                            game.sendErrorMessage(exp);
                            return 0;
                        }
                    }
                };
            } else if (type.equalsIgnoreCase("forEachInPlayPile")) {
                BlueprintUtils.validateAllowedFields(object, "filter", "owner");
                final String owner = BlueprintUtils.getString(object, "owner", "you");
                final PlayerSource playerSource = PlayerResolver.resolvePlayer(owner);
                final FilterBlueprint filterBlueprint = BlueprintUtils.getFilterable(object, "any");
                return actionContext -> new Evaluator() {
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        try {
                            String playerId = playerSource.getPlayerId(actionContext);
                            Player player = game.getPlayer(playerId);
                            Collection<PhysicalCard> cards = Filters.filter(
                                    player.getCardsInGroup(Zone.PLAY_PILE), game,
                                    filterBlueprint.getFilterable(actionContext)
                            );
                            return cards.size();
                        } catch(PlayerNotFoundException exp) {
                            game.sendErrorMessage(exp);
                            return 0;
                        }
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
                final ValueSource multiplier = resolveEvaluator(ctxt, object.get("multiplier"));
                final ValueSource valueSource = resolveEvaluator(ctxt, object.get("source"), 0);
                return (actionContext) -> new MultiplyEvaluator(actionContext, multiplier.getEvaluator(actionContext), valueSource.getEvaluator(actionContext));
            } else if (type.equalsIgnoreCase("forEachStrength")) {
                BlueprintUtils.validateAllowedFields(object, "multiplier", "over", "filter");
                final int multiplier = BlueprintUtils.getInteger(object, "multiplier", 1);
                final int over = BlueprintUtils.getInteger(object, "over", 0);
                final String filter = BlueprintUtils.getString(object, "filter", "any");
                final FilterBlueprint strengthSource = BlueprintUtils.getFilterable(object, "any");

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
                        resolveEvaluator(ctxt, object.get("firstNumber"), 0);
                final ValueSource secondNumber =
                        resolveEvaluator(ctxt, object.get("secondNumber"), 0);
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
                ValueSource first = resolveEvaluator(ctxt, object.get("first"));
                ValueSource second = resolveEvaluator(ctxt, object.get("second"));

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
                ValueSource first = resolveEvaluator(ctxt, object.get("first"));
                ValueSource second = resolveEvaluator(ctxt, object.get("second"));

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