package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Blueprint_163_038_KVit_Test extends AbstractAtTest {

    private PersonnelCard kvit1;
    private PersonnelCard kvit2;
    private FacilityCard outpost;
    private ShipCard chajoh1;
    private ShipCard chajoh2;

    private void initializeGame(boolean playerOne, boolean playerTwo, boolean playerTwoOnTheirOwnShip)
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        outpost = builder.addOutpost(Affiliation.KLINGON, P1);
        chajoh1 = builder.addDockedShip("163_052", "Cha'Joh", P1, outpost);
        chajoh2 = builder.addDockedShip("163_052", "Cha'Joh", P2, outpost);
        if (playerOne) {
            kvit1 = builder.addCardAboardShipOrFacility("163_038", "K'Vit", P1, chajoh1, PersonnelCard.class);
        }
        if (playerTwo) {
            ShipCard shipToJoin = (playerTwoOnTheirOwnShip) ? chajoh2 : chajoh1;
            kvit2 = builder.addCardAboardShipOrFacility("163_038", "K'Vit", P2, shipToJoin,
                    PersonnelCard.class);
        }
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void yourPersonnelTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame(true, false, false);
        assertEquals(2, chajoh1.getRange(_game) - chajoh1.getPrintedRange());
        assertEquals(0, chajoh2.getRange(_game) - chajoh2.getPrintedRange());
    }

    @Test
    public void opponentsPersonnelTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame(false, true, false);
        // Verify P1 Cha'Joh still gets the RANGE boost even though K'Vit belongs to opponent
        assertEquals(2, chajoh1.getRange(_game) - chajoh1.getPrintedRange());
        assertEquals(0, chajoh2.getRange(_game) - chajoh2.getPrintedRange());
    }

    @Test
    public void bothPersonnelOnYourShipTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame(true, true, false);
        // Verify P1 Cha'Joh still gets the RANGE boost even though K'Vit belongs to opponent
        assertEquals(2, chajoh1.getRange(_game) - chajoh1.getPrintedRange());
        assertEquals(0, chajoh2.getRange(_game) - chajoh2.getPrintedRange());
    }

    @Test
    public void personnelOnEachShipTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame(true, true, true);
        // Verify that both ships get the boost if they each have their own K'Vit
        assertEquals(2, chajoh1.getRange(_game) - chajoh1.getPrintedRange());
        assertEquals(2, chajoh2.getRange(_game) - chajoh2.getPrintedRange());
    }



}