package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.PlayEventAction;
import com.gempukku.stccg.results.PlayEventResult;

import java.util.Collections;

public class PlayEventEffect extends PlayCardEffect {
    private final PhysicalCard _cardPlayed;
    private final PlayEventResult _playEventResult;

    public PlayEventEffect(PlayEventAction action, Zone playedFrom, PhysicalCard cardPlayed, boolean requiresRanger, boolean paidToil) {
        super(playedFrom, cardPlayed, (Zone) null, null);
        _cardPlayed = cardPlayed;
        _playEventResult = new PlayEventResult(action, playedFrom, getPlayedCard(), requiresRanger);
    }

    public PlayEventResult getPlayEventResult() {
        return _playEventResult;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (_cardPlayed.getZone() == Zone.VOID_FROM_HAND) {
            // At this point, card should change initiative if played
            game.getGameState().removeCardsFromZone(_cardPlayed.getOwner(), Collections.singleton(_cardPlayed));
            game.getGameState().addCardToZone(game, _cardPlayed, Zone.VOID);
        }

        game.getActionsEnvironment().emitEffectResult(_playEventResult);
        return new FullEffectResult(true);
    }
}
