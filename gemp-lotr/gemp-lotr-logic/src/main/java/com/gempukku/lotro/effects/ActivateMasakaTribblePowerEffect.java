package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.EndOfPile;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.TribblesGame;

public class ActivateMasakaTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateMasakaTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    public boolean isPlayableInFull(TribblesGame game) {
        return (game.getGameState().getHand(_activatingPlayer).size() >= 4);
    }
    @Override
    protected FullEffectResult playEffectReturningResult(TribblesGame game) {
        SubAction subAction = new SubAction(_action);
        for (String player : game.getPlayers()) {
            for (PhysicalCard card : game.getGameState().getHand(player)) {
                subAction.appendEffect(new PutCardsFromZoneOnEndOfPileEffect(
                        false, Zone.HAND, Zone.DRAW_DECK, EndOfPile.BOTTOM, card));
            }
            subAction.appendEffect(new DrawCardsEffect(_action, player, 3));
        }
        return addActionAndReturnResult(game, subAction);
    }
}