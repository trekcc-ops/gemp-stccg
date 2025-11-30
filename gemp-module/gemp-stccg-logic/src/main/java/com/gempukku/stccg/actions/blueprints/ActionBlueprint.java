package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = SeedCardActionBlueprint.class, name = "seedThis"),
        @JsonSubTypes.Type(value = ActivateCardActionBlueprint.class, name = "activate"),
        @JsonSubTypes.Type(value = EncounterSeedCardActionBlueprint.class, name = "encounter"),
        @JsonSubTypes.Type(value = OptionalTriggerActionBlueprint.class, name = "optionalTrigger"),
        @JsonSubTypes.Type(value = RequiredTriggerActionBlueprint.class, name = "requiredTrigger")
})
public interface ActionBlueprint {
    boolean isValid(DefaultGame cardGame, ActionContext actionContext);

    void addRequirement(Requirement requirement);

    void appendActionToContext(DefaultGame cardGame, TopLevelSelectableAction action, ActionContext actionContext);

    TopLevelSelectableAction createActionWithNewContext(DefaultGame cardGame, PhysicalCard card);
    TopLevelSelectableAction createActionWithNewContext(PhysicalCard card, ActionResult actionResult);
    TopLevelSelectableAction createActionWithNewContext(PhysicalCard card, String playerId, ActionResult actionResult);

    void addCost(SubActionBlueprint subActionBlueprint);
    void addEffect(SubActionBlueprint subActionBlueprint);
}