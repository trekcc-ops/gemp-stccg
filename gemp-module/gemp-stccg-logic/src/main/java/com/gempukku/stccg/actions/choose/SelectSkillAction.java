package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectSkillAction extends ActionyAction {
    private final Map<String, SkillName> _skillOptions = new HashMap<>();
    private SkillName _selectedSkill;

    public SelectSkillAction(DefaultGame cardGame, String performingPlayerName, List<SkillName> skillOptions) {
        super(cardGame, performingPlayerName, ActionType.SELECT_SKILL);
        for (SkillName skill : skillOptions)
            _skillOptions.put(skill.get_humanReadable(), skill);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws PlayerNotFoundException {
        cardGame.sendAwaitingDecision(
                new MultipleChoiceAwaitingDecision(cardGame.getPlayer(_performingPlayerId), "Choose a skill",
                        _skillOptions.keySet(), cardGame) {
                    @Override
                    protected void validDecisionMade(int index, String result) {
                        _selectedSkill = _skillOptions.get(result);
                        setAsSuccessful();
                    }
                });
        return getNextAction();
    }

}