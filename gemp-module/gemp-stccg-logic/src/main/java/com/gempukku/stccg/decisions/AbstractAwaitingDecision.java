package com.gempukku.stccg.decisions;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

public abstract class AbstractAwaitingDecision implements AwaitingDecision {
    private final int _decisionId;
    private final String _text;
    private final String _decidingPlayerId;

    public AbstractAwaitingDecision(String playerName, String text,
                                    DefaultGame cardGame) {
        _decisionId = cardGame.getUserFeedback().getNextDecisionIdAndIncrement();
        _text = text;
        _decidingPlayerId = playerName;
    }

    public AbstractAwaitingDecision(Player player, String text,
                                    DefaultGame cardGame) {
        _decisionId = cardGame.getUserFeedback().getNextDecisionIdAndIncrement();
        _text = text;
        _decidingPlayerId = player.getPlayerId();
    }

    public AbstractAwaitingDecision(String playerName, DecisionContext context,
                                    DefaultGame cardGame) {
        _decisionId = cardGame.getUserFeedback().getNextDecisionIdAndIncrement();
        _text = context.getClientText();
        _decidingPlayerId = playerName;
    }

    public AbstractAwaitingDecision(Player player, DecisionContext context,
                                    DefaultGame cardGame) {
        _decisionId = cardGame.getUserFeedback().getNextDecisionIdAndIncrement();
        _text = context.getClientText();
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

    public String getDecidingPlayerId() { return _decidingPlayerId; }
}