package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_163_036_BEtor_Test extends AbstractAtTest {

    private MissionCard _mission;
    private PersonnelCard crosis;
    private PersonnelCard betor;
    private PhysicalCard lursa;
    private FacilityCard outpost;
    private FacilityCard outpost2;
    private PhysicalCard lursa2;

    private void initializeGame(Phase startingPhase, boolean lursaInPlayAlready, boolean twoLursas)
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_170", "Investigate Raid", P1);
        outpost = builder.addFacility("101_104", P1); // Federation Outpost
        outpost2 = builder.addFacility("101_104", P2);
        crosis = builder.addCardInHand("163_044", "Lieutenant Crosis", P2, PersonnelCard.class);
        lursa = builder.addDrawDeckCard("101_280", "Lursa", P1);
        if (twoLursas) {
            lursa2 = builder.addDrawDeckCard("163_039", "Lursa", P1);
        }
        if (lursaInPlayAlready) {
            builder.addCardAboardShipOrFacility("101_280", "Lursa", P1, outpost, PersonnelCard.class);
        }
        betor = builder.addCardAboardShipOrFacility("163_036", "B'Etor", P1, outpost, PersonnelCard.class);
        builder.setPhase(startingPhase);
        builder.startGame();
    }

    @Test
    public void downloadTest() throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame(Phase.EXECUTE_ORDERS, false, false);
        useGameText(betor, P1);
        assertTrue(betor.isInPlay());
        selectCard(P1, _mission);
        assertTrue(lursa.isInPlay());
    }

    @Test
    public void twoLursasTest() throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame(Phase.EXECUTE_ORDERS, false, true);
        useGameText(betor, P1);
        assertTrue(betor.isInPlay());
        assertTrue(getSelectableCards(P1).containsAll(List.of(lursa, lursa2)));
        selectCard(P1, lursa2);
        selectCard(P1, _mission);
        assertTrue(lursa2.isInPlay());
    }


    @Test
    public void cannotDownloadIfIncompatibleTest() throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame(Phase.EXECUTE_ORDERS, false, false);
        useGameText(betor, P1);
        assertThrows(DecisionResultInvalidException.class, () -> selectCard(P1, outpost));
    }

    @Test
    public void canDownloadToOpponentsOutpostTest() throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame(Phase.EXECUTE_ORDERS, false, false);
        useGameText(betor, P1);
        selectCard(P1, outpost2);
    }


    @Test
    public void downloadDuringCardPlayTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame(Phase.CARD_PLAY, false, false);
        useGameText(betor, P1);
        assertTrue(betor.isInPlay());
        selectCard(P1, _mission);
        assertTrue(lursa.isInPlay());
    }

    @Test
    public void uniquenessTest() throws CardNotFoundException, InvalidGameOperationException {
        initializeGame(Phase.CARD_PLAY, true, false);

        // Verify that you can't use B'Etor's special skill because Lursa is already in play
        assertThrows(DecisionResultInvalidException.class, () -> useGameText(betor, P1));
    }

    @Test
    public void downloadDuringOpponentsTurnTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame(Phase.CARD_PLAY, false, false);
        skipToNextTurnAndPhase(P2, Phase.CARD_PLAY);
        playCard(P2, crosis);
        useGameText(betor, P1);
        selectCard(P1, _mission);
        assertTrue(lursa.isInPlay());
    }


}