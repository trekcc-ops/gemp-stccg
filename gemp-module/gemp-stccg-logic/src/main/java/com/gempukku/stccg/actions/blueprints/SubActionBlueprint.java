package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
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
        @JsonSubTypes.Type(value = SelectSubActionBlueprint.class,
                names = {"chooseANumber", "chooseOpponent", "choosePlayer", "choosePlayerExcept",
                        "choosePlayerWithCardsInDeck", "chooseTribblePower"}),
        @JsonSubTypes.Type(value = DiscardThisCardSubActionBlueprint.class, name = "discardThisCard"),
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
    default void addEffectToAction(boolean cost, CardPerformedAction action, ActionContext actionContext) {
            final SystemQueueAction sysAction = new SystemQueueAction(actionContext.getGame()) {
                @Override
                public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
                    try {
                        // Need to insert them, but in the reverse order
                        final List<Action> actions = createActions(action, actionContext);
                        if (actions != null) {
                            final Action[] effectsArray = actions.toArray(new Action[0]);
                            for (int i = effectsArray.length - 1; i >= 0; i--)
                                if (cost)
                                    action.insertCost(effectsArray[i]);
                                else
                                    action.insertEffect(effectsArray[i]);
                        }
                    } catch (InvalidCardDefinitionException exp) {
                        throw new InvalidGameLogicException(exp.getMessage());
                    }
                    Action nextAction = getNextAction();
                    if (nextAction != null)
                        return nextAction;
                    else {
                        setAsSuccessful();
                        return null;
                    }
                }
            };

            if (cost) {
                action.appendCost(sysAction);
            } else {
                action.appendEffect(sysAction);
            }
    }
    List<Action> createActions(CardPerformedAction action, ActionContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException;
    default boolean isPlayableInFull(ActionContext actionContext) { return true; }
    default boolean isPlayabilityCheckedForEffect() {
        return false;
    }
}