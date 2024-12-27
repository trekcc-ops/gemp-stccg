package com.gempukku.stccg.cards.blueprints.actionsource;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
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

    TopLevelSelectableAction createActionWithNewContext(PhysicalCard card);
    TopLevelSelectableAction createActionWithNewContext(PhysicalCard card, ActionResult actionResult);
    TopLevelSelectableAction createActionWithNewContext(PhysicalCard card, String playerId, ActionResult actionResult);

    void addCost(EffectBlueprint effectBlueprint);
    void addEffect(EffectBlueprint effectBlueprint);
}