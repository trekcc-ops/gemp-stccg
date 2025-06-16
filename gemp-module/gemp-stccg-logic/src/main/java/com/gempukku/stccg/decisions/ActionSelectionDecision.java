package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ActionSelectionDecision extends AbstractAwaitingDecision {

    @JsonProperty("actions")
    final List<TopLevelSelectableAction> _actions;

    @JsonProperty("context")
    private final DecisionContext _context;

    @JsonProperty("cardIds")
    protected final String[] _cardIds;

    @JsonProperty("min")
    private final int _minActions;
    @JsonProperty("max")
    private static final int MAX_SELECTIONS = 1;

    public ActionSelectionDecision(Player player, DecisionContext context, List<TopLevelSelectableAction> actions,
                                   DefaultGame cardGame, boolean required) {
        super(player, context, AwaitingDecisionType.ACTION_CHOICE, cardGame);
        _context = context;
        _actions = actions;
        _cardIds = getCardIds();
        _minActions = required ? 1 : 0;
    }

    public String getElementType() { return "ACTION"; }

    public String[] getActionTexts(DefaultGame game) {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++)
            try {
                result[i] = _actions.get(i).getActionSelectionText(game);
            } catch(InvalidGameLogicException exp) {
                result[i] = "Select action";
            }
        return result;
    }

    public String[] getCardIds() {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(_actions.get(i).getCardIdForActionSelection());
        return result;
    }

    public List<TopLevelSelectableAction> getActions() { return _actions; }

    public void decisionMade(Action action) throws DecisionResultInvalidException {
        if (action instanceof TopLevelSelectableAction && _actions.contains(action))
            decisionMade(String.valueOf(action.getActionId()));
        else throw new DecisionResultInvalidException("Action not found in ActionDecision");
    }

    protected TopLevelSelectableAction getSelectedAction(String result) throws DecisionResultInvalidException {
        if (result.isEmpty())
            return null;
        try {
            int actionId = Integer.parseInt(result);
            for (TopLevelSelectableAction action : _actions) {
                if (action.getActionId() == actionId) {
                    return action;
                }
            }
            throw new DecisionResultInvalidException();
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException();
        }
    }

    public void selectFirstAction() throws DecisionResultInvalidException {
        decisionMade(_actions.getFirst());
    }

}