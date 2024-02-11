package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.results.PlayCardResult;

import java.util.Collections;

public class PlayCardEffect extends DefaultEffect {
    private final Zone _playedFrom;
    private final PhysicalCard _cardPlayed;
    private PhysicalCard _attachedToCard;
    private final Zone _zone;
    private final PhysicalCard _attachedOrStackedPlayedFrom;
    protected final DefaultGame _game;

    public PlayCardEffect(DefaultGame game, Zone playedFrom, PhysicalCard cardPlayed, Zone playedTo) {
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _zone = playedTo;
        _attachedOrStackedPlayedFrom = null;
        _game = game;
    }

    public PlayCardEffect(DefaultGame game, Zone playedFrom, PhysicalCard cardPlayed, Zone playedTo, PhysicalCard attachedOrStackedPlayedFrom) {
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _zone = playedTo;
        _game = game;
        _attachedOrStackedPlayedFrom = attachedOrStackedPlayedFrom;
    }

    public PlayCardEffect(DefaultGame game, Zone playedFrom, PhysicalCard cardPlayed, PhysicalCard attachedToCard, PhysicalCard attachedOrStackedPlayedFrom) {
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _attachedToCard = attachedToCard;
        _attachedOrStackedPlayedFrom = attachedOrStackedPlayedFrom;
        _zone = Zone.ATTACHED;
        _game = game;
    }

    public PhysicalCard getPlayedCard() {
        return _cardPlayed;
    }

    public PhysicalCard getAttachedTo() {
        return _attachedToCard;
    }

    @Override
    public String getText() {
        return "Play " + _cardPlayed.getFullName();
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        _game.getGameState().removeCardsFromZone(_cardPlayed.getOwnerName(), Collections.singleton(_cardPlayed));
        if (_attachedToCard != null) {
            _game.getGameState().attachCard(_game, _cardPlayed, _attachedToCard);
        } else {
            _game.getGameState().addCardToZone(_game, _cardPlayed, _zone);
        }

        _game.getActionsEnvironment().emitEffectResult(new PlayCardResult(_playedFrom, _cardPlayed, _attachedToCard, _attachedOrStackedPlayedFrom));

        return new FullEffectResult(true);
    }
}
