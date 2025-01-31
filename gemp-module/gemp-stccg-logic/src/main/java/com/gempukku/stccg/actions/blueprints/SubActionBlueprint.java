package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.cards.ActionContext;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = ActivateTribblePowerEffectBlueprint.class, name = "activateTribblePower"),
        @JsonSubTypes.Type(value = AddModifierEffectBlueprint.class, name = "addModifier"),
        @JsonSubTypes.Type(value = SubActionWithCostBlueprint.class, name = "costToEffect"),
        @JsonSubTypes.Type(value = DrawCardsActionBlueprint.class, name = "drawCards"),
/*        @JsonSubTypes.Type(value = ChooseCardEffectBlueprint.class,
                names = {"chooseActiveCards", "chooseCardsFromDiscard", "chooseCardsFromDrawDeck"}),
        @JsonSubTypes.Type(value = SelectEffectBlueprint.class,
                names = {"chooseANumber", "chooseOpponent", "choosePlayer", "choosePlayerExcept",
                        "choosePlayerWithCardsInDeck", "chooseTribblePower"}),*/
        @JsonSubTypes.Type(value = DiscardActionBlueprint.class, name = "discard"),
        @JsonSubTypes.Type(value = DownloadActionBlueprint.class, name = "download"),
        @JsonSubTypes.Type(value = KillActionBlueprint.class, name = "kill"),
        @JsonSubTypes.Type(value = OvercomeDilemmaConditionActionBlueprint.class, name = "overcomeCondition")
/*        @JsonSubTypes.Type(value = CardResolverMultiEffectBlueprint.class,
                names = {"discardCardsFromDrawDeck", "discardfromhand", "play",
                        "playcardfromdiscard", "putcardsfromplayonbottomofdeck", "removecardsindiscardfromgame",
                        "shufflecardsfromdiscardintodrawdeck", "shufflecardsfromhandintodrawdeck",
                        "shufflecardsfromplayintodrawdeck"}) */
})
public interface SubActionBlueprint {
    void addEffectToAction(boolean cost, CardPerformedAction action, ActionContext actionContext);
    boolean isPlayableInFull(ActionContext actionContext);
    default boolean isPlayabilityCheckedForEffect() {
        return false;
    }
}