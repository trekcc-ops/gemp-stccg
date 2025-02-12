package com.gempukku.stccg.actions.choose;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class SelectAndInsertAction extends ActionyAction {
    @JsonProperty("selectableActions")
    @JsonIdentityReference(alwaysAsId=true)
    private final List<TopLevelSelectableAction> _selectableActions = new LinkedList<>();
    @JsonProperty("parentAction")
    @JsonIdentityReference(alwaysAsId=true)
    private final ActionyAction _parentAction;
    @JsonProperty("selectedAction")
    @JsonIdentityReference(alwaysAsId=true)
    private Action _selectedAction;
    @JsonProperty("decisionId")
    @JsonIdentityReference(alwaysAsId=true)
    private AwaitingDecision _decision;

    public SelectAndInsertAction(DefaultGame cardGame, ActionyAction parentAction, Player selectingPlayer,
                                 TopLevelSelectableAction... actions) {
        super(cardGame, selectingPlayer, "Choose an action", ActionType.SELECT_ACTION);
        _selectableActions.addAll(Arrays.asList(actions));
        _parentAction = parentAction;
    }


    public boolean requirementsAreMet(DefaultGame game) {
        boolean result = false;
        for (Action action : _selectableActions) {
            if (action.canBeInitiated(game)) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (_decision == null) {
            List<TopLevelSelectableAction> performableActions = new LinkedList<>();
            List<String> actionTexts = new LinkedList<>();
            for (TopLevelSelectableAction action : _selectableActions) {
                if (action.canBeInitiated(cardGame)) {
                    performableActions.add(action);
                    actionTexts.add(action.getActionSelectionText(cardGame));
                }
            }
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            _decision = new MultipleChoiceAwaitingDecision(performingPlayer, "Choose an action",
                    actionTexts, cardGame) {
                @Override
                protected void validDecisionMade(int index, String result) {
                    try {
                        _selectedAction = performableActions.get(index);
                        _parentAction.insertAction(_selectedAction);
                        setAsSuccessful();
                        _wasCarriedOut = true;
                    } catch(NoSuchElementException exp) {
                        setAsFailed();
                        cardGame.sendErrorMessage(exp);
                    }
                }
            };
            cardGame.getUserFeedback().sendAwaitingDecision(_decision);
        }
        return getNextAction();
    }

    @Override
    public boolean wasCarriedOut() {
        return wasCompleted();
    }

}