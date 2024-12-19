package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.ST1EGameState;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_155_026_GetItDone_Test extends AbstractAtTest {

    private PersonnelCard picard;
    private PhysicalShipCard runabout;
    private PhysicalCard cardToDiscard;
    private List<PhysicalCard> wallaces = new LinkedList<>();

    @Test
    public void actionOption1() throws DecisionResultInvalidException, CardNotFoundException {
        runGameUntilActionSelection();
        playerDecided(P1, "0");
        selectCard(P1, cardToDiscard);
        assertEquals(11, picard.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(10, picard.getAttribute(CardAttribute.CUNNING));
        assertEquals(8, picard.getAttribute(CardAttribute.STRENGTH));
        assertFalse(canUseCardAgain());
        skipExecuteOrders();
        assertEquals(P2, _game.getCurrentPlayerId());
        skipCardPlay();
        skipExecuteOrders();
        assertEquals(P1, _game.getCurrentPlayerId());
        skipCardPlay();
        assertTrue(canUseCardAgain());
    }

    @Test
    public void actionOption2() throws DecisionResultInvalidException, CardNotFoundException {
        runGameUntilActionSelection();
        playerDecided(P1, "1");
        selectCard(P1, cardToDiscard);
        assertEquals(9, runabout.getFullRange());
        assertFalse(canUseCardAgain());
        skipExecuteOrders();
        assertEquals(P2, _game.getCurrentPlayerId());
        skipCardPlay();
        skipExecuteOrders();
        assertEquals(P1, _game.getCurrentPlayerId());
        skipCardPlay();
        assertTrue(canUseCardAgain());
    }

    @Test
    public void actionOption3() throws DecisionResultInvalidException, CardNotFoundException {
        runGameUntilActionSelection();
        playerDecided(P1, "2");
        selectCard(P1, cardToDiscard);
        for (PhysicalCard wallace : wallaces) {
            assertEquals(Zone.DRAW_DECK, wallace.getZone());
        }
        assertFalse(canUseCardAgain());
        skipExecuteOrders();
        assertEquals(P2, _game.getCurrentPlayerId());
        skipCardPlay();
        skipExecuteOrders();
        assertEquals(P1, _game.getCurrentPlayerId());
        skipCardPlay();
        assertTrue(canUseCardAgain());
    }



    @SuppressWarnings("SpellCheckingInspection")
    public void runGameUntilActionSelection() throws CardNotFoundException, DecisionResultInvalidException {

        initializeGameWithAttentionAllHands();
        ST1EGameState gameState = _game.getGameState();
        autoSeedMissions();
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA) skipDilemma();
        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());

        final PhysicalCard getItDone = newCardForGame("155_026", P1);
        gameState.addCardToZone(getItDone, Zone.TABLE);
        assertTrue(getItDone.isInPlay());

        autoSeedFacility();

        for (int i = 0; i <= 2; i++) {
            wallaces.add(newCardForGame("101_203", P1));
        }
        assertEquals(3, wallaces.size());

        gameState.addCardToZone(wallaces.get(0), Zone.DISCARD, EndOfPile.TOP); // Darian Wallace
        gameState.addCardToZone(newCardForGame("156_010", P1), Zone.DISCARD, EndOfPile.TOP); // Surprise Party
        gameState.addCardToZone(wallaces.get(1), Zone.DISCARD, EndOfPile.TOP); // Darian Wallace
        gameState.addCardToZone(wallaces.get(2), Zone.DISCARD, EndOfPile.TOP); // Darian Wallace
        gameState.addCardToZone(newCardForGame("156_010", P1), Zone.DISCARD, EndOfPile.TOP); // Surprise Party
        gameState.addCardToZone(newCardForGame("156_010", P1), Zone.DISCARD, EndOfPile.TOP); // Surprise Party
        gameState.addCardToZone(newCardForGame("101_236", P1), Zone.DISCARD, EndOfPile.TOP); // Simon Tarses
        gameState.addCardToZone(newCardForGame("101_236", P1), Zone.DISCARD, EndOfPile.TOP); // Simon Tarses
        gameState.addCardToZone(newCardForGame("101_236", P1), Zone.DISCARD, EndOfPile.TOP); // Simon Tarses

        assertEquals(9, gameState.getDiscard(P1).size());

        _outpost = null;
        for (PhysicalCard card : _game.getGameState().getAllCardsInGame()) {
            if (Objects.equals(card.getTitle(), "Federation Outpost") && card instanceof FacilityCard facility)
                _outpost = facility;
        }
        assertNotNull(_outpost);

        picard = (PersonnelCard) newCardForGame("101_215", P1);
        runabout = (PhysicalShipCard) newCardForGame("101_331", P1);
        picard.reportToFacility(_outpost);
        runabout.reportToFacility(_outpost);

        skipCardPlay();

        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());
        assertTrue(getItDone.isControlledBy(P1));
        assertEquals(P1, _game.getCurrentPlayerId());

        useGameText(getItDone, P1);

        List<PhysicalCard> cardsToPlace = new LinkedList<>();
        for (PhysicalCard card : gameState.getHand(P1)) {
            if (card.hasIcon(_game, CardIcon.TNG_ICON) && cardsToPlace.size() < 2) {
                cardsToPlace.add(card);
            }
        }

        selectCards(P1, cardsToPlace);

        for (int i = 0; i < 5; i++) {
            gameState.addCardToZone(newCardForGame("101_236", P1), Zone.HAND); // Simon Tarses
        }

        assertNotNull(_userFeedback.getAwaitingDecision(P1));
        Player player1 = _game.getPlayer(P1);
        Collection<PhysicalCard> discardable = Filters.filter(_game, Filters.yourHand(player1), CardIcon.TNG_ICON);
        for (PhysicalCard card : discardable) {
            cardToDiscard = card;
        }
    }

    private boolean canUseCardAgain() {
        boolean result = false;
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(P1);
        if (decision instanceof CardActionSelectionDecision actionSelection) {
            for (Action action : actionSelection.getActions()) {
                if (Objects.equals(action.getActionSource().getTitle(), "Get It Done")) {
                    result = true;
                }
            }
        }
        return result;
    }

}