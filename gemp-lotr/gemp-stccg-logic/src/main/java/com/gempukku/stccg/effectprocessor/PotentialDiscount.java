package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.discard.DiscardCardFromHandDiscountEffect;
import com.gempukku.stccg.actions.discard.OptionalDiscardDiscountEffect;
import com.gempukku.stccg.actions.discard.RemoveCardsFromDiscardDiscountEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.actions.discard.DiscountEffect;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.common.filterable.Filterable;
import org.json.simple.JSONObject;

public class PotentialDiscount implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "max", "discount", "memorize");

        final ValueSource maxSource = ValueResolver.resolveEvaluator(value.get("max"), 1000, environment);
        final JSONObject discount = (JSONObject) value.get("discount");
        final String memory = environment.getString(value.get("memorize"), "memorize", "_temp");

        final String discountType = environment.getString(discount.get("type"), "type");
        if (discountType.equalsIgnoreCase("perDiscardFromHand")) {
            environment.validateAllowedFields(discount, "filter");

            final String filter = environment.getString(discount.get("filter"), "filter", "any");
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);

            blueprint.appendDiscountSource(
                new DiscountSource() {
                    @Override
                    public int getPotentialDiscount(ActionContext actionContext) {
                        return maxSource.evaluateExpression(actionContext, actionContext.getSource());
                    }

                    @Override
                    public DiscountEffect getDiscountEffect(CostToEffectAction action, ActionContext actionContext) {
                        final Filterable filterable = filterableSource.getFilterable(actionContext);
                        return new DiscardCardFromHandDiscountEffect(actionContext, action, filterable) {
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
            environment.validateAllowedFields(discount, "count", "filter");

            final ValueSource discardCountSource = ValueResolver.resolveEvaluator(discount.get("count"), environment);
            final String filter = environment.getString(discount.get("filter"), "filter", "any");
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);

            blueprint.appendDiscountSource(
                new DiscountSource() {
                    @Override
                    public int getPotentialDiscount(ActionContext actionContext) {
                        return maxSource.evaluateExpression(actionContext, actionContext.getSource());
                    }

                    @Override
                    public DiscountEffect getDiscountEffect(CostToEffectAction action, ActionContext actionContext) {
                        final Filterable filterable = filterableSource.getFilterable(actionContext);
                        final int max = maxSource.evaluateExpression(actionContext);
                        final int discardCount = discardCountSource.evaluateExpression(actionContext);
                        actionContext.setValueToMemory(memory, "No");
                        return new OptionalDiscardDiscountEffect(actionContext, action, max, discardCount, filterable) {
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
            environment.validateAllowedFields(discount, "count", "filter");

            final ValueSource removeCountSource = ValueResolver.resolveEvaluator(value.get("count"), environment);
            final String filter = environment.getString(discount.get("filter"), "filter", "any");
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);

            blueprint.appendDiscountSource(
                new DiscountSource() {
                    @Override
                    public int getPotentialDiscount(ActionContext actionContext) {
                        return maxSource.evaluateExpression(actionContext);
                    }

                    @Override
                    public DiscountEffect getDiscountEffect(CostToEffectAction action, ActionContext actionContext) {
                        final Filterable filterable = filterableSource.getFilterable(actionContext);
                        final int max = maxSource.evaluateExpression(actionContext);
                        final int removeCount = removeCountSource.evaluateExpression(actionContext);
                        actionContext.setValueToMemory(memory, "No");
                        return new RemoveCardsFromDiscardDiscountEffect(actionContext, removeCount, max, filterable) {
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
