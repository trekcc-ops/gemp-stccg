package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.actions.PlayEventAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.results.PlayEventResult;

import java.util.Collections;

public class PlayEventEffect extends PlayCardEffect {
    private final PhysicalCard _cardPlayed;
    private final PlayEventResult _playEventResult;

    public PlayEventEffect(DefaultGame game, PlayEventAction action, Zone playedFrom, PhysicalCard cardPlayed, boolean requiresRanger) {
        super(game, playedFrom, cardPlayed, (Zone) null, null);
        _cardPlayed = cardPlayed;
        _playEventResult = new PlayEventResult(action, playedFrom, getPlayedCard(), requiresRanger);
    }

    public PlayEventResult getPlayEventResult() {
        return _playEventResult;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (_cardPlayed.getZone() == Zone.VOID_FROM_HAND) {
            // At this point, card should change initiative if played
            _game.getGameState().removeCardsFromZone(_cardPlayed.getOwnerName(), Collections.singleton(_cardPlayed));
            _game.getGameState().addCardToZone(_game, _cardPlayed, Zone.VOID);
        }

        _game.getActionsEnvironment().emitEffectResult(_playEventResult);
        return new FullEffectResult(true);
    }
}
