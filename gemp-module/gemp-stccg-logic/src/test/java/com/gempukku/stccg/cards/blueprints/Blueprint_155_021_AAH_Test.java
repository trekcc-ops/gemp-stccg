package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.playcard.SelectAndReportForFreeCardAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.CardSelectionDecision;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_155_021_AAH_Test extends AbstractAtTest {

    @Test
    public void testWithAndWithoutLarsonInPlay() throws InvalidGameOperationException, PlayerNotFoundException, DecisionResultInvalidException, InvalidGameLogicException, CardNotFoundException {
        testThis(true);
        testThis(false);
    }

    public void testThis(boolean larsonInPlay) throws CardNotFoundException, DecisionResultInvalidException, InvalidGameLogicException, PlayerNotFoundException, InvalidGameOperationException {

        initializeGameWithAttentionAllHands();
        ST1EGameState gameState = _game.getGameState();
        autoSeedMissions();
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA) skipDilemma();
        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());

        List<PhysicalCard> playableCards = new ArrayList<>();
        PhysicalCard lopez = newCardForGame("155_063", P1); // Lopez
        playableCards.add(lopez);
        PhysicalCard lopez2 = newCardForGame("155_063", P1); // Lopez
        playableCards.add(newCardForGame("101_203", P1)); // Darian Wallace

        List<PhysicalCard> unPlayableCards = new ArrayList<>();
        unPlayableCards.add(newCardForGame("101_215", P1)); // Jean-Luc Picard (unique)
        unPlayableCards.add(newCardForGame("112_207", P1)); // Jace Michaels (no TNG icon)
        unPlayableCards.add(newCardForGame("116_095", P1)); // R'Mal (no matching outpost)

        for (PhysicalCard card : playableCards) {
            _game.getPlayer(P1).getDrawDeck().addCardToTop(card);
        }

        for (PhysicalCard card : unPlayableCards) {
            _game.getPlayer(P1).getDrawDeck().addCardToTop(card);
        }


        autoSeedFacility();
        PhysicalCard attention = null;
        _outpost = null;
        for (PhysicalCard card : _game.getGameState().getAllCardsInGame()) {
            if (Objects.equals(card.getTitle(), "Federation Outpost") && card instanceof FacilityCard facility)
                _outpost = facility;
            if (Objects.equals(card.getTitle(), "Attention All Hands") && card.getOwnerName().equals(P1))
                attention = card;
        }
        assertNotNull(_outpost);
        assertNotNull(attention);
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());
        assertEquals(1, gameState.getCurrentTurnNumber());

        /* TODO - The commented out code below was intended to test the "you do not have this card in play already"
                filter in AAH's free report. Currently not able to do it because this test does not give an
                opportunity for Linda to be put into play. She needs to report to a facility. The facility is
                played during the autoSeedFacility() method above, but at the end of that method, the valid
                reportable cards for AAH have already been identified. Low priority at the moment since testing
                may or may not be getting an overhaul from ketura.

            */
/*
        PhysicalCard newLarson = newCardForGame("101_220", P1);
        if (larsonInPlay) {
            unPlayableCards.add(newLarson);
        } else {
            playableCards.add(newLarson);
        } */

/*        if (larsonInPlay) {
            PersonnelCard larsonAlreadyInPlay = (PersonnelCard) newCardForGame("101_220", P1);
            larsonAlreadyInPlay.reportToFacility(_outpost);
            assertTrue(larsonAlreadyInPlay.isInPlay());
        } */

        selectAction(SelectAndReportForFreeCardAction.class, attention, P1);

        assertTrue(CardSelectionDecision.class.isAssignableFrom(_userFeedback.getAwaitingDecision(P1).getClass()));
        CardSelectionDecision cardDecision = (CardSelectionDecision) _userFeedback.getAwaitingDecision(P1);

        List<String> selectableCards = List.of(cardDecision.getCardIds());

        for (PhysicalCard card : playableCards) {
            assertTrue(selectableCards.contains(String.valueOf(card.getCardId())));
        }

        for (PhysicalCard card : unPlayableCards) {
            assertFalse(selectableCards.contains(String.valueOf(card.getCardId())));
        }

        assertFalse(lopez.isInPlay());
        // Select a card for normal card play
        selectCard(P1, lopez);
        assertTrue(lopez.isInPlay());

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