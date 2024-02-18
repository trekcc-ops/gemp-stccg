package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.defaulteffect.AttemptMissionEffect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

public abstract class ChooseAwayTeamEffect extends UnrespondableEffect {
    protected final String _playerId;
    protected final DefaultGame _game;
    protected List<String> _awayTeams;

    public ChooseAwayTeamEffect(DefaultGame game, String playerId, List<String> awayTeams) {
        _playerId = playerId;
        _game = game;
        _awayTeams = awayTeams;
    }

    @Override
    public void doPlayEffect() {
        if (_awayTeams.size() == 1)
            awayTeamChosen(_awayTeams.get(0));
        else
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision(1, "Choose an Away Team", _awayTeams) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            awayTeamChosen(result);
                        }
                    });
    }

    protected abstract void awayTeamChosen(String awayTeam);
}
