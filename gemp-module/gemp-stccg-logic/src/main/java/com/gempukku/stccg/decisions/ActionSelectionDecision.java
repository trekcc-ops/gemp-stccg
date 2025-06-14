package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.List;

public abstract class ActionSelectionDecision extends ActionDecision {

    @JsonProperty("min")
    private final int _min = 1;
    @JsonProperty("max")
    private final int _max = 1;

    public ActionSelectionDecision(Player player, DecisionContext context, List<TopLevelSelectableAction> actions,
                                   DefaultGame cardGame)
            throws CardNotFoundException {
        super(player, context, actions, cardGame);
        setBlueprintIds(cardGame);
        setParam("blueprintId", _blueprintIds.toArray(new String[0]));
        setParam("imageUrl", getImageUrls(cardGame));
        setParam("cardId", _cardIds);
    }

    protected void setBlueprintIds(DefaultGame cardGame) throws CardNotFoundException {
        _blueprintIds.clear();
        for (TopLevelSelectableAction action : _actions) {
            int cardId = action.getCardIdForActionSelection();
            PhysicalCard physicalCard = cardGame.getCardFromCardId(cardId);
            _blueprintIds.add(String.valueOf(physicalCard.getBlueprintId()));
        }
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

    public String[] getCardIds() {
        String[] result = new String[_actions.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = "temp" + i;
        }
        return result;
    }

}