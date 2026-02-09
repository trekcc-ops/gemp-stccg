package com.gempukku.stccg.rules.tribbles;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.LinkedList;
import java.util.List;

public class TribblesPlayCardRule extends TribblesRule {
    public List<TopLevelSelectableAction> getPhaseActions(DefaultGame cardGame, Player player) {
        if (cardGame.getGameState().getCurrentPlayerId().equals(player.getPlayerId())) {
            List<TopLevelSelectableAction> result = new LinkedList<>();
            for (PhysicalCard card : player.getCardsInHand()) {
                if (cardGame.getRules().cardCanEnterPlay(cardGame, card, PlayCardAction.EnterPlayActionType.PLAY)) {
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