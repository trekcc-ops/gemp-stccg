package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.EquipmentCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_101_057_FedPADD_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private EquipmentCard padd;
    private PersonnelCard picard;
    private PersonnelCard opposingPicard;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        outpost = builder.addFacility("101_104", P1); // Federation Outpost
        padd = builder.addCardInHand("101_057", "Federation PADD", P1, EquipmentCard.class);
        picard = builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P1, outpost, PersonnelCard.class);
        opposingPicard = builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P2, outpost, PersonnelCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void cunningTest() throws Exception {
        initializeGame();

        assertEquals(8, picard.getCunning(_game));
        reportCard(P1, padd, outpost);
        assertTrue(_game.getGameState().cardsArePresentWithEachOther(picard, padd));
        assertEquals(10, picard.getCunning(_game));
        assertEquals(8, opposingPicard.getCunning(_game));
    }
}