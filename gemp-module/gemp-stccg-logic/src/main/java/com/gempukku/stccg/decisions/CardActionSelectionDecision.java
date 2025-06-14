package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.*;

public abstract class CardActionSelectionDecision extends ActionDecision {

    @JsonProperty("min")
    protected final int _min;

    @JsonProperty("max")
    protected final int _max = 1;

    public CardActionSelectionDecision(Player player, DecisionContext context, List<TopLevelSelectableAction> actions,
                                       DefaultGame cardGame) {
        this(player, context, actions, false, cardGame);
    }


    public CardActionSelectionDecision(Player player, DecisionContext context, List<TopLevelSelectableAction> actions,
                                       boolean noPass, DefaultGame cardGame) {
        super(player, context, actions, cardGame);
        _min = noPass ? 1 : 0;
        setBlueprintIds();
        setParam("cardId", getCardIds());
        setParam("blueprintId", _blueprintIds.toArray(new String[0])); // done in super
        setParam("imageUrl", getImageUrls()); // done in super
        setParam("actionType", getActionTypes());
    }


    private void setBlueprintIds() {
        _blueprintIds.clear();
        for (TopLevelSelectableAction action : _actions) {
            _blueprintIds.add("inPlay");
        }
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
}