package com.gempukku.lotro.game.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.actions.lotronly.PlayEventAction;
import com.gempukku.lotro.game.timing.results.PlayEventResult;

import java.util.Collections;

public class PlayEventEffect extends PlayCardEffect {
    private final PhysicalCard _cardPlayed;
    private final PlayEventResult _playEventResult;

    public PlayEventEffect(PlayEventAction action, Zone playedFrom, PhysicalCard cardPlayed, boolean requiresRanger, boolean paidToil) {
        super(playedFrom, cardPlayed, (Zone) null, null, paidToil);
        _cardPlayed = cardPlayed;
        _playEventResult = new PlayEventResult(action, playedFrom, getPlayedCard(), requiresRanger, paidToil);
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