package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;

import java.util.List;

public class CostToEffect implements EffectAppenderProducer {
    @Override
    public EffectBlueprint createEffectAppender(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "cost", "effect", "requires");

        final List<EffectBlueprint> costAppenders = environment.getEffectAppendersFromJSON(node.get("cost"));
        final List<EffectBlueprint> effectBlueprints = environment.getEffectAppendersFromJSON(node.get("effect"));
        final Requirement[] requirements = environment.getRequirementsFromJSON(node);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {

                if(requirementsNotMet(context))
                    return null;

                SubAction subAction = action.createSubAction();

                for (EffectBlueprint costAppender : costAppenders)
                    costAppender.appendEffect(true, subAction, context);
                for (EffectBlueprint effectBlueprint : effectBlueprints)
                    effectBlueprint.appendEffect(false, subAction, context);

                return new StackActionEffect(context.getGame(), subAction);
            }

            private boolean requirementsNotMet(ActionContext actionContext) {
                return (!actionContext.acceptsAllRequirements(requirements));
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {

                if(requirementsNotMet(actionContext))
                    return false;

                for (EffectBlueprint costAppender : costAppenders) {
                    if (!costAppender.isPlayableInFull(actionContext))
                        return false;
                }

                for (EffectBlueprint effectBlueprint : effectBlueprints) {
                    if (effectBlueprint.isPlayabilityCheckedForEffect()
                            && !effectBlueprint.isPlayableInFull(actionContext))
                        return false;
                }

                return true;
            }
        };
    }
}