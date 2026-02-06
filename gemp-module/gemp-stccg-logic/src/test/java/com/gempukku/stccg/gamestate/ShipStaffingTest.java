package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpellCheckingInspection")
public class ShipStaffingTest extends AbstractAtTest {

    ShipCard runabout;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        FacilityCard outpost = builder.addFacility("101_104", P1); // Federation outpost
        runabout = builder.addDockedShip("101_331", "Runabout", P1, outpost);
        builder.addCardAboardShipOrFacility("101_204", "Data", P1, runabout, PersonnelCard.class);
        _game = builder.getGame();
        builder.startGame();
    }
    
    @Test
    public void shipStaffingTest() throws CardNotFoundException, InvalidGameOperationException {
        initializeGame();
        assertTrue(runabout.isStaffed(_game));
    }

}