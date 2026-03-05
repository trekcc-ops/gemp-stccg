package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_127_023_RobertDeSoto_Test extends AbstractAtTest {

    private PersonnelCard multiAffilVip;
    private PhysicalCard spock;
    private PhysicalCard betor;
    private PhysicalCard outpost;
    private ShipCard hood;

    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        MissionCard mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        outpost = builder.addOutpost(Affiliation.ROMULAN, P1, mission);
        hood = builder.addShipInSpace("101_337", "U.S.S. Hood", P1, mission);
        builder.addCardAboardShipOrFacility("127_023", "Robert DeSoto", P1, hood, PersonnelCard.class);

        spock = builder.addCardInHand("106_018", "Spock", P1);
        multiAffilVip = builder.addCardInHand("991_008", "V.I.P. for DeSoto Test", P1,
                PersonnelCard.class);
        betor = builder.addCardInHand("101_252", "B'Etor", P1);

        builder.setPhase(Phase.CARD_PLAY);
        _game = builder.startGame();
    }

    @Test
    public void reportOnYourShipTest() throws Exception {
        initializeGame();
        // cannot play B'Etor
        assertThrows(DecisionResultInvalidException.class, () -> playCard(P1, betor));
        assertDoesNotThrow(() -> playCard(P1, spock));
        assertEquals(1, _game.getGameState().getNormalCardPlaysAvailable(P1));

        // play the multi-affiliation V.I.P. He can play to the outpost or ship. He will have to play as Fed when playing to the ship.
        playCard(P1, multiAffilVip);
        assertTrue(selectableCardsAre(P1, outpost, hood));
        selectCard(P1, hood);
        assertTrue(multiAffilVip.isInPlay());
        assertTrue(multiAffilVip.isAboard(hood));
        assertTrue(multiAffilVip.hasAffiliation(_game, Affiliation.FEDERATION, P1));
        assertFalse(multiAffilVip.hasAffiliation(_game, Affiliation.ROMULAN, P1));

        // Verify that normal card play was used
        assertEquals(0, _game.getGameState().getNormalCardPlaysAvailable(P1));
    }

}