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

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_155_054_Beverly_Test extends AbstractAtTest {

    private PhysicalCard phaser;
    private FacilityCard outpost;
    private PersonnelCard beverly;
    private MissionCard mission;

    private void initializeGame()
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission("101_154", "Excavation", P1);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1);
        phaser = builder.addDrawDeckCard("101_064", "Starfleet Type II Phaser", P1);
        beverly = builder.addCardInHand("155_054", "Beverly", P1, PersonnelCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void downloadTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {
        initializeGame();
        playCard(P1, beverly);
        downloadCard(P1, phaser);

        // Verify that phaser can be downloaded to planet mission or outpost
        assertTrue(getSelectableCards(P1).containsAll(List.of(mission, outpost)));

        selectCard(P1, outpost);
        assertTrue(phaser.isInPlay());
        assertTrue(phaser.isAboard(outpost));
    }

}