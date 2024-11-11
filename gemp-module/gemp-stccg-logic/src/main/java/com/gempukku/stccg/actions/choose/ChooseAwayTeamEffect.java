package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.Player;

import java.util.List;

public abstract class ChooseAwayTeamEffect extends UnrespondableEffect {
    protected final String _playerId;
    protected final List<String> _awayTeams;

    public ChooseAwayTeamEffect(Player player, List<String> awayTeams) {
        super(player);
        _playerId = player.getPlayerId();
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