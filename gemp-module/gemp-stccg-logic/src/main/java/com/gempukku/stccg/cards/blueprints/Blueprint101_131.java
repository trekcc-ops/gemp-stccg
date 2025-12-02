package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.playcard.STCCGPlayCardAction;
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
            TopLevelSelectableAction playAction = new STCCGPlayCardAction(stCard, Zone.CORE, player, true);
            result.add(playAction);
        }
        return result;
    }
}