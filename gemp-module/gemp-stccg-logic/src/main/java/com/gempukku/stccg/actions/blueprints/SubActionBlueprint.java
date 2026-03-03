package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AddModifierEffectBlueprint.class, name = "addModifier"),
        @JsonSubTypes.Type(value = DrawCardsActionBlueprint.class, name = "drawCards"),
        @JsonSubTypes.Type(value = DiscardSubActionBlueprint.class, name = "discard"),
        @JsonSubTypes.Type(value = DiscardThisCardSubActionBlueprint.class, name = "discardThisCard"),
        @JsonSubTypes.Type(value = DownloadActionBlueprint.class, name = "download"),
        @JsonSubTypes.Type(value = DownloadReportableActionBlueprint.class, name = "downloadReportable"),
        @JsonSubTypes.Type(value = IncreaseAttributesSubActionBlueprint.class, name = "increaseAttributes"),
        @JsonSubTypes.Type(value = ConditionalSubActionBlueprint.class, name = "if"),
        @JsonSubTypes.Type(value = KillActionBlueprint.class, name = "kill"),
        @JsonSubTypes.Type(value = NullifySubActionBlueprint.class, name = "nullify"),
        @JsonSubTypes.Type(value = OvercomeDilemmaConditionActionBlueprint.class, name = "overcomeCondition"),
        @JsonSubTypes.Type(value = PlaceCardInPointAreaSubActionBlueprint.class, name = "placeCardInPointArea"),
        @JsonSubTypes.Type(value = PlaceCardsOnBottomOfDrawDeckSubactionBlueprint.class,
                name = "placeCardsOnBottomOfDrawDeck"),
        @JsonSubTypes.Type(value = PlaceCardsOnTopOfDrawDeckSubactionBlueprint.class,
                name = "placeCardsOnTopOfDrawDeck"),
        @JsonSubTypes.Type(value = PlaceOnThisMissionActionBlueprint.class, name = "placeOnThisMission"),
        @JsonSubTypes.Type(value = RandomSelectionSubActionBlueprint.class, name = "randomSelection"),
        @JsonSubTypes.Type(value = RemoveCardsFromGameSubActionBlueprint.class, name = "removeCardsFromGame"),
        @JsonSubTypes.Type(value = ScorePointsSubActionBlueprint.class, name = "scorePoints"),
        @JsonSubTypes.Type(value = SelectCardSubActionBlueprint.class, name = "selectCard"),
        @JsonSubTypes.Type(value = SelectAndPerformSubActionBlueprint.class, name = "selectAndPerformSubAction"),
        @JsonSubTypes.Type(value = ShuffleCardsIntoDrawDeckSubActionBlueprint.class, name = "shuffleCardsIntoDrawDeck"),
        @JsonSubTypes.Type(value = StopSubActionBlueprint.class, name = "stop")
})
public interface SubActionBlueprint {

    default void addEffectToAction(boolean cost, ActionWithSubActions action) {
        if (cost) {
            action.appendCost(this);
        } else {
            action.appendSubAction(this);
        }
    }

    Action createAction(DefaultGame cardGame, ActionWithSubActions parentAction, GameTextContext context);

    default boolean isPlayableInFull(DefaultGame cardGame, GameTextContext actionContext) { return true; }

    default boolean isPlayabilityCheckedForEffect() {
        return false;
    }
}