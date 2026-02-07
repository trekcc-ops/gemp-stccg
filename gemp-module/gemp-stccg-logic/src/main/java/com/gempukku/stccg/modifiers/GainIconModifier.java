package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Condition;

public class GainIconModifier extends AbstractModifier implements IconAffectingModifier {

    @JsonProperty("icon")
    private final CardIcon _icon;

    @JsonCreator
    private GainIconModifier(@JsonProperty("performingCard") PhysicalCard performingCard,
                                          @JsonProperty("affectedCards") CardFilter affectFilter,
                                          @JsonProperty("condition") Condition condition,
                                          @JsonProperty("effectType") ModifierEffect effectType,
                             @JsonProperty("icon") CardIcon icon) {
        super(performingCard, affectFilter, condition, effectType);
        _icon = icon;
    }


    public GainIconModifier(PhysicalCard performingCard, Filterable affectFilter, Condition condition, CardIcon icon) {
        this(performingCard, Filters.changeToFilter(affectFilter), condition, ModifierEffect.GAIN_ICON_MODIFIER, icon);
    }

    @Override
    public CardIcon getIcon() {
        return _icon;
    }

    @Override
    public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard) {
        String message = "Gains " + _icon.toHTML();
        if (_cardSource != null) {
            message = message + " from " + _cardSource.getCardLink();
        }
        return message;
    }

    @Override
    public boolean hasIcon(PhysicalCard physicalCard, CardIcon icon) {
        return (icon == _icon);
    }
}