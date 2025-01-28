package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.blueprints.resolver.YouPlayerResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.condition.HigherScoreThanAllOtherPlayersCondition;
import com.gempukku.stccg.modifiers.GainSkillModifier;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.AllAttributeModifier;

import java.util.LinkedList;
import java.util.List;


public class Blueprint007_082 extends CardBlueprint {

    // Letek (2E)

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();
        Condition condition = new HigherScoreThanAllOtherPlayersCondition(new YouPlayerResolver(thisCard));
        modifiers.add(new AllAttributeModifier(thisCard, thisCard, condition, 1));
        modifiers.add(new GainSkillModifier(thisCard, thisCard, condition, SkillName.MEDICAL, SkillName.TREACHERY));
        return modifiers;
    }

}