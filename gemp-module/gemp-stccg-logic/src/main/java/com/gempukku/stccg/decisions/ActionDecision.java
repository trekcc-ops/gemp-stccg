package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ActionDecision extends AbstractAwaitingDecision {

    final List<TopLevelSelectableAction> _actions;

    @JsonProperty("context")
    private final DecisionContext _context;

    @JsonProperty("cardIds")
    protected final String[] _cardIds;

    protected final List<String> _blueprintIds = new ArrayList<>();
    private final String[] _actionTexts;

    ActionDecision(Player player, DecisionContext context, List<TopLevelSelectableAction> actions,
                   DefaultGame cardGame) {
        super(player, context, AwaitingDecisionType.ACTION_CHOICE, cardGame);
        _context = context;
        _actions = actions;
        setParam("actionId", getActionIds());
        _actionTexts = getActionTexts(cardGame);
        _cardIds = getCardIds();
    }

    public String getElementType() { return "ACTION"; }



    protected String[] getActionIds() {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(i);
        return result;
    }

    protected String[] getActionTypes() {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(_actions.get(i).getActionType());
        return result;
    }


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

    public List<TopLevelSelectableAction> getActions() { return _actions; }

    public void decisionMade(Action action) throws DecisionResultInvalidException {
        if (_actions.contains(action))
            decisionMade(String.valueOf(_actions.indexOf(action)));
        else throw new DecisionResultInvalidException("Action not found in ActionDecision");
    }

    @JsonProperty("displayedCards")
    protected List<Map<Object, Object>> getDisplayedCards() {
        List<Map<Object, Object>> result = new ArrayList<>();
        for (int i = 0; i < _cardIds.length; i++) {
            Map<Object, Object> mapToAdd = new HashMap<>();
            mapToAdd.put("cardId", _cardIds[i]);
            mapToAdd.put("blueprintId", _blueprintIds.get(i));
            mapToAdd.put("actionId", getActionIds()[i]);
            mapToAdd.put("actionText", _actionTexts[i]);
            mapToAdd.put("actionType", getActionTypes()[i]);
            mapToAdd.put("selectable", "true");
            result.add(mapToAdd);
        }
        return result;
    }

}