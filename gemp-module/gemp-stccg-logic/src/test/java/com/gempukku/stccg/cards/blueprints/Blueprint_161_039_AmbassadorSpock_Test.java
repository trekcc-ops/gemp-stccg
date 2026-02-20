package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_161_039_AmbassadorSpock_Test extends AbstractAtTest {

    private PhysicalCard nefets;
    private FacilityCard outpost;
    private PersonnelCard spock;
    private MissionCard mission;
    private PhysicalCard pardek;
    private PhysicalCard bochra;
    private PhysicalCard jacen;

    private void initializeGame()
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission("101_154", "Excavation", P1);
        outpost = builder.addOutpost(Affiliation.ROMULAN, P1);

        // downloadable cards - Pardek OR any universal [Rom] personnel with INTEGRITY>6
        nefets = builder.addDrawDeckCard("204_025", "Nefets", P1);
        pardek = builder.addCardInHand("101_315", "Pardek", P1);

        // can't be downloaded
        bochra = builder.addDrawDeckCard("101_305", "Bochra", P1); // unique
        jacen = builder.addDrawDeckCard("204_023", "Jacen", P1); // INTEGRITY = 6

        spock = builder.addCardInHand("161_039", "Ambassador Spock", P1, PersonnelCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void downloadTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame();
        playCard(P1, spock);

        // Verify cards you can download
        initiateDownloadAction(P1, spock);
        assertEquals(2, getSelectableCards(P1).size());
        assertTrue(getSelectableCards(P1).containsAll(List.of(nefets, pardek)));
        assertFalse(getSelectableCards(P1).containsAll(List.of(bochra, jacen)));
        selectCard(P1, nefets);

        // Verify that Nefets can be downloaded to mission or outpost
        assertTrue(getSelectableCards(P1).containsAll(List.of(mission, outpost)));
        selectCard(P1, outpost);

        assertTrue(nefets.isInPlay());
        assertTrue(nefets.isAboard(outpost));
    }

}