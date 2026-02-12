package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_163_039_Lursa_Test extends AbstractAtTest {

    private PersonnelCard betor;
    private PersonnelCard lursa;
    private FacilityCard outpost;
    private PersonnelCard koral;

    private void initializeGame()
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.addMission("101_170", "Investigate Raid", P1);
        outpost = builder.addFacility("101_105", P1); // Klingon Outpost
        lursa = builder.addCardInHand("163_039", "Lursa", P1, PersonnelCard.class);
        betor = builder.addDrawDeckCard("163_036", "B'Etor", P1, PersonnelCard.class);
        koral = builder.addCardAboardShipOrFacility("155_093", "Koral", P1, outpost,
                PersonnelCard.class, Affiliation.NON_ALIGNED);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void downloadAndAttributeTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame();
        assertEquals(Affiliation.NON_ALIGNED, Iterables.getOnlyElement(koral.getCurrentAffiliations()));
        assertTrue(personnelAttributesAre(lursa, List.of(2,8,7)));
        assertTrue(personnelAttributesAre(betor, List.of(2,7,7)));
        assertTrue(personnelAttributesAre(koral, List.of(3,4,10)));
        playCard(P1, lursa);

        assertTrue(personnelAttributesAre(lursa, List.of(3,9,8)));
        assertTrue(personnelAttributesAre(koral, List.of(4,5,11)));

        // Download B'Etor to outpost
        useGameText(lursa, P1);
        selectCard(P1, outpost);
        assertTrue(personnelAttributesAre(betor, List.of(3,8,8)));
    }

}