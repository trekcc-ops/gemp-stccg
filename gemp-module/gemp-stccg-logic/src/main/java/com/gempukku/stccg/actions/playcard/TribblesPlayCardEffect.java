package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.TribblesPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;

public class TribblesPlayCardEffect extends DefaultEffect {
    private final Zone _playedFrom;
    private final PhysicalCard _cardPlayed;
    // TODO - _tribblesGame member is redundant with the DefaultEffect already having _game
    private final TribblesGame _tribblesGame;

    public TribblesPlayCardEffect(TribblesPhysicalCard cardPlayed, Zone playedTo) {
        super(cardPlayed);
        _playedFrom = cardPlayed.getZone();
        _cardPlayed = cardPlayed;
        _tribblesGame = cardPlayed.getGame();
    }

    public PhysicalCard getPlayedCard() {
        return _cardPlayed;
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
        TribblesGameState gameState = _tribblesGame.getGameState();
        int tribbleValue = _cardPlayed.getBlueprint().getTribbleValue();
        gameState.setLastTribblePlayed(tribbleValue);

        int nextTribble = (tribbleValue == 100000) ? 1 : (tribbleValue * 10);
        gameState.setNextTribbleInSequence(nextTribble);

        gameState.setChainBroken(false);
        _tribblesGame.getActionsEnvironment().emitEffectResult(new PlayCardResult(this, _playedFrom, _cardPlayed));

        return new FullEffectResult(true);
    }
}