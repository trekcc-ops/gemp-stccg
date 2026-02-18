package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.EquipmentCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_103_100_TashaYarAlternate_Test extends AbstractAtTest {

    private EquipmentCard phaser;
    private FacilityCard outpost;
    private PersonnelCard tasha;
    private MissionCard mission;

    private void initializeGame(boolean allowAltUnivCards)
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission("101_154", "Excavation", P1);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1);
        phaser = builder.addDrawDeckCard("101_064", "Starfleet Type II Phaser", P1, EquipmentCard.class);
        tasha = builder.addCardInHand("103_100", "Tasha Yar - Alternate", P1, PersonnelCard.class);

        if (allowAltUnivCards) {
            builder.addCardToCoreAsSeeded("103_032", "Alternate Universe Door", P1);
        }

        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void AUTest() throws CardNotFoundException, InvalidGameOperationException {
        initializeGame(false);
        assertThrows(DecisionResultInvalidException.class, () -> playCard(P1, tasha));
    }

    @Test
    public void downloadTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame(true);
        playCard(P1, tasha);
        downloadCard(P1, phaser);

        // Verify that phaser can be downloaded to planet mission or outpost
        assertTrue(getSelectableCards(P1).containsAll(List.of(mission, outpost)));

        selectCard(P1, outpost);
        assertTrue(phaser.isInPlay());
        assertTrue(phaser.isAboard(outpost));
    }

}