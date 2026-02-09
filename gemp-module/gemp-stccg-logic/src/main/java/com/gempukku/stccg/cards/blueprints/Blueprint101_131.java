package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint101_131 extends CardBlueprint {

    // Palor Toff: Alien Trader

    @Override
    public List<TopLevelSelectableAction> getPlayActionsFromGameText(PhysicalCard thisCard, Player player, DefaultGame cardGame) {
        List<TopLevelSelectableAction> result = new ArrayList<>();
        if (thisCard instanceof ST1EPhysicalCard stCard && !cardGame.getCurrentPhase().isSeedPhase()) {
            PlayCardAction playAction =
                    new PlayCardAction(cardGame, stCard, stCard, player.getPlayerId(), Zone.CORE, ActionType.PLAY_CARD);
            result.add(playAction);
        }
        return result;
    }
}