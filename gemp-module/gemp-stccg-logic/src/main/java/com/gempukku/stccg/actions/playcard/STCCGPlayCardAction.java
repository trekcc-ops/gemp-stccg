package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.usage.UseNormalCardPlayAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

public class STCCGPlayCardAction extends PlayCardAction {

    public STCCGPlayCardAction(DefaultGame cardGame, ST1EPhysicalCard card, Zone zone, String performingPlayerName,
                               boolean forFree) {
        super(cardGame, card, card, performingPlayerName, zone, ActionType.PLAY_CARD);
        if (!forFree)
            appendCost(new UseNormalCardPlayAction(cardGame, performingPlayerName));
    }

    public STCCGPlayCardAction(DefaultGame cardGame, ST1EPhysicalCard card, Zone zone, Player player, boolean forFree) {
        super(cardGame, card, card, player.getPlayerId(), zone, ActionType.PLAY_CARD);
        if (!forFree)
            appendCost(new UseNormalCardPlayAction(cardGame, player));
    }

    public STCCGPlayCardAction(DefaultGame cardGame, PhysicalCard card, Zone zone, String playerName, boolean forFree,
                               ActionContext context) {
        super(cardGame, card, card, playerName, zone, ActionType.PLAY_CARD, context);
        if (!forFree)
            appendCost(new UseNormalCardPlayAction(cardGame, playerName));
    }

    public STCCGPlayCardAction(DefaultGame cardGame, PhysicalCard card, Zone zone, String performingPlayerName,
                               boolean forFree) {
        super(cardGame, card, card, performingPlayerName, zone, ActionType.PLAY_CARD);
        if (!forFree)
            appendCost(new UseNormalCardPlayAction(cardGame, performingPlayerName));
    }


}