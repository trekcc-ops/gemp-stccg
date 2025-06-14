package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;

import java.util.List;

public abstract class ActionDecision extends AbstractAwaitingDecision {

    final List<TopLevelSelectableAction> _actions;

    @JsonProperty("context")
    private final DecisionContext _context;

    ActionDecision(Player player, DecisionContext context, List<TopLevelSelectableAction> actions,
                   AwaitingDecisionType type, DefaultGame cardGame) {
        super(player, context, type, cardGame);
        _context = context;
        _actions = actions;
        setParam("actionId", getActionIds());
        try {
            setParam("actionText", getActionTexts(cardGame));
        } catch(InvalidGameLogicException exp) {
            setParam("actionText", "Select action");
        }
    }

    public String getElementType() { return "ACTION"; }



    protected String[] getActionIds() {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(i);
        return result;
    }

    protected String[] getActionTexts(DefaultGame game) throws InvalidGameLogicException {
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