package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.turn.AddSubactionEffectsAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = ActivateTribblePowerEffectBlueprint.class, name = "activateTribblePower"),
        @JsonSubTypes.Type(value = AddModifierEffectBlueprint.class, name = "addModifier"),
        @JsonSubTypes.Type(value = SubActionWithCostBlueprint.class, name = "costToEffect"),
        @JsonSubTypes.Type(value = DrawCardsActionBlueprint.class, name = "drawCards"),
/*        @JsonSubTypes.Type(value = ChooseCardEffectBlueprint.class,
                names = {"chooseActiveCards", "chooseCardsFromDiscard", "chooseCardsFromDrawDeck"}),*/
        @JsonSubTypes.Type(value = ChooseStuffSubActionBlueprint.class,
                names = {"chooseANumber", "chooseOpponent", "choosePlayer", "choosePlayerExcept",
                        "choosePlayerWithCardsInDeck", "chooseTribblePower"}),
        @JsonSubTypes.Type(value = DiscardSubActionBlueprint.class, name = "discard"),
        @JsonSubTypes.Type(value = DiscardThisCardSubActionBlueprint.class, name = "discardThisCard"),
        @JsonSubTypes.Type(value = DownloadActionBlueprint.class, name = "download"),
        @JsonSubTypes.Type(value = DownloadReportableActionBlueprint.class, name = "downloadReportable"),
        @JsonSubTypes.Type(value = IncreaseAttributesSubActionBlueprint.class, name = "increaseAttributes"),
        @JsonSubTypes.Type(value = KillActionBlueprint.class, name = "kill"),
        @JsonSubTypes.Type(value = NullifySubActionBlueprint.class, name = "nullify"),
        @JsonSubTypes.Type(value = OvercomeDilemmaConditionActionBlueprint.class, name = "overcomeCondition"),
        @JsonSubTypes.Type(value = PlaceCardsOnBottomOfDrawDeckSubactionBlueprint.class,
                name = "placeCardsOnBottomOfDrawDeck"),
        @JsonSubTypes.Type(value = RandomSelectionSubActionBlueprint.class, name = "randomSelection"),
        @JsonSubTypes.Type(value = ScorePointsSubActionBlueprint.class, name = "scorePoints"),
        @JsonSubTypes.Type(value = SelectAndPerformSubActionBlueprint.class, name = "selectAndPerformSubAction"),
        @JsonSubTypes.Type(value = ShuffleCardsIntoDrawDeckSubActionBlueprint.class, name = "shuffleCardsIntoDrawDeck"),
        @JsonSubTypes.Type(value = StopActionBlueprint.class, name = "stop")
/*        @JsonSubTypes.Type(value = CardResolverMultiEffectBlueprint.class,
                names = {"discardCardsFromDrawDeck", "discardfromhand", "play",
                        "playcardfromdiscard", "putcardsfromplayonbottomofdeck", "removecardsindiscardfromgame",
                        "shufflecardsfromdiscardintodrawdeck", "shufflecardsfromhandintodrawdeck",
                        "shufflecardsfromplayintodrawdeck"}) */
})
public interface SubActionBlueprint {

    default void addEffectToAction(DefaultGame cardGame, boolean cost, ActionWithSubActions action,
                                   ActionContext actionContext) {
        final AddSubactionEffectsAction sysAction =
                new AddSubactionEffectsAction(cardGame, actionContext, cost, action, this);
        if (cost) {
            action.appendCost(sysAction);
        } else {
            action.appendEffect(sysAction);
        }
    }

    List<Action> createActions(DefaultGame cardGame, ActionWithSubActions parentAction, ActionContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException;
    default boolean isPlayableInFull(DefaultGame cardGame, ActionContext actionContext) { return true; }

    default boolean isPlayabilityCheckedForEffect() {
        return false;
    }
}