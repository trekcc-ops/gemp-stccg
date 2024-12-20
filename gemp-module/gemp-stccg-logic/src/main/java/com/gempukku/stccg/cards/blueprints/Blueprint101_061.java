package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.TrueCondition;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.modifiers.GainSkillModifier;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.LinkedList;
import java.util.List;


@SuppressWarnings("unused")
public class Blueprint101_061 extends CardBlueprint {
    Blueprint101_061() {
        super("101_061"); // Medical Tricorder
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();
        Filterable affectFilter = Filters.and(Filters.yourCardsPresentWithThisCard(thisCard), CardType.PERSONNEL,
                Filters.classification(SkillName.SCIENCE));
        modifiers.add(new GainSkillModifier(thisCard, affectFilter, new TrueCondition(), SkillName.MEDICAL));
        return modifiers;
    }
}