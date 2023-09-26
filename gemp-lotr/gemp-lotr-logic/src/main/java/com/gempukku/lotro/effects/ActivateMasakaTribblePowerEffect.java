package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.TribblesGame;

public class ActivateMasakaTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateMasakaTribblePowerEffect(CostToEffectAction action, LotroPhysicalCard source) {
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
            for (LotroPhysicalCard card : game.getGameState().getHand(player)) {
                subAction.appendEffect(new PutCardFromHandOnBottomOfDeckEffect(false, card));
            }
            subAction.appendEffect(new DrawCardsEffect(_action, player, 3));
        }
        return addActionAndReturnResult(game, subAction);
    }
}