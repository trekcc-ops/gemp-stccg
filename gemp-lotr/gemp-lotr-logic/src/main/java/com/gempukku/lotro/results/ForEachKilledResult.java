package com.gempukku.lotro.results;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.effects.KillEffect;
import com.gempukku.lotro.effects.EffectResult;

public class ForEachKilledResult extends EffectResult {
    private final PhysicalCard _killedCard;
    private final KillEffect.Cause _cause;

    public ForEachKilledResult(PhysicalCard killedCard, KillEffect.Cause cause) {
        super(EffectResult.Type.FOR_EACH_KILLED);
        _killedCard = killedCard;
        _cause = cause;
    }

    public PhysicalCard getKilledCard() {
        return _killedCard;
    }

    public KillEffect.Cause getCause() {
        return _cause;
    }
}
