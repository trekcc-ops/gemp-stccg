package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.CardWithHullIntegrity;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ShieldsEvaluator extends Evaluator {

    @JsonProperty("cardWithShieldsId")
    private final int _cardWithShieldsId;

    public ShieldsEvaluator(CardWithHullIntegrity cardWithShields) {
        _cardWithShieldsId = cardWithShields.getCardId();
    }

    public float evaluateExpression(DefaultGame cardGame) {
        try {
            PhysicalCard cardWithShields = cardGame.getCardFromCardId(_cardWithShieldsId);
            if (cardWithShields instanceof CardWithHullIntegrity hullCard) {
                return hullCard.getShields(cardGame);
            } else {
                cardGame.sendErrorMessage("Unable to calculate SHIELDS for card with id " + _cardWithShieldsId);
            }
        } catch(CardNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
        }
        return 0;
    }
}