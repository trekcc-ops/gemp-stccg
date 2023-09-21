package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;
import com.gempukku.lotro.results.PlayCardResult;

import java.util.Collections;

public class TribblesPlayCardEffect extends AbstractEffect {
    private final Zone _playedFrom;
    private final LotroPhysicalCard _cardPlayed;
    private final Zone _zone;

    public TribblesPlayCardEffect(Zone playedFrom, LotroPhysicalCard cardPlayed, Zone playedTo) {
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _zone = playedTo;
    }

    public LotroPhysicalCard getPlayedCard() {
        return _cardPlayed;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Play " + GameUtils.getFullName(_cardPlayed);
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        game.getGameState().removeCardsFromZone(_cardPlayed.getOwner(), Collections.singleton(_cardPlayed));
        game.getGameState().addCardToZone(game, _cardPlayed, _zone);

        // Defined by the game. For Tribbles, this is where the chain is advanced.
        game.getGameState().playEffectReturningResult(_cardPlayed);

        game.getActionsEnvironment().emitEffectResult(new PlayCardResult(_playedFrom, _cardPlayed));

        return new FullEffectResult(true);
    }
}