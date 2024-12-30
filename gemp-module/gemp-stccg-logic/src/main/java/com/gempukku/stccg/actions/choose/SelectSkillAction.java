package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectSkillAction extends ActionyAction {
    private final Map<String, SkillName> _skillOptions = new HashMap<>();
    private SkillName _selectedSkill;
    public SelectSkillAction(Player player, List<SkillName> skillOptions) {
        super(player, ActionType.SELECT_SKILL);
        for (SkillName skill : skillOptions)
            _skillOptions.put(skill.get_humanReadable(), skill);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        cardGame.getUserFeedback().sendAwaitingDecision(
                new MultipleChoiceAwaitingDecision(cardGame.getPlayer(_performingPlayerId), "Choose a skill",
                        _skillOptions.keySet()) {
                    @Override
                    protected void validDecisionMade(int index, String result) {
                        _selectedSkill = _skillOptions.get(result);
                    }
                });
        return getNextAction();
    }

}