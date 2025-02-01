package com.gempukku.stccg.rules.tribbles;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.game.TribblesGame;

import java.util.LinkedList;
import java.util.List;

public class TribblesPlayCardRule extends TribblesRule {
    public TribblesPlayCardRule(TribblesGame game) {
        super(game);
    }

    @Override
    public List<TopLevelSelectableAction> getPhaseActions(Player player) {
        if (_game.getGameState().getCurrentPlayerId().equals(player.getPlayerId())) {
            List<TopLevelSelectableAction> result = new LinkedList<>();
            for (PhysicalCard card : player.getCardsInHand()) {
                if (card.canBePlayed(_game)) {
                    TopLevelSelectableAction action = card.getPlayCardAction();
                    if (action.canBeInitiated(_game))
                        result.add(action);
                }
            }
            return result;
        }
        return null;
    }

}