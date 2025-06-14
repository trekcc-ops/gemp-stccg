package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.*;

public abstract class CardActionSelectionDecision extends ActionDecision {

    @JsonProperty("min")
    protected final int _min;

    @JsonProperty("max")
    protected final int _max = 1;

    @JsonProperty("cardIds")
    private final String[] _cardIds;

    public CardActionSelectionDecision(Player player, DecisionContext context, List<TopLevelSelectableAction> actions,
                                       DefaultGame cardGame) {
        this(player, context, actions, false, cardGame);
    }


    public CardActionSelectionDecision(Player player, DecisionContext context, List<TopLevelSelectableAction> actions,
                                       boolean noPass, DefaultGame cardGame) {
        super(player, context, actions, AwaitingDecisionType.CARD_ACTION_CHOICE, cardGame);
        _min = noPass ? 1 : 0;
        _cardIds = getCardIds();
        setParam("cardId", getCardIds());
        setParam("blueprintId", getBlueprintIds()); // done in super
        setParam("imageUrl", getImageUrls()); // done in super
        setParam("actionType", getActionTypes());
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

    public String[] getCardIds() {
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

    @JsonProperty("displayedCards")
    private List<Map<Object, Object>> getDisplayedCards() {
        List<Map<Object, Object>> result = new ArrayList<>();
        for (int i = 0; i < _cardIds.length; i++) {
            Map<Object, Object> mapToAdd = new HashMap<>();
            mapToAdd.put("cardId", _cardIds[i]);
            mapToAdd.put("blueprintId", getBlueprintIds()[i]);
            mapToAdd.put("actionId", getActionIds()[i]);
            mapToAdd.put("actionText", getDecisionParameters().get("actionText")[i]);
            mapToAdd.put("actionType", getActionTypes()[i]);
            result.add(mapToAdd);
        }
        return result;
    }
}