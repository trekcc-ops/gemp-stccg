package com.gempukku.lotro.effects.results;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.effects.EffectResult;

public class WoundResult extends EffectResult {
    private final LotroPhysicalCard _card;

    public WoundResult(LotroPhysicalCard card) {
        super(EffectResult.Type.FOR_EACH_WOUNDED);
        _card = card;
    }

    public LotroPhysicalCard getWoundedCard() {
        return _card;
    }
}
