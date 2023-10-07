package com.gempukku.lotro.effects.tribblepowers;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.effects.AbstractEffect;
import com.gempukku.lotro.effects.PlacePlayedCardBeneathDrawDeckEFfect;
import com.gempukku.lotro.effects.PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect;
import com.gempukku.lotro.game.TribblesGame;

public class ActivateConvertTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateConvertTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        super(action, source);
    }

    @Override
    protected AbstractEffect.FullEffectResult playEffectReturningResult(TribblesGame game) {
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(new PlacePlayedCardBeneathDrawDeckEFfect(_source));
        subAction.appendEffect(new PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(_activatingPlayer, 1));
        return addActionAndReturnResult(game, subAction);
    }
}