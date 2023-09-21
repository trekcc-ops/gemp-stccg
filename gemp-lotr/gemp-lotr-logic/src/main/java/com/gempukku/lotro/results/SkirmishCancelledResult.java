package com.gempukku.lotro.results;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.EffectResult;

public class SkirmishCancelledResult extends EffectResult {
    public final LotroPhysicalCard fpCharacter;

    public SkirmishCancelledResult(LotroPhysicalCard fpCharacter) {
        super(Type.SKIRMISH_CANCELLED);
        this.fpCharacter = fpCharacter;
    }

    public LotroPhysicalCard getFpCharacter() {
        return fpCharacter;
    }
}
