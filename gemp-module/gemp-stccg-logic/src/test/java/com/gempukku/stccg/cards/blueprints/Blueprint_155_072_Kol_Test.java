package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_155_072_Kol_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private PersonnelCard kol;
    private PersonnelCard arridor;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        outpost = builder.addFacility("101_105", P1); // Klingon Outpost
        arridor = builder.addCardInHand("155_069", "Dr. Arridor", P1, PersonnelCard.class);
        kol = builder.addCardInHand("155_072", "Kol", P1, PersonnelCard.class);
    }

    @Test
    public void attributeTest() throws Exception {
        initializeGame();

        assertTrue(outpost.isInPlay());

        reportCardToFacility(kol, outpost);
        assertTrue(personnelAttributesAre(kol, List.of(6, 6, 5)));
        assertTrue(personnelAttributesAre(arridor, List.of(4, 8, 5)));

        reportCardToFacility(arridor, outpost);
        assertTrue(_game.getGameState().cardsArePresentWithEachOther(arridor, kol));

        assertTrue(personnelAttributesAre(kol, List.of(8, 8, 7)));
        assertTrue(personnelAttributesAre(arridor, List.of(4, 8, 5)));
    }
}