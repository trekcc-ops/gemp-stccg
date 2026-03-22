package com.gempukku.stccg.modifiers.attributes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.AbstractModifier;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.TrueCondition;

public class WeaponsDisabledModifier extends AbstractModifier {

    @JsonCreator
    private WeaponsDisabledModifier(@JsonProperty("affectedCards") CardFilter affectFilter,
                                    @JsonProperty("condition") Condition condition,
                                    @JsonProperty("effectType") ModifierEffect effectType) {
        super(affectFilter, condition, effectType, false);
    }

    public WeaponsDisabledModifier(CardFilter affectedCards) {
        this(affectedCards, new TrueCondition(), ModifierEffect.WEAPONS_DISABLED_MODIFIER);
    }


    @Override
    public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard) {
        return "Weapons disabled while no matching personnel aboard";
    }
}