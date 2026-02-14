package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_155_063_Lopez_Test extends AbstractAtTest {

    private PhysicalCard padd1;
    private PhysicalCard padd2;
    private FacilityCard outpost;
    private PersonnelCard lopez1;
    private PersonnelCard lopez2;
    private MissionCard mission;

    private void initializeGame()
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission("101_154", "Excavation", P1);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1);
        padd1 = builder.addDrawDeckCard("101_057", "Federation PADD", P1);
        padd2 = builder.addDrawDeckCard("101_057", "Federation PADD", P1);
        lopez1 = builder.addCardInHand("155_063", "Lopez", P1, PersonnelCard.class);
        lopez2 = builder.addCardInHand("155_063", "Lopez", P1, PersonnelCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void downloadTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame();
        playCard(P1, lopez1);

        // Verify that you can download either phaser
        initiateDownloadAction(P1, lopez1);
        assertEquals(2, getSelectableCards(P1).size());
        assertTrue(getSelectableCards(P1).containsAll(List.of(padd1, padd2)));
        selectCard(P1, padd1);

        // Verify that PADD can be downloaded to mission or outpost
        assertTrue(getSelectableCards(P1).containsAll(List.of(mission, outpost)));
        selectCard(P1, outpost);

        assertTrue(padd1.isInPlay());
        assertTrue(padd1.isAttachedTo(outpost));

        // Skip to next turn. Should be able to play another copy of Lopez, but his special download can't be reused.
        skipToNextTurnAndPhase(P1, Phase.CARD_PLAY);
        playCard(P1, lopez2);
        assertThrows(DecisionResultInvalidException.class, () -> initiateDownloadAction(P1, lopez2));
    }

}