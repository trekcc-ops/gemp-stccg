package com.gempukku.stccg.rules.tribbles;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.game.TribblesGame;

import java.util.LinkedList;
import java.util.List;

public class TribblesPlayCardRule extends TribblesRule {
    @Override
    public List<TopLevelSelectableAction> getPhaseActions(TribblesGame cardGame, Player player) {
        if (cardGame.getGameState().getCurrentPlayerId().equals(player.getPlayerId())) {
            List<TopLevelSelectableAction> result = new LinkedList<>();
            for (PhysicalCard card : player.getCardsInHand()) {
                if (card.canBePlayed(cardGame)) {
                    TopLevelSelectableAction action = card.getPlayCardAction(cardGame);
                    if (action.canBeInitiated(cardGame))
                        result.add(action);
                }
            }
            return result;
        }
        return null;
    }

}