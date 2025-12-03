package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.blueprints.*;
import com.gempukku.stccg.actions.choose.SelectAndInsertAction;
import com.gempukku.stccg.actions.choose.SelectCardAction;
import com.gempukku.stccg.actions.choose.SelectRandomCardAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.modifiers.KillSinglePersonnelAction;
import com.gempukku.stccg.actions.modifiers.StopCardsAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.evaluator.SkillDotCountEvaluator;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.filters.EncounteringCardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.*;

@SuppressWarnings("unused")
public class Blueprint152_003 extends CardBlueprint {
    // Dedication to Duty
    Blueprint152_003() {
        super("152_003");
    }

    private ActionBlueprint getEncounterActionBlueprint(DefaultGame game, PhysicalCard thisCard, AttemptingUnit attemptingUnit)
            throws InvalidCardDefinitionException {
        String playerName = attemptingUnit.getControllerName();
        FilterBlueprint uniquePersonnelAttemptingFilter =
                (cardGame, actionContext) -> Filters.and(new EncounteringCardFilter(thisCard), Filters.unique);

        // TODO - Make these constructors private again once I'm done noodling around
        CardTargetBlueprint targetBlueprint =
                new SelectCardTargetBlueprint(uniquePersonnelAttemptingFilter, 1, true);
        StopActionBlueprint stopBlueprint =
                new StopActionBlueprint(targetBlueprint, "stoppedPersonnel");

        ValueSource skillDotCountSource = new ValueSource() {

            @Override
            public float evaluateExpression(DefaultGame cardGame, ActionContext actionContext) {
                Collection<Integer> selectedCardIds = actionContext.getCardIdsFromMemory("stoppedPersonnel");
                float result = 0;
                for (Integer cardId : selectedCardIds) {
                    try {
                        PhysicalCard selectedCard = cardGame.getCardFromCardId(cardId);
                        if (selectedCard instanceof PersonnelCard personnel) {
                            result = result + personnel.getSkillDotCount();
                        }
                    } catch(CardNotFoundException exp) {
                        cardGame.sendErrorMessage(exp);
                    }
                }
                return result;
            }
        };

        KillActionBlueprint killBlueprint = new KillActionBlueprint(targetBlueprint);
        DrawCardsActionBlueprint drawBlueprint =
                new DrawCardsActionBlueprint(skillDotCountSource, "opponent");

        SubActionBlueprint selectAndInsertAction = new SubActionBlueprint() {

            @Override
            public List<Action> createActions(DefaultGame cardGame, CardPerformedAction action, ActionContext actionContext)
                    throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
                Action killAction = killBlueprint.createActions(cardGame, action, actionContext).getFirst();
                Action drawAction = drawBlueprint.createActions(cardGame, action, actionContext).getFirst();
                Map<Action, String> messageMap = new HashMap<>();
                messageMap.put(killAction, "kill");
                messageMap.put(drawAction, "draw");
                return List.of(new SelectAndInsertAction(cardGame, action, playerName, List.of(killAction, drawAction),
                        messageMap));
            }
        };

        return new EncounterSeedCardActionBlueprint(List.of(stopBlueprint, selectAndInsertAction));
    }

    @Override
    public List<Action> getEncounterActionsFromJava(ST1EPhysicalCard thisCard, DefaultGame game,
                                                    AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action, MissionLocation missionLocation) {

        boolean useOldDef = true;

        if (useOldDef) {
            return getEncounterActionsFromJavaOld(thisCard, game, attemptingUnit, action, missionLocation);
        } else {
            try {
                _actionBlueprints.add(getEncounterActionBlueprint(game, thisCard, attemptingUnit));
                return getEncounterSeedCardActions(thisCard, action.getAttemptAction(), game, attemptingUnit, missionLocation);
            } catch(InvalidCardDefinitionException | InvalidGameLogicException | PlayerNotFoundException exp) {
                game.sendErrorMessage(exp);
                return new ArrayList<>();
            }
        }
    }


    public List<Action> getEncounterActionsFromJavaOld(ST1EPhysicalCard thisCard, DefaultGame game, AttemptingUnit attemptingUnit,
                                                    EncounterSeedCardAction action, MissionLocation missionLocation) {

        List<Action> result = new ArrayList<>();
        String opponentId = game.getOpponent(attemptingUnit.getControllerName());

        // One unique personnel is "stopped" (random selection).
        List<PersonnelCard> uniquePersonnel = new ArrayList<>();
        for (PersonnelCard personnel : attemptingUnit.getAttemptingPersonnel()) {
            if (personnel.isUnique())
                uniquePersonnel.add(personnel);
        }

        SelectCardAction randomSelection =
                new SelectRandomCardAction(game, thisCard.getOwnerName(), "Choose a personnel to be stopped",
                        uniquePersonnel);
        Action stopAction = new StopCardsAction(game, thisCard.getOwnerName(), randomSelection);
        TopLevelSelectableAction action1 =
                new KillSinglePersonnelAction(game, thisCard.getOwnerName(), thisCard, randomSelection);

        SkillDotCountEvaluator skillDotEvaluator = new SkillDotCountEvaluator(randomSelection);
        TopLevelSelectableAction action2 =
                new DrawCardsAction(game, thisCard, opponentId, skillDotEvaluator);

        List<Action> selectableActions = new ArrayList<>();
        selectableActions.add(action1);
        selectableActions.add(action2);

        Map<Action, String> actionMessageMap = new HashMap<>();
        actionMessageMap.put(action1, "Kill personnel");
        actionMessageMap.put(action2, "Draw card(s)");

        Action multipleChoiceDecision = new SelectAndInsertAction(game, action, attemptingUnit.getControllerName(),
                selectableActions, actionMessageMap);

        result.add(randomSelection);
        result.add(stopAction);
        result.add(multipleChoiceDecision);

        return result;
    }

}