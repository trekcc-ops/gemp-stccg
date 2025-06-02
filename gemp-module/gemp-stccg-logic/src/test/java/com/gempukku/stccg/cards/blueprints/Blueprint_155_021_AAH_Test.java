package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.playcard.SelectAndReportCardAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
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

        List<PhysicalCard> playableCards = new ArrayList<>();
        PhysicalCard lopez = newCardForGame("155_063", P1); // Lopez
        playableCards.add(lopez);
        playableCards.add(newCardForGame("101_203", P1)); // Darian Wallace

        List<PhysicalCard> unPlayableCards = new ArrayList<>();
        unPlayableCards.add(newCardForGame("101_215", P1)); // Jean-Luc Picard (unique)
        unPlayableCards.add(newCardForGame("112_207", P1)); // Jace Michaels (no TNG icon)
        unPlayableCards.add(newCardForGame("116_095", P1)); // R'Mal (no matching outpost)



        PhysicalCard newLarson = newCardForGame("101_220", P1);
        if (larsonInPlay) {
            unPlayableCards.add(newLarson);
        } else {
            playableCards.add(newLarson);
        }


        for (PhysicalCard card : playableCards) {
            gameState.addCardToZoneWithoutSendingToClient(card, Zone.HAND);
        }

        for (PhysicalCard card : unPlayableCards) {
            gameState.addCardToZoneWithoutSendingToClient(card, Zone.HAND);
        }


        if (larsonInPlay) {
            PersonnelCard larsonAlreadyInPlay = (PersonnelCard) newCardForGame("101_220", P1);
            larsonAlreadyInPlay.reportToFacility(_outpost);
            assertTrue(larsonAlreadyInPlay.isInPlay());
        }

        useGameText(attention, P1);
        List<String> selectableCards =
                List.of(_userFeedback.getAwaitingDecision(P1).getDecisionParameters().get("blueprintId"));

        for (PhysicalCard card : playableCards) {
            assertTrue(selectableCards.contains(card.getBlueprintId()));
        }

        for (PhysicalCard card : unPlayableCards) {
            assertFalse(selectableCards.contains(card.getBlueprintId()));
        }

        assertFalse(lopez.isInPlay());
        // Select a card
        selectCard(P1, lopez);
        assertTrue(lopez.isInPlay());

        // Verify that it can't be used twice
        boolean errorThrown = false;
        try {
            selectAction(SelectAndReportCardAction.class, attention, P1);
        } catch(Exception exp) {
            errorThrown = true;
        }
        assertTrue(errorThrown);
    }

}