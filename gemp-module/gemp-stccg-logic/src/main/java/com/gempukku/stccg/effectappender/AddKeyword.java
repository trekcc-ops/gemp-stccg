package com.gempukku.stccg.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Keyword;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.TimeResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.AddUntilModifierEffect;
import com.gempukku.stccg.modifiers.KeywordModifier;
import com.gempukku.stccg.TextUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class AddKeyword implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node,
                "count", "filter", "memorize", "keyword", "amount", "until");

        final ValueSource valueSource =
                ValueResolver.resolveEvaluator(node.get("count"), 1, environment);
        final String filter = node.get("filter").textValue();
        final String memory = environment.getString(node, "memorize", "_temp");
        final String keywordString = node.get("keyword").textValue();
        final TimeResolver.Time until = TimeResolver.resolveTime(node.get("until"), "end(current)");

        Function<ActionContext, Keyword> keywordFunction;
        ValueSource amount;
        if (keywordString.startsWith("fromMemory(") && keywordString.endsWith(")")) {
            String keywordMemory =
                    keywordString.substring(keywordString.indexOf("(") + 1, keywordString.lastIndexOf(")"));
            keywordFunction = actionContext -> Keyword.valueOf(actionContext.getValueFromMemory(keywordMemory));
            amount = new ConstantValueSource(1);
        } else {
            final String[] keywordSplit = keywordString.split("\\+");
            Keyword keyword = environment.getEnum(Keyword.class, keywordSplit[0], "keyword");
            keywordFunction = (actionContext) -> keyword;
            int value = 1;
            if (keywordSplit.length == 2)
                value = Integer.parseInt(keywordSplit[1]);

            amount = ValueResolver.resolveEvaluator(node.get("amount"), value, environment);
        }

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(CardResolver.resolveCards(filter, valueSource, memory, "you",
                "Choose cards to add keyword to", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        List<Effect> result = new LinkedList<>();
                        final Collection<? extends PhysicalCard> cardsFromMemory =
                                actionContext.getCardsFromMemory(memory);
                        for (PhysicalCard physicalCard : cardsFromMemory) {
                            final int keywordCount = amount.evaluateExpression(actionContext, physicalCard);
                            result.add(new AddUntilModifierEffect(actionContext.getGame(),
                                    new KeywordModifier(actionContext, physicalCard,
                                            keywordFunction.apply(actionContext), keywordCount), until));
                        }

                        actionContext.getGame().sendMessage(
                                actionContext.getSource().getCardLink()
                                        + " adds " + keywordFunction.apply(actionContext).getHumanReadableGeneric()
                                        + " to " + TextUtils.getConcatenatedCardLinks(cardsFromMemory)
                                        + " until " + until.getHumanReadable());
                        return result;
                    }
                });

        return result;
    }

}
