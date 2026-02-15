package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_155_079_CaptainWorf_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private PersonnelCard worf;
    private PersonnelCard kehleyr1;
    private PersonnelCard kehleyr2;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        outpost = builder.addOutpost(Affiliation.KLINGON, P1);
        worf = builder.addCardInHand("155_079", "Captain Worf", P1, PersonnelCard.class);
        kehleyr1 = builder.addCardAboardShipOrFacility("155_080", "Commander K'Ehleyr", P1, outpost, PersonnelCard.class);
        kehleyr2 = builder.addCardAboardShipOrFacility("101_217", "K'Ehleyr", P2, outpost, PersonnelCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }


    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void captainWorfTest() throws Exception {
        initializeGame();

        assertTrue(personnelAttributesAre(worf, List.of(8, 6, 10)));
        assertTrue(personnelAttributesAre(kehleyr1, List.of(7, 8, 7)));
        assertTrue(personnelAttributesAre(kehleyr2, List.of(8, 7, 7)));
        playCard(P1, worf);

        assertTrue(_game.getGameState().getAllCardsInPlay().contains(kehleyr1));
        assertTrue(_game.getGameState().getAllCardsInPlay().contains(kehleyr2));
        assertTrue(_game.getGameState().cardsArePresentWithEachOther(kehleyr1, worf));

        assertTrue(personnelAttributesAre(worf, List.of(10, 8, 12)));
        assertTrue(personnelAttributesAre(kehleyr1, List.of(9, 10, 9)));
        assertTrue(personnelAttributesAre(kehleyr2, List.of(10, 9, 9)));
    }

}