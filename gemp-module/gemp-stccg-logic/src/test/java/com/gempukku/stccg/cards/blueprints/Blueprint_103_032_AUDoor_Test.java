package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_103_032_AUDoor_Test extends AbstractAtTest {

    private PhysicalCard doorway;
    private PersonnelCard syrus;
    private PhysicalCard doorwayInHand;
    private PhysicalCard doorway2;

    private void initializeGame(boolean includeDoorway) throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.addMission("101_154", "Excavation", P1);
        if (includeDoorway) {
            builder.addCardToCoreAsSeeded("103_032", "Alternate Universe Door", P1);
        }
        builder.addFacility("101_104", P1);// Federation Outpost
        syrus = builder.addCardInHand("155_092", "Dr. Syrus", P1, PersonnelCard.class);
        doorwayInHand = builder.addCardInHand("103_032", "Alternate Universe Door", P1);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void canOnlySeedOneTest() throws InvalidGameOperationException, CardNotFoundException,
            DecisionResultInvalidException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        doorway = builder.addSeedDeckCard("103_032", "Alternate Universe Door", P1);
        doorway2 = builder.addSeedDeckCard("103_032", "Alternate Universe Door", P1);
        builder.setPhase(Phase.SEED_DOORWAY);
        builder.startGame();
        assertThrows(DecisionResultInvalidException.class, () -> selectCards(P1, List.of(doorway, doorway2)));
        selectCard(P1, doorway);
        assertTrue(doorway.isInPlay());
        assertFalse(doorway2.isInPlay());
    }

    @Test
    public void cannotBePlayedTest() throws InvalidGameOperationException, CardNotFoundException {
        initializeGame(false);
        assertThrows(DecisionResultInvalidException.class, () -> playCard(P1, doorwayInHand));
    }

    @Test
    public void cannotPlayAUCardsTest() throws InvalidGameOperationException, CardNotFoundException {
        initializeGame(false);
        assertThrows(DecisionResultInvalidException.class, () -> playCard(P1, syrus));
    }

    @Test
    public void canPlayAUCardsTest() throws InvalidGameOperationException, CardNotFoundException,
            DecisionResultInvalidException {
        initializeGame(true);
        playCard(P1, syrus);
        assertTrue(syrus.isInPlay());
    }

}