package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.UnrespondableEffect;

import java.util.List;

public abstract class ChooseAwayTeamEffect extends UnrespondableEffect {
    protected final String _playerId;
    protected final List<String> _awayTeams;

    public ChooseAwayTeamEffect(DefaultGame game, String playerId, List<String> awayTeams) {
        super(game, playerId);
        _playerId = playerId;
        _awayTeams = awayTeams;
    }

    @Override
    public void doPlayEffect() {
        if (_awayTeams.size() == 1)
            awayTeamChosen(_awayTeams.getFirst());
        else
            _game.getUserFeedback().sendAwaitingDecision(
                    new MultipleChoiceAwaitingDecision(_game.getPlayer(_playerId), "Choose an Away Team", _awayTeams) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            awayTeamChosen(result);
                        }
                    });
    }

    protected abstract void awayTeamChosen(String awayTeam);
}