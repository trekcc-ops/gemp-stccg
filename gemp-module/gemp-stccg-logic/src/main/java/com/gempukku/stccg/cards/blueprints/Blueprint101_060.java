package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.TrueCondition;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.GainSkillModifier;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.LinkedList;
import java.util.List;


@SuppressWarnings("unused")
public class Blueprint101_060 extends CardBlueprint {
    Blueprint101_060() {
        super("101_060"); // Medical Kit
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(Player player, final PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();
        Filterable affectFilter = Filters.and(Filters.yourCardsPresentWith(player, thisCard), CardType.PERSONNEL,
                Filters.classification(SkillName.OFFICER));
        modifiers.add(new GainSkillModifier(thisCard, affectFilter, new TrueCondition(), SkillName.MEDICAL));
        return modifiers;
    }
}