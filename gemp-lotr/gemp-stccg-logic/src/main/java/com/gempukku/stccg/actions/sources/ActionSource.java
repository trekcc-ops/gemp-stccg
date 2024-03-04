package com.gempukku.stccg.actions.sources;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public interface ActionSource {

    boolean isValid(ActionContext actionContext);
    void setText(String text);
    void addRequirement(Requirement requirement);

    void appendActionToContext(CostToEffectAction action, ActionContext actionContext);
    Action createAction(PhysicalCard card);

    Action createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext);
    void addCost(EffectAppender effectAppender);
    void addEffect(EffectAppender effectAppender);
    void processRequirementsCostsAndEffects(JSONObject value, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException;
}
