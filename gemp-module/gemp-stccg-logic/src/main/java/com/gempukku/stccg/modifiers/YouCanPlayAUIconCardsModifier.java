package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.TrueCondition;

public class YouCanPlayAUIconCardsModifier extends AbstractModifier {

    @JsonCreator
    private YouCanPlayAUIconCardsModifier(@JsonProperty("performingCard") PhysicalCard performingCard,
                                        @JsonProperty("affectedCards") CardFilter affectFilter,
                                        @JsonProperty("condition") Condition condition,
                                        @JsonProperty("effectType") ModifierEffect effectType) {
        super(performingCard, affectFilter, condition, effectType, false);
    }

    public YouCanPlayAUIconCardsModifier(PhysicalCard performingCard) {
        this(performingCard, Filters.any, new TrueCondition(), ModifierEffect.AU_CARDS_ENTER_PLAY);
    }


    @Override
    public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard) {
        return "Can play [AU] cards";
    }

    public boolean canPlayerPlayAUCards(String playerName) {
        return _cardSource.isControlledBy(playerName);
    }
}