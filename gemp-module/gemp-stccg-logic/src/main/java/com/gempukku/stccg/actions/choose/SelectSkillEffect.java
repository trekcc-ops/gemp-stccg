package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SelectSkillEffect extends UnrespondableEffect {
    private final String _playerId;
    private final Map<String, SkillName> _skillOptions = new HashMap<>();
    public SelectSkillEffect(DefaultGame game, Player player, List<SkillName> skillOptions) {
        super(game);
        _playerId = player.getPlayerId();
        for (SkillName skill : skillOptions)
            _skillOptions.put(skill.get_humanReadable(), skill);
    }

    @Override
    public void doPlayEffect() {
        _game.getUserFeedback().sendAwaitingDecision(
                new MultipleChoiceAwaitingDecision(_game.getPlayer(_playerId), "Choose a skill", _skillOptions.keySet()) {
                    @Override
                    protected void validDecisionMade(int index, String result) {
                        skillChosen(_skillOptions.get(result));
                    }
                });
    }

    protected abstract void skillChosen(SkillName skill);
}