package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_155_026_GetItDone_Test extends AbstractAtTest {

    private PersonnelCard picard;
    private ShipCard runabout;
    private PhysicalCard cardToDiscard;
    private PhysicalCard getItDone;
    private List<PhysicalCard> wallaces;

    @SuppressWarnings("SpellCheckingInspection")
    public void initializeGame(boolean hasCardsInHand) throws CardNotFoundException, DecisionResultInvalidException, InvalidGameOperationException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.addCardToCoreAsSeeded("155_022", "Continuing Mission", P1);
        getItDone = builder.addCardToCoreAsSeeded("155_026", "Get It Done", P1);
        wallaces = new ArrayList<>();
        wallaces.add(builder.addCardToTopOfDiscard("101_203", "Darian Wallace", P1));
        builder.addCardToTopOfDiscard("156_010", "Surprise Party", P1);
        wallaces.add(builder.addCardToTopOfDiscard("101_203", "Darian Wallace", P1));
        wallaces.add(builder.addCardToTopOfDiscard("101_203", "Darian Wallace", P1));
        builder.addCardToTopOfDiscard("156_010", "Surprise Party", P1);
        builder.addCardToTopOfDiscard("156_010", "Surprise Party", P1);
        builder.addCardToTopOfDiscard("101_236", "Simon Tarses", P1);
        builder.addCardToTopOfDiscard("101_236", "Simon Tarses", P1);
        builder.addCardToTopOfDiscard("101_236", "Simon Tarses", P1);
        builder.addMission("101_154", "Excavation", P1);
        FacilityCard outpost = builder.addOutpost(Affiliation.FEDERATION, P1);
        runabout = builder.addDockedShip("101_331", "Runabout", P1, outpost);
        picard = builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P1, outpost, PersonnelCard.class);

        if (hasCardsInHand) {
            builder.addCardInHand("101_236", "Simon Tarses", P1);
            builder.addCardInHand("101_236", "Simon Tarses", P1);
            builder.addCardInHand("101_236", "Simon Tarses", P1);
            builder.addCardInHand("101_236", "Simon Tarses", P1);
            builder.addCardInHand("101_236", "Simon Tarses", P1);
        }

        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    private void initiateGetItDoneAction() throws InvalidGameOperationException, DecisionResultInvalidException {
        useGameText(getItDone, P1);
        Player player1 = _game.getPlayer(P1);

        List<PhysicalCard> cardsToPlace = new LinkedList<>();
        for (PhysicalCard card : player1.getCardsInHand()) {
            if (card.hasIcon(_game, CardIcon.TNG_ICON) && cardsToPlace.size() < 2) {
                cardsToPlace.add(card);
            }
        }

        selectCards(P1, cardsToPlace);

        assertNotNull(_game.getAwaitingDecision(P1));
        Collection<PhysicalCard> discardable = Filters.filter(_game, Filters.yourHand(player1), CardIcon.TNG_ICON);
        for (PhysicalCard card : discardable) {
            cardToDiscard = card;
        }
    }

    @Test
    public void actionOption1() throws DecisionResultInvalidException, CardNotFoundException, JsonProcessingException,
            InvalidGameOperationException {
        initializeGame(true);
        initiateGetItDoneAction();
        playerDecided(P1, "0");
        selectCard(P1, cardToDiscard);
        assertEquals(11, picard.getIntegrity(_game));
        assertEquals(10, picard.getCunning(_game));
        assertEquals(8, picard.getStrength(_game));
        assertFalse(canUseCardAgain());
        skipToNextTurnAndPhase(P1, Phase.EXECUTE_ORDERS);
        assertTrue(canUseCardAgain());
        String gameStateString = _game.getGameState().serializeComplete();
    }

    @Test
    public void actionOption2() throws DecisionResultInvalidException, CardNotFoundException,
            InvalidGameLogicException, JsonProcessingException, PlayerNotFoundException, InvalidGameOperationException {
        initializeGame(true);
        initiateGetItDoneAction();
        playerDecided(P1, "1");
        selectCard(P1, cardToDiscard);
        assertEquals(9, runabout.getFullRange(_game));
        assertFalse(canUseCardAgain());
        skipToNextTurnAndPhase(P1, Phase.EXECUTE_ORDERS);
        assertEquals(7, runabout.getFullRange(_game)); // confirm that runabout's range is back to normal
        assertTrue(canUseCardAgain());
        String gameStateString = _game.getGameState().serializeComplete();
    }

    @Test
    public void actionOption3() throws DecisionResultInvalidException, CardNotFoundException,
            JsonProcessingException, InvalidGameOperationException {
        initializeGame(true);
        initiateGetItDoneAction();
        playerDecided(P1, "2");
        selectCard(P1, cardToDiscard);
        for (PhysicalCard wallace : wallaces) {
            assertEquals(Zone.DRAW_DECK, wallace.getZone());
        }
        assertFalse(canUseCardAgain());
        skipToNextTurnAndPhase(P1, Phase.EXECUTE_ORDERS);
        assertTrue(canUseCardAgain());
        String gameStateString = _game.getGameState().serializeComplete();
    }

    private boolean canUseCardAgain() {
        boolean result = false;
        AwaitingDecision decision = _game.getAwaitingDecision(P1);
        if (decision instanceof ActionSelectionDecision actionSelection) {
            for (TopLevelSelectableAction action : actionSelection.getActions()) {
                PhysicalCard cardSource = action.getPerformingCard();
                if (cardSource != null && Objects.equals(cardSource.getTitle(), "Get It Done")) {
                    result = true;
                }
            }
        }
        return result;
    }

}