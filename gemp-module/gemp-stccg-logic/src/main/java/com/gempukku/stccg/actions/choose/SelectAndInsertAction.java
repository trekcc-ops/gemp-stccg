package com.gempukku.stccg.actions.choose;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class SelectAndInsertAction extends ActionyAction {
    @JsonProperty("selectableActions")
    @JsonIdentityReference(alwaysAsId=true)
    private final List<Action> _selectableActions = new LinkedList<>();
    @JsonProperty("parentAction")
    @JsonIdentityReference(alwaysAsId=true)
    private final Action _parentAction;
    @JsonProperty("selectedAction")
    @JsonIdentityReference(alwaysAsId=true)
    private Action _selectedAction;
    @JsonProperty("decisionId")
    @JsonIdentityReference(alwaysAsId=true)
    private AwaitingDecision _decision;

    private final Map<Action, String> _actionMessageMap;

    public SelectAndInsertAction(DefaultGame cardGame, Action parentAction, String selectingPlayerName,
                                 List<Action> selectableActions, Map<Action, String> actionMessageMap) {
        super(cardGame, selectingPlayerName, ActionType.SELECT_ACTION);
        _selectableActions.addAll(selectableActions);
        _parentAction = parentAction;
        _actionMessageMap = actionMessageMap;
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
    protected void processEffect(DefaultGame cardGame) {
        List<Action> performableActions = new LinkedList<>();
        List<String> actionTexts = new LinkedList<>();
        for (Action action : _selectableActions) {
            if (action.canBeInitiated(cardGame)) {
                performableActions.add(action);
                actionTexts.add(_actionMessageMap.get(action));
            }
        }
        _decision = new MultipleChoiceAwaitingDecision(_performingPlayerId, "Choose an action",
                actionTexts, cardGame) {
            @Override
            protected void validDecisionMade(int index, String result) {
                try {
                    _selectedAction = performableActions.get(index);
                    _parentAction.insertAction(_selectedAction);
                    setAsSuccessful();
                } catch(NoSuchElementException exp) {
                    setAsFailed();
                    cardGame.sendErrorMessage(exp);
                }
            }
        };
        cardGame.sendAwaitingDecision(_decision);
        setAsSuccessful();
    }

}