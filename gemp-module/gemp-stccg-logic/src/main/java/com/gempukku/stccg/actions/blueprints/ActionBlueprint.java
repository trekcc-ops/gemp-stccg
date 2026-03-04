package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = SeedThisCardActionBlueprint.class, name = "seedThis"),
        @JsonSubTypes.Type(value = SeedCardIntoPlayBlueprint.class, name = "seedCardIntoPlay"),
        @JsonSubTypes.Type(value = ActivateCardActionBlueprint.class, name = "activate"),
        @JsonSubTypes.Type(value = DownloadCardToDestinationActionBlueprint.class, name = "downloadCardToDestination"),
        @JsonSubTypes.Type(value = DrawAdditionalCardsAtEndOfTurnActionBlueprint.class,
                name = "drawAdditionalCardsAtEndOfTurn"),
        @JsonSubTypes.Type(value = EncounterSeedCardActionBlueprint.class, name = "encounter"),
        @JsonSubTypes.Type(value = OptionalTriggerActionBlueprint.class, name = "optionalTrigger"),
        @JsonSubTypes.Type(value = PlayCardForFreeActionBlueprint.class, name = "playCardForFree"),
        @JsonSubTypes.Type(value = PlayThisCardActionBlueprint.class, name = "playThis"),
        @JsonSubTypes.Type(value = PlayThisCardAsResponseActionBlueprint.class, name = "playThisAsResponse"),
        @JsonSubTypes.Type(value = RequiredTriggerActionBlueprint.class, name = "requiredTrigger"),
        @JsonSubTypes.Type(value = VolunteerForSelectionActionBlueprint.class, name = "volunteerForRandomSelection")
})
public interface ActionBlueprint {
    Action createAction(DefaultGame cardGame, GameTextContext context);
}