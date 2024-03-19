package com.gempukku.stccg.actions.lotr;

import com.gempukku.stccg.actions.playcard.PlayCardEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.Collections;

public class PlayEventEffect extends PlayCardEffect {
    private final PhysicalCard _cardPlayed;
    private final PlayEventResult _playEventResult;

    public PlayEventEffect(PlayEventAction action, Zone playedFrom, PhysicalCard cardPlayed, boolean requiresRanger) {
        super(cardPlayed.getOwnerName(), playedFrom, cardPlayed, (Zone) null, null);
        _cardPlayed = cardPlayed;
        _playEventResult = new PlayEventResult(this, action, playedFrom, getPlayedCard(), requiresRanger);
    }

    public PlayEventResult getPlayEventResult() {
        return _playEventResult;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (_cardPlayed.getZone() == Zone.VOID_FROM_HAND) {
            // At this point, card should change initiative if played
            _game.getGameState().removeCardsFromZone(_cardPlayed.getOwnerName(), Collections.singleton(_cardPlayed));
            _game.getGameState().addCardToZone(_cardPlayed, Zone.VOID);
        }

        _game.getActionsEnvironment().emitEffectResult(_playEventResult);
        return new FullEffectResult(true);
    }
}
