package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SeedThisCardActionBlueprint.class, name = "seedThis"),
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

    default boolean hasEffectOfType(Class<? extends ActionBlueprint> clazz) {
        if (clazz.isAssignableFrom(getClass())) {
            return true;
        } else {
            for (ActionBlueprint subAction : getAllTheoreticalSubActions()) {
                if (subAction.hasEffectOfType(clazz)) {
                    return true;
                }
            }
        }
        return false;
    }

    default boolean hasPlayCardForFreeEffect() {
        if (this instanceof PlayCardForFreeActionBlueprint) {
            return true;
        } else if (this instanceof PlayThisCardActionBlueprint playBlueprint && playBlueprint.isForFree()) {
            return true;
        } else {
            for (ActionBlueprint subAction : getAllTheoreticalSubActions()) {
                if (subAction.hasPlayCardForFreeEffect()) {
                    return true;
                }
            }
        }
        return false;
    }

    Collection<ActionBlueprint> getAllTheoreticalSubActions();
}