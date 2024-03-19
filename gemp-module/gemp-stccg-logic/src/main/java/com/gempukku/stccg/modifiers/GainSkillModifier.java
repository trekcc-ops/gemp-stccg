package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.Condition;

public class GainSkillModifier extends AbstractModifier {
    private final SkillName _skill;

    public GainSkillModifier(PhysicalCard actionSource, Filterable affectFilter, Condition condition, SkillName skillName) {
                // TODO - Need to set cumulative = false as the default, and implement what that means
                // TODO - This method doesn't really do anything right now
        super(actionSource, null, affectFilter, condition, ModifierEffect.GAIN_ICON_MODIFIER);
        _skill = skillName;
/*        _context = context;
        _icon = icon;
        _valueSource = valueSource;*/
    }

    @Override
    public String getCardInfoText(PhysicalCard affectedCard) {
        return "Gains " + _skill.get_humanReadable() + " from " + _cardSource.getCardLink();
    }
}
