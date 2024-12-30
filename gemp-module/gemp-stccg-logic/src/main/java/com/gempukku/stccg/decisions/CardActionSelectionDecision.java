package com.gempukku.stccg.decisions;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.Player;

import java.util.List;

public abstract class CardActionSelectionDecision extends ActionDecision {

    public CardActionSelectionDecision(Player player, String text, List<TopLevelSelectableAction> actions) {
        this(player, text, actions, false);
    }


    public CardActionSelectionDecision(Player player, String text, List<TopLevelSelectableAction> actions,
                                       boolean noPass) {
        super(player, text, actions, AwaitingDecisionType.CARD_ACTION_CHOICE);
        setParam("cardId", getCardIds());
        setParam("blueprintId", getBlueprintIds()); // done in super
        setParam("imageUrl", getImageUrls()); // done in super
        setParam("actionType", getActionTypes());
        setParam("noPass", String.valueOf(noPass));
    }



    private String[] getActionTypes() {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(_actions.get(i).getActionType());
        return result;
    }

    private String[] getBlueprintIds() {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++) {
            TopLevelSelectableAction action = _actions.get(i);
            if (action.isVirtualCardAction())
                result[i] = String.valueOf(action.getPerformingCard().getBlueprintId());
            else
                result[i] = "inPlay";
        }
        return result;
    }

    private String[] getImageUrls() {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++) {
            TopLevelSelectableAction action = _actions.get(i);
            if (action.isVirtualCardAction())
                result[i] = String.valueOf(action.getPerformingCard().getBlueprint().getImageUrl());
            else
                result[i] = "inPlay";
        }
        return result;
    }

    private String[] getCardIds() {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(_actions.get(i).getCardForActionSelection().getCardId());
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