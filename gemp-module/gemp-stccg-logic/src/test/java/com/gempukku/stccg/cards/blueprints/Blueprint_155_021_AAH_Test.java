package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.actions.playcard.SelectAndReportForFreeCardAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.CardSelectionDecision;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_155_021_AAH_Test extends AbstractAtTest {

    private PersonnelCard lopez1;
    private List<PhysicalCard> playableCards;
    private List<PhysicalCard> unplayableCards;
    private PhysicalCard attention;

    @SuppressWarnings("SpellCheckingInspection")
    public void initializeGame(boolean larsonInPlay) throws CardNotFoundException, DecisionResultInvalidException,
            InvalidGameOperationException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.addCardToCoreAsSeeded("155_022", "Continuing Mission", P1);
        attention = builder.addCardToCoreAsSeeded("155_021", "Attention All Hands", P1);
        FacilityCard outpost = builder.addFacility("101_104", P1);

        // playable
        lopez1 = builder.addCardInHand("155_063", "Lopez", P1, PersonnelCard.class);
        PhysicalCard lopez2 = builder.addCardInHand("155_063", "Lopez", P1, PersonnelCard.class);
        playableCards = new ArrayList<>();
        playableCards.addAll(List.of(lopez1, lopez2));

        // not playable
        PhysicalCard picard = builder.addCardInHand("101_215", "Jean-Luc Picard", P1, PersonnelCard.class); // unique
        PhysicalCard jace = builder.addCardInHand("112_207", "Jace Michaels", P1, PersonnelCard.class); // no TNG icon
        PhysicalCard rmal = builder.addCardInHand("116_095", "R'Mal", P1, PersonnelCard.class); // no matching outpost
        unplayableCards = new ArrayList<>();
        unplayableCards.addAll(List.of(picard, jace, rmal));

        PhysicalCard larsonInHand = builder.addCardInHand("101_220", "Linda Larson", P1, PersonnelCard.class);

        if (larsonInPlay) {
            builder.addCardAboardShipOrFacility("101_220", "Linda Larson", P1, outpost, PersonnelCard.class);
            unplayableCards.add(larsonInHand);
        } else {
            playableCards.add(larsonInHand);
        }

        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void testWithAndWithoutLarsonInPlay() throws Exception {
        testThis(true);
        testThis(false);
    }

    public void testThis(boolean larsonInPlay) throws Exception {
        initializeGame(larsonInPlay);

        selectAction(SelectAndReportForFreeCardAction.class, attention, P1);

        assertTrue(CardSelectionDecision.class.isAssignableFrom(_game.getAwaitingDecision(P1).getClass()));
        CardSelectionDecision cardDecision = (CardSelectionDecision) _game.getAwaitingDecision(P1);

        List<String> selectableCards = List.of(cardDecision.getCardIds());

        for (PhysicalCard card : playableCards) {
            assertTrue(selectableCards.contains(String.valueOf(card.getCardId())));
        }

        for (PhysicalCard card : unplayableCards) {
            assertFalse(selectableCards.contains(String.valueOf(card.getCardId())));
        }

        // Select a card for normal card play
        selectCard(P1, lopez1);
        assertTrue(lopez1.isInPlay());

        // Verify that it can't be used twice
        boolean errorThrown = false;
        try {
            selectAction(SelectAndReportForFreeCardAction.class, attention, P1);
        } catch(Exception exp) {
            errorThrown = true;
        }
        assertTrue(errorThrown);
    }

}