package com.gempukku.stccg.decisions;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.Player;

import java.util.List;

public abstract class ActionSelectionDecision extends ActionDecision {

    public ActionSelectionDecision(Player player, String text, List<TopLevelSelectableAction> actions) {
        super(player, text, actions, AwaitingDecisionType.ACTION_CHOICE);
        setParam("blueprintId", getBlueprintIds());
        setParam("imageUrl", getImageUrls());
    }


    private String[] getBlueprintIds() {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++) {
            PhysicalCard physicalCard = _actions.get(i).getCardForActionSelection();
            if (physicalCard != null)
                result[i] = String.valueOf(physicalCard.getBlueprintId());
            else
                result[i] = "rules";
        }
        return result;
    }

    private String[] getImageUrls() {
        String[] images = new String[_actions.size()];
        for (int i = 0; i < images.length; i++) {
            PhysicalCard physicalCard = _actions.get(i).getCardForActionSelection();
            images[i] = (physicalCard == null) ? "rules" : physicalCard.getImageUrl();
        }
        return images;
    }

    protected Action getSelectedAction(String result) throws DecisionResultInvalidException {
        if (result.isEmpty())
            throw new DecisionResultInvalidException();

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