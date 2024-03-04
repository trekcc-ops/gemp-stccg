package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

public class STCCGPlayCardAction extends PlayCardAction {
    private final DefaultGame _game;

    public STCCGPlayCardAction(PhysicalCard card, Zone zone, Player player) {
        super(card, card, player.getPlayerId(), zone, ActionType.PLAY_CARD);
        _game = card.getGame();
        setText("Play " + card.getFullName());
    }

    public STCCGPlayCardAction(PhysicalCard card, Filterable destinationFilter, Player player) {
        super(card, card, player.getPlayerId(), Zone.ATTACHED, ActionType.PLAY_CARD);
        _game = card.getGame();
        setText("Play " + card.getFullName());
    }

    public STCCGPlayCardAction(PhysicalCard card, Zone zone, Player player, boolean forFree) {
        super(card, card, player.getPlayerId(), zone, ActionType.PLAY_CARD);
        _game = card.getGame();
        setText("Play " + card.getFullName());
/*        if (!forFree)
            appendCost(new UseNormalCardPlayEffect()); */
    }

    @Override
    public DefaultGame getGame() { return _game; }
    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _cardEnteringPlay;
    }

    protected Effect getFinalEffect() {
        return new PlayCardEffect(_performingPlayerId, _fromZone, _cardEnteringPlay, _toZone);
    }
}
