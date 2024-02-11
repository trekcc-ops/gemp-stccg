package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collection;

public class ShuffleCardsIntoDrawDeckEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final String _shufflingPlayer;
    private final Collection<PhysicalCard> _cards;
    private final Zone _fromZone;
    private final boolean _validateInPlay;
    private final DefaultGame _game;

    public ShuffleCardsIntoDrawDeckEffect(DefaultGame game, PhysicalCard source, Zone fromZone, String shufflingPlayer,
                                          Collection<PhysicalCard> cards) {
        _source = source;
        _fromZone = fromZone;
        _shufflingPlayer = shufflingPlayer;
        _cards = cards;
        _validateInPlay = false;
        _game = game;
    }

    @Override
    public boolean isPlayableInFull() {
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
    protected FullEffectResult playEffectReturningResult() {
        if (!isPlayableInFull()) {
            return new FullEffectResult(false);
        } else  {
                _game.getGameState().removeCardsFromZone(_source.getOwnerName(), _cards);
                _game.getGameState().shuffleCardsIntoDeck(_cards, _shufflingPlayer);
                _game.getGameState().sendMessage(GameUtils.getAppendedNames(_cards) + " " + GameUtils.be(_cards) + " shuffled into " + _shufflingPlayer + " deck");
            return new FullEffectResult(true);
        }
    }
}
