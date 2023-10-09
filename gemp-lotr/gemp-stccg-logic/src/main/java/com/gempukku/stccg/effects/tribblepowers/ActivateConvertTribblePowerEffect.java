package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.AbstractEffect;
import com.gempukku.stccg.effects.PlacePlayedCardBeneathDrawDeckEFfect;
import com.gempukku.stccg.effects.PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect;
import com.gempukku.stccg.game.TribblesGame;

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