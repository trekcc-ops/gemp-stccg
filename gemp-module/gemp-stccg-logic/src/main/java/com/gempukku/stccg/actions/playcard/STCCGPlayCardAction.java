package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.usage.UseNormalCardPlayAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.Player;

public class STCCGPlayCardAction extends PlayCardAction {

    public STCCGPlayCardAction(ST1EPhysicalCard card, Zone zone, Player player, boolean forFree) {
        super(card, card, player, zone, ActionType.PLAY_CARD);
        setText("Play " + card.getFullName());
        if (!forFree)
            appendCost(new UseNormalCardPlayAction(card.getGame(), player));
    }

    public STCCGPlayCardAction(PhysicalCard card, Zone zone, Player player, boolean forFree,
                               Enum<?>[] progressValues) {
        super(card, card, player, zone, ActionType.PLAY_CARD, progressValues);
        setText("Play " + card.getFullName());
        if (!forFree)
            appendCost(new UseNormalCardPlayAction(card.getGame(), player));
    }


}