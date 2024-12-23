package com.gempukku.stccg.actions.placecard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.TextUtils;

import java.util.Collection;

public class ShuffleCardsIntoDrawDeckEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final String _shufflingPlayer;
    private final Collection<PhysicalCard> _cards;
    private final Zone _fromZone;
    private final boolean _validateInPlay;

    public ShuffleCardsIntoDrawDeckEffect(DefaultGame game, PhysicalCard source, Zone fromZone, String shufflingPlayer,
                                          Collection<PhysicalCard> cards) {
        super(game, shufflingPlayer);
        _source = source;
        _fromZone = fromZone;
        _shufflingPlayer = shufflingPlayer;
        _cards = cards;
        _validateInPlay = false;
    }

    @Override
    public boolean isPlayableInFull() {
        if (_cards.isEmpty())
            return false;
        for (PhysicalCard card : _cards) {
            if ((_fromZone == null && _validateInPlay && !card.getZone().isInPlay()) ||
                    (_fromZone != null && card.getZone() != _fromZone))
                return false;
        }
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (!isPlayableInFull()) {
            return new FullEffectResult(false);
        } else  {
                _game.getGameState().removeCardsFromZone(_source.getOwnerName(), _cards);
                _game.getGameState().shuffleCardsIntoDeck(_cards, _shufflingPlayer);
                _game.sendMessage(TextUtils.concatenateStrings(_cards.stream().map(PhysicalCard::getCardLink)) + " " + TextUtils.be(_cards) + " shuffled into " + _shufflingPlayer + " deck");
            return new FullEffectResult(true);
        }
    }
}