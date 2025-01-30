package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = SeedCardActionBlueprint.class, name = "seedThis"),
        @JsonSubTypes.Type(value = ActivateCardActionBlueprint.class, name = "activate"),
        @JsonSubTypes.Type(value = OptionalTriggerActionBlueprint.class, name = "optionalTrigger"),
        @JsonSubTypes.Type(value = RequiredTriggerActionBlueprint.class, name = "requiredTrigger")
})
public interface ActionBlueprint {

    boolean isValid(ActionContext actionContext);
    void setText(String text);
    void addRequirement(Requirement requirement);

    void appendActionToContext(TopLevelSelectableAction action, ActionContext actionContext);
    Action createAction(PhysicalCard card);

    TopLevelSelectableAction createActionWithNewContext(PhysicalCard card);
    TopLevelSelectableAction createActionWithNewContext(PhysicalCard card, ActionResult actionResult);
    TopLevelSelectableAction createActionWithNewContext(PhysicalCard card, String playerId, ActionResult actionResult);

    void addCost(SubActionBlueprint subActionBlueprint);
    void addEffect(SubActionBlueprint subActionBlueprint);
}