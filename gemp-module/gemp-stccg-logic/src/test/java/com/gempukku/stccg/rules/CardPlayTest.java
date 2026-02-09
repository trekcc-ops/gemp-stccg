package com.gempukku.stccg.rules;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.ST1EGame;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CardPlayTest extends AbstractAtTest {

    private PhysicalCard continuing;
    private PhysicalCard attention;
    private PhysicalCard attention2;
    private PersonnelCard worf;
    private PersonnelCard millin;
    private PhysicalCard runabout;
    private FacilityCard outpost;
    private PersonnelCard picard;
    private PersonnelCard lopez;
    private PhysicalCard altUnivEvent;
    private PhysicalCard uniqueEvent;

    private ST1EGame initializeGame(boolean includeDownloadableCards,
                                    boolean allowAUCards) throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        continuing = builder.addSeedDeckCard("155_022", "Continuing Mission", P1);
        outpost = builder.addFacility("101_104", P1); // Federation Outpost
        builder.addCardToCoreAsSeeded("991_004", "Dummy 1E Unique Warp Core Event", P1);
        if (includeDownloadableCards) {
            attention = builder.addDrawDeckCard("155_021", "Attention All Hands", P1);
            attention2 = builder.addDrawDeckCard("155_021", "Attention All Hands", P1);
            altUnivEvent = builder.addDrawDeckCard("991_003", "Dummy 1E AU Warp Core Event", P1);
            uniqueEvent = builder.addDrawDeckCard("991_004", "Dummy 1E Unique Warp Core Event", P1);
        }
        worf = builder.addCardInHand("101_251", "Worf", P1, PersonnelCard.class);
        picard = builder.addCardInHand("101_215", "Jean-Luc Picard", P1, PersonnelCard.class);
        millin = builder.addCardInHand("163_045", "Millin", P1, PersonnelCard.class);
        runabout = builder.addCardInHand("101_331", "Runabout", P1);
        lopez = builder.addCardInHand("155_063", "Lopez", P1, PersonnelCard.class);

        if (allowAUCards) {
            builder.addCardToCoreAsSeeded("103_032", "Alternate Universe Door", P1);
        }

        builder.setPhase(Phase.SEED_FACILITY);
        builder.startGame();
        return builder.getGame();
    }

    @Test
    public void downloadSucceedTest() throws Exception {
        _game = initializeGame(true, true);
        seedCard(P1, continuing);
        useGameText(continuing, P1);

        // Verify that both copies of Attention All Hands are in selectable cards, but can't select both
        assertTrue(getSelectableCards(P1).containsAll(List.of(attention, attention2, altUnivEvent)));

        // Select card to download
        selectCard(P1, altUnivEvent);
        assertTrue(altUnivEvent.isInPlay());
    }

    @Test
    public void downloadFailTest() throws Exception {
        _game = initializeGame(true, false);
        seedCard(P1, continuing);
        useGameText(continuing, P1);

        assertTrue(getSelectableCards(P1).containsAll(List.of(attention, attention2)));
        assertFalse(getSelectableCards(P1).contains(altUnivEvent)); // can't download because of AU
        assertFalse(getSelectableCards(P1).contains(uniqueEvent)); // can't download because of uniqueness
    }
}