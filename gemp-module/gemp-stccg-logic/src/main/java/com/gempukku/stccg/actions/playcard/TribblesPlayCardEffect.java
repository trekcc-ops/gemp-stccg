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
/*        gameState.removeCardsFromZone(_cardPlayed.getOwnerName(), Collections.singleton(_cardPlayed));
        gameState.addCardToZone(_game, _cardPlayed, _zone); */

        int tribbleValue = _cardPlayed.getBlueprint().getTribbleValue();

        gameState.setLastTribblePlayed(tribbleValue);
        if (tribbleValue == 100000) {
            gameState.setNextTribbleInSequence(1);
        } else {
            gameState.setNextTribbleInSequence(tribbleValue * 10);
        }
        gameState.setChainBroken(false);
        _tribblesGame.getActionsEnvironment().emitEffectResult(new PlayCardResult(this, _playedFrom, _cardPlayed));

        return new FullEffectResult(true);
    }
}