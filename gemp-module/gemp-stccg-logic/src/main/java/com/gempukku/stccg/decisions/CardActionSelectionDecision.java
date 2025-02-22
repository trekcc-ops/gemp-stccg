package com.gempukku.stccg.decisions;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.Arrays;
import java.util.List;

public abstract class CardActionSelectionDecision extends ActionDecision {

    public CardActionSelectionDecision(Player player, String text, List<TopLevelSelectableAction> actions,
                                       DefaultGame cardGame) {
        this(player, text, actions, false, cardGame);
    }


    public CardActionSelectionDecision(Player player, String text, List<TopLevelSelectableAction> actions,
                                       boolean noPass, DefaultGame cardGame) {
        super(player, text, actions, AwaitingDecisionType.CARD_ACTION_CHOICE, cardGame);
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
        Arrays.fill(result, "inPlay");
        return result;
    }

    private String[] getImageUrls() {
        String[] result = new String[_actions.size()];
        Arrays.fill(result, "inPlay");
        return result;
    }

    private String[] getCardIds() {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(_actions.get(i).getCardIdForActionSelection());
        return result;
    }

    protected TopLevelSelectableAction getSelectedAction(String result) throws DecisionResultInvalidException {
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