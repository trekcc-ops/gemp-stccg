package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Collection;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="serialized")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SelectCardsResolver.class, name = "select")
})
public interface ActionCardResolver {

    void resolve(DefaultGame cardGame) throws InvalidGameLogicException;
    boolean isResolved();
    Collection<PhysicalCard> getCards(DefaultGame cardGame) throws InvalidGameLogicException;
    SelectCardsAction getSelectionAction();
    boolean willProbablyBeEmpty(DefaultGame cardGame);

}