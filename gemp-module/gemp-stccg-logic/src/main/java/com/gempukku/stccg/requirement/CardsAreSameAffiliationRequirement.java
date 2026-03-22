package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.CardWithAffiliations;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CardsAreSameAffiliationRequirement implements Requirement {

    private final String _card1;
    private final String _card2;

    public CardsAreSameAffiliationRequirement(@JsonProperty("card1") String card1,
            @JsonProperty("card2") String card2) {
        _card1 = card1;
        _card2 = card2;
    }

    public boolean accepts(GameTextContext actionContext, DefaultGame cardGame) {
        Collection<Integer> card1 = actionContext.getCardIdsFromMemory(_card1);
        Collection<Integer> card2 = actionContext.getCardIdsFromMemory(_card2);

        Collection<PhysicalCard> cards1 = new ArrayList<>();
        Collection<PhysicalCard> cards2 = new ArrayList<>();

        for (Integer cardId : card1) {
            try {
                PhysicalCard card = cardGame.getCardFromCardId(cardId);
                if (card != null) {
                    cards1.add(card);
                }
            } catch(CardNotFoundException ignored) {

            }
        }

        for (Integer cardId : card2) {
            try {
                PhysicalCard card = cardGame.getCardFromCardId(cardId);
                if (card != null) {
                    cards2.add(card);
                }
            } catch(CardNotFoundException ignored) {

            }
        }

        if (cards1.isEmpty() || cards2.isEmpty()) {
            return false;
        } else {
            List<PhysicalCard> allCards = new ArrayList<>();
            allCards.addAll(cards1);
            allCards.addAll(cards2);
            for (PhysicalCard checkCard : allCards) {
                for (PhysicalCard checkCard2 : allCards) {
                    if (checkCard instanceof CardWithAffiliations affil1 &&
                            checkCard2 instanceof CardWithAffiliations affil2 &&
                    cardGame instanceof ST1EGame stGame) {
                        if (!(affil1.matchesAffiliationOfCard(stGame, affil2, actionContext.yourName()))) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}