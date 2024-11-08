package com.gempukku.stccg.decisions;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.Player;

import java.util.List;
import java.util.Objects;

public abstract class ActionSelectionDecision extends ActionDecision {

    public ActionSelectionDecision(Player player, String text, List<Action> actions) {
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
        CardBlueprintLibrary blueprintLibrary = _decidingPlayer.getGame().getBlueprintLibrary();
        String[] blueprints = getBlueprintIds();
        String[] images = new String[blueprints.length];
        for (int i = 0; i < blueprints.length; i++) {
            if (Objects.equals(blueprints[i], "rules")) {
                images[i] = "rules";
            } else {
                try {
                    images[i] = blueprintLibrary.getCardBlueprint(blueprints[i]).getImageUrl();
                } catch (CardNotFoundException exp) {
                    throw new RuntimeException(
                            "ActionSelectionDecision unable to find image URLs for all card blueprints", exp);
                }
            }
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