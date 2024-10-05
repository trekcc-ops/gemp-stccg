package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.actions.Effect;

public class DiscardCardFromPlayPileResult extends EffectResult {

    public DiscardCardFromPlayPileResult(Effect effect, PhysicalCard source) {
        super(Type.FOR_EACH_DISCARDED_FROM_PLAY_PILE, effect, source);
    }

}