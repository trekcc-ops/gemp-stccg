package com.gempukku.stccg.decisions;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;

import java.util.LinkedList;
import java.util.List;

public abstract class CardActionSelectionDecision extends AbstractAwaitingDecision {
    private final List<Action> _actions;

    public CardActionSelectionDecision(int decisionId, String text, List<? extends Action> actions) {
        this(decisionId, text, actions, false);
    }

    public CardActionSelectionDecision(String text, List<? extends Action> actions) {
        this(1, text, actions, false);
    }

    public CardActionSelectionDecision(int decisionId, String text, List<? extends Action> actions,
                                       boolean revertEligible) {
        super(decisionId, text, AwaitingDecisionType.CARD_ACTION_CHOICE);
        _actions = new LinkedList<>(actions);

        setParam("actionId", getActionIds(actions));
        setParam("cardId", getCardIds(actions));
        setParam("blueprintId", getBlueprintIdsForVirtualActions(actions));
        setParam("imageUrl", getImageUrlsForVirtualActions(actions));
        setParam("actionText", getActionTexts(actions));
        setParam("actionType", getActionTypes(actions));
        setParam("revertEligible", String.valueOf(revertEligible)); // TODO SNAPSHOT - no methods for "revertEligible" in client
    }

    /**
     * For testing, being able to inject an extra action at any point
     *
     * @param action
     */
    public void addAction(Action action) {
        _actions.add(action);
    }

    private String[] getActionIds(List<? extends Action> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(i);
        return result;
    }

    private String[] getActionTypes(List<? extends Action> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(actions.get(i).getActionType());
        return result;
    }

    public List<Action> getActions() { return _actions; }

    private String[] getBlueprintIdsForVirtualActions(List<? extends Action> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++) {
            Action action = actions.get(i);
            if (action.isVirtualCardAction())
                result[i] = String.valueOf(action.getActionSource().getBlueprintId());
            else
                result[i] = "inPlay";
        }
        return result;
    }

    private String[] getImageUrlsForVirtualActions(List<? extends Action> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++) {
            Action action = actions.get(i);
            if (action.isVirtualCardAction())
                result[i] = String.valueOf(action.getActionSource().getBlueprint().getImageUrl());
            else
                result[i] = "inPlay";
        }
        return result;
    }

    private String[] getCardIds(List<? extends Action> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(actions.get(i).getActionAttachedToCard().getCardId());
        return result;
    }

    private String[] getActionTexts(List<? extends Action> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = actions.get(i).getText();
        return result;
    }

    protected Action getSelectedAction(String result) throws DecisionResultInvalidException {
        if (result.isEmpty())
            return null;
        try {
            int actionIndex = Integer.parseInt(result);
            if (actionIndex < 0 || actionIndex >= _actions.size())
                throw new DecisionResultInvalidException();

            return _actions.get(actionIndex);
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException();
        }
    }
}
