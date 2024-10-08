package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.blueprints.requirement.RequirementFactory;

import java.util.List;

public class EffectWithCostBlueprint extends DelayedEffectBlueprint {
    private final List<EffectBlueprint> _effectBlueprints;
    private final List<EffectBlueprint> _costAppenders;
    private final Requirement[] _requirements;

    public EffectWithCostBlueprint(JsonNode node) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "cost", "effect", "requires");
        _costAppenders = EffectBlueprintDeserializer.getEffectBlueprints(node.get("cost"));
        _effectBlueprints = EffectBlueprintDeserializer.getEffectBlueprints(node.get("effect"));
        _requirements = RequirementFactory.getRequirements(node);
    }
    @Override
    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {

        if(requirementsNotMet(context))
            return null;
        SubAction subAction = action.createSubAction();
        for (EffectBlueprint costAppender : _costAppenders)
            costAppender.addEffectToAction(true, subAction, context);
        for (EffectBlueprint effectBlueprint : _effectBlueprints)
            effectBlueprint.addEffectToAction(false, subAction, context);

        return new StackActionEffect(context.getGame(), subAction);
    }

    private boolean requirementsNotMet(ActionContext actionContext) {
        return (!actionContext.acceptsAllRequirements(_requirements));
    }

    @Override
    public boolean isPlayableInFull(ActionContext actionContext) {

        if(requirementsNotMet(actionContext))
            return false;

        for (EffectBlueprint costAppender : _costAppenders) {
            if (!costAppender.isPlayableInFull(actionContext))
                return false;
        }

        for (EffectBlueprint effectBlueprint : _effectBlueprints) {
            if (effectBlueprint.isPlayabilityCheckedForEffect()
                    && !effectBlueprint.isPlayableInFull(actionContext))
                return false;
        }

        return true;
    }
}