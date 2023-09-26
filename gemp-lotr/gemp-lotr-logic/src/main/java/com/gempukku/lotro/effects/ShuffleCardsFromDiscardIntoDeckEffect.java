package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ShuffleCardsFromDiscardIntoDeckEffect extends AbstractEffect<DefaultGame> {
    private final LotroPhysicalCard _source;
    private final String _shufflingPlayer;
    private final Collection<? extends LotroPhysicalCard> _cards;

    public ShuffleCardsFromDiscardIntoDeckEffect(LotroPhysicalCard source, String shufflingPlayer,
                                                 Collection<? extends LotroPhysicalCard> cards) {
        _source = source;
        _shufflingPlayer = shufflingPlayer;
        _cards = cards;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        for (LotroPhysicalCard card : _cards) {
            if (card.getZone() != Zone.DISCARD)
                return false;
        }

        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        Set<LotroPhysicalCard> toShuffleIn = new HashSet<>();
        for (LotroPhysicalCard card : _cards) {
            if (card.getZone() == Zone.DISCARD)
                toShuffleIn.add(card);
        }

        if (toShuffleIn.size() > 0) {
            game.getGameState().removeCardsFromZone(_source.getOwner(), toShuffleIn);

            game.getGameState().shuffleCardsIntoDeck(toShuffleIn, _shufflingPlayer);

            game.getGameState().sendMessage(getAppendedNames(toShuffleIn) + " " + GameUtils.be(toShuffleIn) + " shuffled into " + _shufflingPlayer + " deck");
        }

        return new FullEffectResult(toShuffleIn.size() == _cards.size());
    }
}
