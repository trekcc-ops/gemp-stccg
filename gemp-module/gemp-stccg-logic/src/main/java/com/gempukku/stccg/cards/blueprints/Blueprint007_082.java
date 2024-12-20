package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.condition.HigherScoreThanAllOtherPlayersCondition;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.GainSkillModifier;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.AllAttributeModifier;

import java.util.LinkedList;
import java.util.List;


public class Blueprint007_082 extends CardBlueprint {

    // Letek (2E)
    Blueprint007_082() {
        super("007_082");
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(Player player, final PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();
        Condition condition = new HigherScoreThanAllOtherPlayersCondition(player);
        modifiers.add(new AllAttributeModifier(thisCard, thisCard, condition, 1));
        modifiers.add(new GainSkillModifier(thisCard, thisCard, condition, SkillName.MEDICAL, SkillName.TREACHERY));
        return modifiers;
    }

}