package com.gempukku.stccg.decisions;

import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAwaitingDecision implements AwaitingDecision {
    private final int _decisionId;
    private final String _text;
    private final AwaitingDecisionType _decisionType;
    private final String _decidingPlayerId;

    public AbstractAwaitingDecision(Player player, String text, AwaitingDecisionType decisionType,
                                    DefaultGame cardGame) {
        _decisionId = cardGame.getUserFeedback().getNextDecisionIdAndIncrement();
        _text = text;
        _decisionType = decisionType;
        _decidingPlayerId = player.getPlayerId();
    }

    public AbstractAwaitingDecision(Player player, DecisionContext context, AwaitingDecisionType decisionType,
                                    DefaultGame cardGame) {
        _decisionId = cardGame.getUserFeedback().getNextDecisionIdAndIncrement();
        _text = context.getClientText();
        _decisionType = decisionType;
        _decidingPlayerId = player.getPlayerId();
    }


    @Override
    public int getDecisionId() {
        return _decisionId;
    }

    @Override
    public String getText() {
        return _text;
    }

    @Override
    public AwaitingDecisionType getDecisionType() {
        return _decisionType;
    }

    public String getDecidingPlayerId() { return _decidingPlayerId; }
}