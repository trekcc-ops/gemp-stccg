package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Collection;

public interface ActionCardResolver {

    void resolve(DefaultGame cardGame) throws InvalidGameLogicException;
    boolean isResolved();
    Collection<PhysicalCard> getCards();
    default Collection<PhysicalCard> getCards(DefaultGame cardGame) {
        return getCards();
    }
    SelectCardsAction getSelectionAction();

    boolean cannotBeResolved(DefaultGame cardGame);

}