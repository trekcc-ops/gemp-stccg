package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.GainSkillModifier;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.Modifiers;
import com.gempukku.stccg.player.YouPlayerResolver;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.HigherScoreThanAllOtherPlayersCondition;

import java.util.LinkedList;
import java.util.List;

import static com.gempukku.stccg.modifiers.ModifierTimingType.WHILE_IN_PLAY;


public class Blueprint007_082 extends CardBlueprint {

    // Letek (2E)

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(DefaultGame cardGame,
                                                                           PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();
        Condition condition = new HigherScoreThanAllOtherPlayersCondition(new YouPlayerResolver(thisCard));
        CardFilter affectedCard = Filters.card(thisCard);
        modifiers.add(Modifiers.allPersonnelAttributes(thisCard, affectedCard, condition, 1, WHILE_IN_PLAY));
        modifiers.add(new GainSkillModifier(thisCard, Filters.card(thisCard), condition, SkillName.MEDICAL, SkillName.TREACHERY));
        return modifiers;
    }

}