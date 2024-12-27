package com.gempukku.stccg.cards.blueprints.actionsource;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprint;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public interface ActionSource {

    boolean isValid(ActionContext actionContext);
    void setText(String text);
    void addRequirement(Requirement requirement);

    void appendActionToContext(Action action, ActionContext actionContext);
    Action createAction(PhysicalCard card);

    Action createActionWithNewContext(PhysicalCard card);
    Action createActionWithNewContext(PhysicalCard card, EffectResult effectResult);
    Action createActionWithNewContext(PhysicalCard card, String playerId, EffectResult effectResult);

    void addCost(EffectBlueprint effectBlueprint);
    void addEffect(EffectBlueprint effectBlueprint);
}