package com.gempukku.stccg.decisions;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.List;

public abstract class ActionDecision extends AbstractAwaitingDecision {

    final List<Action> _actions;

    ActionDecision(Player player, String text, List<Action> actions, AwaitingDecisionType type) {
        super(player, text, type);
        _actions = actions;
        setParam("actionId", getActionIds());
        setParam("actionText", getActionTexts(player.getGame()));
    }



    private String[] getActionIds() {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(i);
        return result;
    }

    private String[] getActionTexts(DefaultGame game) {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = _actions.get(i).getText(game);
        return result;
    }

    public void addAction(Action action) {
        _actions.add(action);
    }

    public List<Action> getActions() { return _actions; }

    public void decisionMade(Action action) throws DecisionResultInvalidException {
        if (_actions.contains(action))
            decisionMade(String.valueOf(_actions.indexOf(action)));
        else throw new DecisionResultInvalidException("Action not found in ActionDecision");
    }

}