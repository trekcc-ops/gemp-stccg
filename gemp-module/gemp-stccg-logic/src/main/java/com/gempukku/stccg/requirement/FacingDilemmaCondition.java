package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class FacingDilemmaCondition implements Condition {

    @JsonProperty("cardId")
    private final int _cardId;

    public FacingDilemmaCondition(PhysicalCard card) {
        _cardId = card.getCardId();
    }

    @Override
    public boolean isFulfilled(DefaultGame cardGame) {
        try {
            PhysicalCard card = cardGame.getCardFromCardId(_cardId);
            if (card instanceof PersonnelCard personnelCard) {
                return personnelCard.isFacingADilemma(cardGame);
            }
        } catch(CardNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
        }
        return false;
    }
}