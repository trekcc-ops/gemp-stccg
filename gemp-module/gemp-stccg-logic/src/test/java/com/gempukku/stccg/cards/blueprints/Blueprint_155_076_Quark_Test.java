package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_155_076_Quark_Test extends AbstractAtTest {

    private MissionCard _mission;
    private PersonnelCard quark;
    private PhysicalCard padd1;
    private PhysicalCard padd2;
    private PhysicalCard strom;

    private void initializeGame()
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_170", "Investigate Raid", P1);
        quark = builder.addCardOnPlanetSurface("155_076", "Quark", P1, _mission, PersonnelCard.class);
        padd1 = builder.addCardInHand("101_057", "Federation PADD", P1, EquipmentCard.class);
            // Can't download Strom even though he has "PADD" in his skill box
        strom = builder.addCardInHand("194_084", "Strom", P1, PersonnelCard.class);
        padd2 = builder.addDrawDeckCard("159_006", "Trilithium Weapon Control PADD", P1, EquipmentCard.class);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void downloadTest() throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame();
        useGameText(quark, P1);
        assertTrue(selectableCardsAre(P1, List.of(padd1, padd2)));
        selectCard(P1, padd1);
        assertEquals(padd1.getLocationId(), quark.getLocationId(), _mission.getLocationId());
    }

}