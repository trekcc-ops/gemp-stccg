package com.gempukku.stccg.decisions;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.List;

public abstract class ActionDecision extends AbstractAwaitingDecision {

    final List<TopLevelSelectableAction> _actions;

    ActionDecision(Player player, String text, List<TopLevelSelectableAction> actions, AwaitingDecisionType type,
                   DefaultGame cardGame) {
        super(player, text, type, cardGame);
        _actions = actions;
        setParam("actionId", getActionIds());
        try {
            setParam("actionText", getActionTexts(cardGame));
        } catch(InvalidGameLogicException exp) {
            setParam("actionText", "Select action");
        }
    }


    private String[] getActionIds() {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(i);
        return result;
    }

    private String[] getActionTexts(DefaultGame game) throws InvalidGameLogicException {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = _actions.get(i).getActionSelectionText(game);
        return result;
    }

    public List<TopLevelSelectableAction> getActions() { return _actions; }

    public void decisionMade(Action action) throws DecisionResultInvalidException {
        if (_actions.contains(action))
            decisionMade(String.valueOf(_actions.indexOf(action)));
        else throw new DecisionResultInvalidException("Action not found in ActionDecision");
    }

}