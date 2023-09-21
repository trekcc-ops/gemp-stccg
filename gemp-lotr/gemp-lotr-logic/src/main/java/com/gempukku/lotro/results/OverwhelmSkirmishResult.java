package com.gempukku.lotro.results;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.EffectResult;

import java.util.Set;

public class OverwhelmSkirmishResult extends SkirmishResult {
    public OverwhelmSkirmishResult(Set<LotroPhysicalCard> winners, Set<LotroPhysicalCard> losers, Set<LotroPhysicalCard> removedFromSkirmish) {
        super(EffectResult.Type.SKIRMISH_FINISHED_WITH_OVERWHELM, winners, losers, removedFromSkirmish);
    }
}
