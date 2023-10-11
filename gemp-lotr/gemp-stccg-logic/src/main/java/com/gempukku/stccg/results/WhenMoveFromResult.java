package com.gempukku.stccg.results;

import com.gempukku.stccg.cards.PhysicalCard;

public class WhenMoveFromResult extends EffectResult {
    private final PhysicalCard _site;

    public WhenMoveFromResult(PhysicalCard site) {
        super(EffectResult.Type.WHEN_MOVE_FROM);
        _site = site;
    }

    public PhysicalCard getSite() {
        return _site;
    }
}
