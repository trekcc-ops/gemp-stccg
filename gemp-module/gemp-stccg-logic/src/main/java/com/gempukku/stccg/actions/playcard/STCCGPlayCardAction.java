package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.UseNormalCardPlayEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

public class STCCGPlayCardAction extends PlayCardAction {
    private final ST1EGame _game;

    public STCCGPlayCardAction(ST1EPhysicalCard card, Zone zone, Player player) {
        this(card, zone, player, false);
    }

    public STCCGPlayCardAction(ST1EPhysicalCard card, Zone zone, Player player, boolean forFree) {
        super(card, card, player.getPlayerId(), zone, ActionType.PLAY_CARD);
        _game = card.getGame();
        setText("Play " + card.getFullName());
        if (!forFree)
            appendCost(new UseNormalCardPlayEffect(_game, player));
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _cardEnteringPlay;
    }

}