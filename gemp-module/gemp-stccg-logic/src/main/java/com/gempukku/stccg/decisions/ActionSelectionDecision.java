package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ActionSelectionDecision extends ActionDecision {

    public ActionSelectionDecision(Player player, DecisionContext context, List<TopLevelSelectableAction> actions,
                                   DefaultGame cardGame)
            throws CardNotFoundException {
        super(player, context, actions, AwaitingDecisionType.ACTION_CHOICE, cardGame);
        setParam("blueprintId", getBlueprintIds(cardGame));
        setParam("imageUrl", getImageUrls(cardGame));
    }

    private String[] getBlueprintIds(DefaultGame cardGame) throws CardNotFoundException {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++) {
            int cardId = _actions.get(i).getCardIdForActionSelection();
            PhysicalCard physicalCard = cardGame.getCardFromCardId(cardId);
            result[i] = (physicalCard == null) ? "rules" : String.valueOf(physicalCard.getBlueprintId());
        }
        return result;
    }

    private String[] getImageUrls(DefaultGame cardGame) throws CardNotFoundException {
        String[] images = new String[_actions.size()];
        for (int i = 0; i < images.length; i++) {
            int cardId = _actions.get(i).getCardIdForActionSelection();
            PhysicalCard physicalCard = cardGame.getCardFromCardId(cardId);
            images[i] = (physicalCard == null) ? "rules" : physicalCard.getImageUrl();
        }
        return images;
    }

    protected TopLevelSelectableAction getSelectedAction(String result) throws DecisionResultInvalidException {
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

    @JsonProperty("displayedCards")
    private List<Map<Object, Object>> getDisplayedCards() {
        List<Map<Object, Object>> result = new ArrayList<>();
        for (int i = 0; i < getDecisionParameters().get("cardId").length; i++) {
            Map<Object, Object> mapToAdd = new HashMap<>();
            mapToAdd.put("cardId", getDecisionParameters().get("cardId")[i]);
            mapToAdd.put("blueprintId", getDecisionParameters().get("blueprintId")[i]);
            mapToAdd.put("actionId", getDecisionParameters().get("actionId")[i]);
            mapToAdd.put("actionText", getDecisionParameters().get("actionText")[i]);
            result.add(mapToAdd);
        }
        return result;
        
    }
}