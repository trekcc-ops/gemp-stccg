package com.gempukku.stccg.results;

import com.gempukku.stccg.cards.PhysicalCard;

public class ForEachKilledResult extends EffectResult {
    private final PhysicalCard _killedCard;

    public ForEachKilledResult(PhysicalCard killedCard) {
        super(EffectResult.Type.FOR_EACH_KILLED);
        _killedCard = killedCard;
    }

    public PhysicalCard getKilledCard() {
        return _killedCard;
    }

}
