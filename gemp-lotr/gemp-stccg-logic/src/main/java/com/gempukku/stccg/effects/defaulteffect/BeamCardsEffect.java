package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalMissionCard;
import com.gempukku.stccg.effects.DefaultEffect;

import java.util.Collection;

public class BeamCardsEffect extends DefaultEffect {
    private final Collection<PhysicalCard> _cardsToBeam;
    private final PhysicalCard _toCard;
    private final PhysicalCard _fromCard;

    public BeamCardsEffect(Collection<PhysicalCard> cardsToBeam, PhysicalCard fromCard, PhysicalCard toCard) {
        _toCard = toCard;
        _cardsToBeam = cardsToBeam;
        _fromCard = fromCard;
    }

    @Override
    public String getText() {
        return "Beam cards";
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        for (PhysicalCard card : _cardsToBeam) {
            card.attachToCardAtLocation(_toCard);
        }
        if (_toCard instanceof PhysicalMissionCard)
            ((PhysicalMissionCard) _toCard).organizeAwayTeamsOnSurface();
        if (_fromCard instanceof PhysicalMissionCard)
            ((PhysicalMissionCard) _fromCard).organizeAwayTeamsOnSurface();
        return new FullEffectResult(true);
    }
}