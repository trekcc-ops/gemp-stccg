package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collection;

public class ShuffleCardsIntoDrawDeckEffect extends AbstractEffect<DefaultGame> {
    private final PhysicalCard _source;
    private final String _shufflingPlayer;
    private final Collection<PhysicalCard> _cards;
    private final Zone _fromZone;
    private final boolean _validateInPlay;

    public ShuffleCardsIntoDrawDeckEffect(PhysicalCard source, Zone fromZone, String shufflingPlayer,
                                          Collection<PhysicalCard> cards) {
        _source = source;
        _fromZone = fromZone;
        _shufflingPlayer = shufflingPlayer;
        _cards = cards;
        _validateInPlay = false;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        if (_cards.size() == 0)
            return false;
        for (PhysicalCard card : _cards) {
            if ((_fromZone == null && _validateInPlay && !card.getZone().isInPlay()) ||
                    (_fromZone != null && card.getZone() != _fromZone))
                return false;
        }
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (!isPlayableInFull(game)) {
            return new FullEffectResult(false);
        } else  {
                game.getGameState().removeCardsFromZone(_source.getOwner(), _cards);
                game.getGameState().shuffleCardsIntoDeck(_cards, _shufflingPlayer);
                game.getGameState().sendMessage(getAppendedNames(_cards) + " " + GameUtils.be(_cards) + " shuffled into " + _shufflingPlayer + " deck");
            return new FullEffectResult(true);
        }
    }
}
