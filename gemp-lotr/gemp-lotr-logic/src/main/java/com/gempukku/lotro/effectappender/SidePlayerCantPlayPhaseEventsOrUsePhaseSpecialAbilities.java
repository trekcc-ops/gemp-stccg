package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.requirement.Requirement;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.TimeResolver;
import com.gempukku.lotro.modifiers.RequirementCondition;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.effects.AddUntilModifierEffect;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.modifiers.SidePlayerCantPlayPhaseEventsOrSpecialAbilitiesModifier;
import org.json.simple.JSONObject;

public class SidePlayerCantPlayPhaseEventsOrUsePhaseSpecialAbilities implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "requires", "side", "phase", "until");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(effectObject.get("requires"), "requires");
        final Side side = FieldUtils.getEnum(Side.class, effectObject.get("side"), "side");
        final Phase phase = FieldUtils.getEnum(Phase.class, effectObject.get("phase"), "phase");
        final TimeResolver.Time until = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");

        final Requirement[] conditions = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                return new AddUntilModifierEffect(
                        new SidePlayerCantPlayPhaseEventsOrSpecialAbilitiesModifier(actionContext.getSource(),
                                new RequirementCondition(conditions, actionContext),
                                side, phase), until);
            }
        };
    }
}
