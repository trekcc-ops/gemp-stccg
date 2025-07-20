package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.responses.ActionSelectionDecisionResponse;
import com.gempukku.stccg.decisions.responses.DecisionResponse;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class ActionSelectionDecision extends AbstractAwaitingDecision {

    @JsonProperty("actions")
    final List<TopLevelSelectableAction> _selectableActions;
    @JsonProperty("context")
    private final DecisionContext _context;
    @JsonProperty("min")
    private final int _minActions;

    // Some of the behavior of this class assumes this will always be 1
    @JsonProperty("max")
    private static final int MAX_SELECTIONS = 1;
    private final List<Action> _selectedActions = new ArrayList<>();

    public ActionSelectionDecision(Player player, DecisionContext context, List<TopLevelSelectableAction> actions,
                                   DefaultGame cardGame, boolean required) {
        super(player, context, cardGame);
        _context = context;
        _selectableActions = actions;
        _minActions = required ? 1 : 0;
    }

    public String getElementType() { return "ACTION"; }

    public List<TopLevelSelectableAction> getActions() { return _selectableActions; }

    public void setResponseAndFollowUp(Action action) throws DecisionResultInvalidException, InvalidGameOperationException {
        if (action instanceof TopLevelSelectableAction && _selectableActions.contains(action)) {
            _selectedActions.clear();
            _selectedActions.add(action);
            _responded = true;
            try {
                followUp();
            } catch(InvalidGameLogicException exp) {
                throw new InvalidGameOperationException(exp.getMessage());
            }
        }
        else throw new DecisionResultInvalidException("Action not found in ActionDecision");
    }

    public void setDecisionResponse(DefaultGame cardGame, DecisionResponse response) throws DecisionResultInvalidException {
        if (_responded || !_selectedActions.isEmpty()) {
            throw new DecisionResultInvalidException("Trying to set response for an already-completed decision");
        } else if (response instanceof ActionSelectionDecisionResponse actionResponse) {
            List<Integer> actionIds = actionResponse.getActionIds();
            if (actionIds == null || actionIds.size() < _minActions || actionIds.size() > MAX_SELECTIONS) {
                throw new DecisionResultInvalidException("Wrong number of actions received as decision response");
            }
            List<Action> actionsToAdd = new ArrayList<>();
            for (Integer actionId : actionIds) {
                Action selectedAction = cardGame.getActionById(actionId);
                if (selectedAction != null && _selectableActions.contains(selectedAction)) {
                    actionsToAdd.add(selectedAction);
                } else {
                    throw new DecisionResultInvalidException("Selected invalid action for decision");
                }
            }
            _selectedActions.addAll(actionsToAdd);
            _responded = true;
        }
    }

    public void setDecisionResponse(List<Action> selectedActions) throws DecisionResultInvalidException {
        if (_responded || !_selectedActions.isEmpty()) {
            throw new DecisionResultInvalidException("Trying to set response for an already-completed decision");
        }
        if (selectedActions == null || selectedActions.size() < _minActions || selectedActions.size() > MAX_SELECTIONS) {
            throw new DecisionResultInvalidException("Wrong number of actions received as decision response");
        }
        List<Action> actionsToAdd = new ArrayList<>();
        for (Action action : selectedActions) {
            if (action != null && _selectableActions.contains(action)) {
                actionsToAdd.add(action);
            } else {
                throw new DecisionResultInvalidException("Selected invalid action for decision");
            }
        }
        _selectedActions.addAll(actionsToAdd);
        _responded = true;
    }

    protected Action getSelectedAction() throws DecisionResultInvalidException {
        if (!_responded) {
            throw new DecisionResultInvalidException("Trying to get selected action from an incomplete decision");
        } else if (_selectedActions.isEmpty()) {
            return null;
        } else {
            return _selectedActions.getFirst();
        }
    }

    protected TopLevelSelectableAction getSelectedAction(String result) throws DecisionResultInvalidException {
        if (result.isEmpty())
            return null;
        try {
            int actionId = Integer.parseInt(result);
            for (TopLevelSelectableAction action : _selectableActions) {
                if (action.getActionId() == actionId) {
                    return action;
                }
            }
            throw new DecisionResultInvalidException();
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException();
        }
    }

    public void selectFirstAction() throws DecisionResultInvalidException, InvalidGameOperationException {
        setResponseAndFollowUp(_selectableActions.getFirst());
    }

}