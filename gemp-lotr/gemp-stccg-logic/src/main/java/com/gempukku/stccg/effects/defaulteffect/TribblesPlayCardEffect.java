package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.results.PlayCardResult;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collections;

public class TribblesPlayCardEffect extends DefaultEffect {
    private final Zone _playedFrom;
    private final PhysicalCard _cardPlayed;
    private final Zone _zone;
    private final TribblesGame _game;

    public TribblesPlayCardEffect(TribblesGame game, Zone playedFrom, PhysicalCard cardPlayed, Zone playedTo) {
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _zone = playedTo;
        _game = game;
    }

    public PhysicalCard getPlayedCard() {
        return _cardPlayed;
    }

    @Override
    public String getText() {
        return "Play " + GameUtils.getFullName(_cardPlayed);
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        TribblesGameState gameState = _game.getGameState();
        gameState.removeCardsFromZone(_cardPlayed.getOwner(), Collections.singleton(_cardPlayed));
        gameState.addCardToZone(_game, _cardPlayed, _zone);

        int tribbleValue = _cardPlayed.getBlueprint().getTribbleValue();

        gameState.setLastTribblePlayed(tribbleValue);
        if (tribbleValue == 100000) {
            gameState.setNextTribbleInSequence(1);
        } else {
            gameState.setNextTribbleInSequence(tribbleValue * 10);
        }
        gameState.setChainBroken(false);
        _game.getActionsEnvironment().emitEffectResult(new PlayCardResult(_playedFrom, _cardPlayed));

        return new FullEffectResult(true);
    }
}