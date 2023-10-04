package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;

public class ScorePointsEffect extends AbstractEffect<DefaultGame> {
    private final PhysicalCard _source;
    private final String _scoringPlayer;
    private final int _points;
    public ScorePointsEffect(PhysicalCard source, String scoringPlayer, int points) {
        _source = source;
        _scoringPlayer = scoringPlayer;
        _points = points;
    }

    @Override
    public String getText(DefaultGame game) {
        return _scoringPlayer + " scored " + _points + " from " + GameUtils.getCardLink(_source);
    }
    @Override
    public boolean isPlayableInFull(DefaultGame game) { return true; }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        game.getGameState().addToPlayerScore(_scoringPlayer, _points);
        return new FullEffectResult(true);
    }

}