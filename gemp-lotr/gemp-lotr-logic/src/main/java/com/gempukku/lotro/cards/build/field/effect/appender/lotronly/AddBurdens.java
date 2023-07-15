package com.gempukku.lotro.cards.build.field.effect.appender.lotronly;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.DelayedAppender;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.game.effects.AddBurdenEffect;
import com.gempukku.lotro.game.effects.Effect;
import com.gempukku.lotro.game.timing.PlayConditions;
import org.json.simple.JSONObject;

public class AddBurdens implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "amount", "player");

        final ValueSource amountSource = ValueResolver.resolveEvaluator(effectObject.get("amount"), 1, environment);
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player, environment);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String playerAddingBurden = playerSource.getPlayer(actionContext);
                final int amount = amountSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                return new AddBurdenEffect(playerAddingBurden, actionContext.getSource(), amount);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String playerAddingBurden = playerSource.getPlayer(actionContext);
                return PlayConditions.canAddBurdens(actionContext.getGame(), playerAddingBurden, actionContext.getSource());
            }
        };
    }

}