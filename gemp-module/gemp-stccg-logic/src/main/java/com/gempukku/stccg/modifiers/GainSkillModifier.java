package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

public class GainSkillModifier extends AbstractModifier implements SkillAffectingModifier {
    private final List<SkillName> _skills = new LinkedList<>();

    public GainSkillModifier(PhysicalCard actionSource, Filterable affectFilter, Condition condition,
                             SkillName skill, ModifierTimingType timingType) {
        super(actionSource, Filters.changeToFilter(affectFilter), condition, ModifierEffect.GAIN_SKILL_MODIFIER, false);
        _skills.add(skill);
    }


    public GainSkillModifier(PhysicalCard actionSource, Filterable affectFilter, Condition condition,
                             SkillName... skills) {
        super(actionSource, Filters.changeToFilter(affectFilter), condition, ModifierEffect.GAIN_SKILL_MODIFIER, false);
        for (SkillName skill : skills)
            _skills.add(skill);
    }

    @Override
    public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard) {
        StringJoiner sj = new StringJoiner(", ");
        for (SkillName skill : _skills)
            sj.add(skill.get_humanReadable());
        String message = "Gains " + sj;
        if (_cardSource != null) {
            message = message + " from " + _cardSource.getCardLink();
        }
        return message;
    }

    public List<SkillName> getSkills() {
        return _skills;
    }
}