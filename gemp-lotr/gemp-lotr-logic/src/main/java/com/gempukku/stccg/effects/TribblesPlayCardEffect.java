package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.results.PlayCardResult;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collections;

public class TribblesPlayCardEffect extends AbstractEffect<TribblesGame> {
    private final Zone _playedFrom;
    private final PhysicalCard _cardPlayed;
    private final Zone _zone;

    public TribblesPlayCardEffect(Zone playedFrom, PhysicalCard cardPlayed, Zone playedTo) {
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _zone = playedTo;
    }

    public PhysicalCard getPlayedCard() {
        return _cardPlayed;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Play " + GameUtils.getFullName(_cardPlayed);
    }

    @Override
    public boolean isPlayableInFull(TribblesGame game) {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(TribblesGame game) {
        TribblesGameState gameState = game.getGameState();
        gameState.removeCardsFromZone(_cardPlayed.getOwner(), Collections.singleton(_cardPlayed));
        gameState.addCardToZone(game, _cardPlayed, _zone);

        int tribbleValue = _cardPlayed.getBlueprint().getTribbleValue();

        gameState.setLastTribblePlayed(tribbleValue);
        if (tribbleValue == 100000) {
            gameState.setNextTribbleInSequence(1);
        } else {
            gameState.setNextTribbleInSequence(tribbleValue * 10);
        }
        gameState.setChainBroken(false);
        game.getActionsEnvironment().emitEffectResult(new PlayCardResult(_playedFrom, _cardPlayed));

        return new FullEffectResult(true);
    }
}