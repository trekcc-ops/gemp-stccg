package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.EndOfPile;
import com.gempukku.stccg.common.Zone;
import com.gempukku.stccg.effects.AbstractEffect;
import com.gempukku.stccg.effects.DrawCardsEffect;
import com.gempukku.stccg.effects.PutCardsFromZoneOnEndOfPileEffect;
import com.gempukku.stccg.game.TribblesGame;

public class ActivateMasakaTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateMasakaTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    public boolean isPlayableInFull(TribblesGame game) {
        return (game.getGameState().getHand(_activatingPlayer).size() >= 4);
    }
    @Override
    protected AbstractEffect.FullEffectResult playEffectReturningResult(TribblesGame game) {
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