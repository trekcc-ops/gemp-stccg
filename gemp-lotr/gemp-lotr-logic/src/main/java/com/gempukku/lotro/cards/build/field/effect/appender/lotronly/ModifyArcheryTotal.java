package com.gempukku.lotro.cards.build.field.effect.appender.lotronly;

import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.appender.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.DelayedAppender;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.modifiers.lotronly.ArcheryTotalModifier;
import org.json.simple.JSONObject;

public class ModifyArcheryTotal implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "amount", "side");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("amount"), environment);
        final Side side = FieldUtils.getSide(effectObject.get("side"), "side");

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                final Evaluator evaluator = valueSource.getEvaluator(actionContext);
                final int amount = evaluator.evaluateExpression(actionContext.getGame(), null);
                return new AddUntilEndOfPhaseModifierEffect(
                        new ArcheryTotalModifier(actionContext.getSource(), side, amount));
            }
        };
    }

}
