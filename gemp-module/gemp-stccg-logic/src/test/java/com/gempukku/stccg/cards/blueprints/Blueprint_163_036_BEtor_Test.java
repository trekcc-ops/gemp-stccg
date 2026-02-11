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

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_163_036_BEtor_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private MissionCard _mission;
    private MissionCard mission2;
    private PersonnelCard crosis;
    private PersonnelCard betor;
    private PhysicalCard lursa;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_170", "Investigate Raid", P1);
        outpost = builder.addFacility("101_104", P1); // Federation Outpost
        builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P2, _mission);
        crosis = builder.addCardInHand("163_044", "Lieutenant Crosis", P1, PersonnelCard.class);
        lursa = builder.addDrawDeckCard("101_280", "Lursa", P1);
        betor = builder.addCardAboardShipOrFacility("163_036", "B'Etor", P1, outpost, PersonnelCard.class);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void downloadTest() throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame();
        useGameText(betor, P1);
        assertTrue(betor.isInPlay());
        selectCard(P1, _mission);
        assertTrue(lursa.isInPlay());
    }
}