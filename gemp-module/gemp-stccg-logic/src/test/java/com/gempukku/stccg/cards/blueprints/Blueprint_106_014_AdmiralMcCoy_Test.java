package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.EquipmentCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_106_014_AdmiralMcCoy_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private PersonnelCard mccoy;
    private PersonnelCard ogawa;
    private PersonnelCard ogawa2;
    private PersonnelCard picard;
    private PersonnelCard worf;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        outpost = builder.addFacility("101_104", P1); // Federation Outpost
        mccoy = builder.addCardInHand("106_014", "Admiral McCoy", P1, PersonnelCard.class);
        ogawa = builder.addCardAboardShipOrFacility("101_198", "Alyssa Ogawa", P1, outpost, PersonnelCard.class);
        ogawa2 = builder.addCardAboardShipOrFacility("101_198", "Alyssa Ogawa", P2, outpost, PersonnelCard.class);
        worf = builder.addCardAboardShipOrFacility("101_251", "Worf", P1, outpost, PersonnelCard.class);
        picard = builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P1, outpost, PersonnelCard.class);
        builder.addCardAboardShipOrFacility("101_060", "Medical Kit", P1, outpost, EquipmentCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }
    
    @Test
    public void cunningIncreaseTest() throws Exception {
        initializeGame();
        assertTrue(picard.hasSkill(SkillName.MEDICAL, _game));
        assertEquals(6, ogawa.getCunning(_game));
        assertEquals(6, ogawa2.getCunning(_game));
        assertEquals(8, picard.getCunning(_game));
        assertEquals(6, mccoy.getCunning(_game));
        assertEquals(6, worf.getCunning(_game));

        // report McCoy to facility
        playCard(P1, mccoy);
        assertTrue(outpost.hasCardInCrew(mccoy));

        // Verify McCoy increases CUNNING of all MEDICAL aboard facility except himself
        assertEquals(9, ogawa.getCunning(_game));
        assertEquals(9, ogawa2.getCunning(_game));
        assertEquals(11, picard.getCunning(_game));

        // Verify CUNNING has not changed for McCoy or Worf
        assertEquals(6, mccoy.getCunning(_game));
        assertEquals(6, worf.getCunning(_game));
    }

}