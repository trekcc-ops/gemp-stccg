package com.gempukku.stccg.actions.sources;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.requirement.Requirement;

public interface ActionSource {
    boolean requiresRanger();

    boolean isValid(ActionContext actionContext);
    void setText(String text);
    void addPlayRequirement(Requirement requirement);

    void appendActionToContext(CostToEffectAction action, ActionContext actionContext);
    Action createAction(PhysicalCard card);

    Action createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext);
    void addCost(EffectAppender effectAppender);
    void addEffect(EffectAppender effectAppender);

}
