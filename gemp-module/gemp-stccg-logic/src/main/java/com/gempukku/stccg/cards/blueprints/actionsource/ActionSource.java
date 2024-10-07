package com.gempukku.stccg.cards.blueprints.actionsource;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.blueprints.effectappender.EffectAppender;
import com.gempukku.stccg.requirement.Requirement;

public interface ActionSource {

    boolean isValid(ActionContext actionContext);
    void setText(String text);
    void addRequirement(Requirement requirement);

    void appendActionToContext(CostToEffectAction action, ActionContext actionContext);
    Action createAction(PhysicalCard card);

    Action createActionWithNewContext(PhysicalCard card);
    Action createActionWithNewContext(PhysicalCard card, EffectResult effectResult);
    Action createActionWithNewContext(PhysicalCard card, String playerId, Effect effect, EffectResult effectResult);
    Action createActionWithNewContext(PhysicalCard card, Effect effect, EffectResult effectResult);
    void addCost(EffectAppender effectAppender);
    void addEffect(EffectAppender effectAppender);
    void processRequirementsCostsAndEffects(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException;
}