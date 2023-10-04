package com.gempukku.lotro.effectprocessor;

import com.gempukku.lotro.cards.DefaultActionContext;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.effects.discount.*;
import com.gempukku.lotro.game.DefaultGame;
import org.json.simple.JSONObject;

public class PotentialDiscount implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "max", "discount", "memorize");

        final ValueSource maxSource = ValueResolver.resolveEvaluator(value.get("max"), 1000, environment);
        final JSONObject discount = (JSONObject) value.get("discount");
        final String memory = FieldUtils.getString(value.get("memorize"), "memorize", "_temp");

        final String discountType = FieldUtils.getString(discount.get("type"), "type");
        if (discountType.equalsIgnoreCase("perDiscardFromHand")) {
            FieldUtils.validateAllowedFields(discount, "filter");

            final String filter = FieldUtils.getString(discount.get("filter"), "filter", "any");
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

            blueprint.appendDiscountSource(
                new DiscountSource() {
                    @Override
                    public int getPotentialDiscount(DefaultActionContext<DefaultGame> actionContext) {
                        return maxSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                    }

                    @Override
                    public DiscountEffect getDiscountEffect(CostToEffectAction action, DefaultActionContext actionContext) {
                        final Filterable filterable = filterableSource.getFilterable(actionContext);
                        return new DiscardCardFromHandDiscountEffect(action, actionContext.getPerformingPlayer(), filterable) {
                            @Override
                            protected void discountPaidCallback(int paid) {
                                actionContext.setValueToMemory(memory, String.valueOf(paid));
                                actionContext.getSource().setWhileInZoneData(paid);
                            }
                        };
                    }
                });
        }
        else if (discountType.equalsIgnoreCase("ifDiscardFromPlay")) {
            FieldUtils.validateAllowedFields(discount, "count", "filter");

            final ValueSource discardCountSource = ValueResolver.resolveEvaluator(discount.get("count"), environment);
            final String filter = FieldUtils.getString(discount.get("filter"), "filter", "any");
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

            blueprint.appendDiscountSource(
                new DiscountSource() {
                    @Override
                    public int getPotentialDiscount(DefaultActionContext<DefaultGame> actionContext) {
                        return maxSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                    }

                    @Override
                    public DiscountEffect getDiscountEffect(CostToEffectAction action, DefaultActionContext actionContext) {
                        final Filterable filterable = filterableSource.getFilterable(actionContext);
                        final int max = maxSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                        final int discardCount = discardCountSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                        actionContext.setValueToMemory(memory, "No");
                        return new OptionalDiscardDiscountEffect(action, max, actionContext.getPerformingPlayer(), discardCount, filterable) {
                            @Override
                            protected void discountPaidCallback(int paid) {
                                actionContext.setValueToMemory(memory, "Yes");
                                actionContext.getSource().setWhileInZoneData(memory);
                            }
                        };
                    }
                });
        }
        else if (discountType.equalsIgnoreCase("ifRemoveFromDiscard")) {
            FieldUtils.validateAllowedFields(discount, "count", "filter");

            final ValueSource removeCountSource = ValueResolver.resolveEvaluator(value.get("count"), environment);
            final String filter = FieldUtils.getString(discount.get("filter"), "filter", "any");
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

            blueprint.appendDiscountSource(
                new DiscountSource() {
                    @Override
                    public int getPotentialDiscount(DefaultActionContext<DefaultGame> actionContext) {
                        return maxSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                    }

                    @Override
                    public DiscountEffect getDiscountEffect(CostToEffectAction action, DefaultActionContext actionContext) {
                        final Filterable filterable = filterableSource.getFilterable(actionContext);
                        final int max = maxSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                        final int removeCount = removeCountSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                        actionContext.setValueToMemory(memory, "No");
                        return new RemoveCardsFromDiscardDiscountEffect(actionContext.getSource(), actionContext.getPerformingPlayer(), removeCount, max, filterable) {
                            @Override
                            protected void discountPaidCallback(int paid) {
                                actionContext.setValueToMemory(memory, "Yes");
                                actionContext.getSource().setWhileInZoneData(memory);
                            }
                        };
                    }
                });
        }
        else {
            throw new InvalidCardDefinitionException("Unknown type of discount: " + discountType);
        }
    }
}
