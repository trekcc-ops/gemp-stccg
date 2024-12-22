package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.Condition;

import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

public class GainSkillModifier extends AbstractModifier implements SkillAffectingModifier {
    private final List<SkillName> _skills = new LinkedList<>();

    public GainSkillModifier(PhysicalCard actionSource, Filterable affectFilter, Condition condition,
                             SkillName... skills) {
                // TODO - Need to set cumulative = false as the default, and implement what that means
                // TODO - This method doesn't really do anything right now
        super(actionSource, null, affectFilter, condition, ModifierEffect.GAIN_SKILL_MODIFIER);
        for (SkillName skill : skills)
            _skills.add(skill);
    }

    @Override
    public String getCardInfoText(PhysicalCard affectedCard) {
        StringJoiner sj = new StringJoiner(", ");
        for (SkillName skill : _skills)
            sj.add(skill.get_humanReadable());
        return "Gains " + sj + " from " + _cardSource.getCardLink();
    }

    public List<SkillName> getSkills() {
        return _skills;
    }
}