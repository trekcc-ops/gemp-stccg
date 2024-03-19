package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ScorePointsEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final String _scoringPlayer;
    private final int _points;
    private final DefaultGame _game;
    public ScorePointsEffect(DefaultGame game, PhysicalCard source, String scoringPlayer, int points) {
        super(scoringPlayer);
        _source = source;
        _scoringPlayer = scoringPlayer;
        _points = points;
        _game = game;
    }

    @Override
    public String getText() {
        return _scoringPlayer + " scored " + _points + " from " + _source.getCardLink();
    }
    @Override
    public boolean isPlayableInFull() { return true; }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        _game.getGameState().addToPlayerScore(_scoringPlayer, _points);
        return new FullEffectResult(true);
    }

}