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
    private final Map<String, String[]> _params = new HashMap<>();
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



    final void setParam(String name, String value) {
        setParam(name, new String[] {value});
    }
    final void setParam(String name, int value) {
        String stringValue = String.valueOf(value);
        setParam(name, new String[] {stringValue});
    }

    final void setParam(String name, String[] value) {
        _params.put(name, value);
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