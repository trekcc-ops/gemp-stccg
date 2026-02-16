package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_159_031_Tomalak_Test extends AbstractAtTest {

    private PersonnelCard tomalak;
    private PhysicalCard lowerDecks;
    private PhysicalCard lowerDecks2;

    private void initializeGame()
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        MissionCard _mission = builder.addMission("101_170", "Investigate Raid", P1);
        tomalak = builder.addCardOnPlanetSurface("159_031", "Tomalak", P1, _mission, PersonnelCard.class);
        lowerDecks = builder.addCardInHand("103_042", "Lower Decks", P1);
        lowerDecks2 = builder.addCardInHand("103_042", "Lower Decks", P1);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void downloadTest() throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame();
        useGameText(tomalak, P1);
        assertTrue(selectableCardsAre(List.of(lowerDecks, lowerDecks2), P1));
        selectCard(P1, lowerDecks);
        assertTrue(lowerDecks.isInPlay());
        assertEquals(Zone.CORE, lowerDecks.getZone());
    }

}