package com.gempukku.stccg.actions.choose;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.NonEmptyListFilter;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public interface SelectCardsAction extends Action {

    @JsonProperty("selectedCards")
    @JsonIdentityReference(alwaysAsId=true)
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyListFilter.class)
    Collection<PhysicalCard> getSelectedCards();

    @JsonProperty("selectableCards")
    @JsonIdentityReference(alwaysAsId=true)
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyListFilter.class)
    Collection<? extends PhysicalCard> getSelectableCards(DefaultGame cardGame);

    @JsonProperty("minimum")
    int getMinimum();

    @JsonProperty("maximum")
    int getMaximum();

}