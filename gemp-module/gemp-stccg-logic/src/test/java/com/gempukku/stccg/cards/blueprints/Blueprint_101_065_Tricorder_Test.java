package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.EquipmentCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_065_Tricorder_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private EquipmentCard tricorder;
    private PersonnelCard geordi;
    private PersonnelCard tamal;
    private PersonnelCard deanna;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1); // Federation Outpost
        tricorder = builder.addCardInHand("101_065", "Tricorder", P1, EquipmentCard.class);
        geordi = builder.addCardAboardShipOrFacility("101_212", "Geordi La Forge", P1, outpost, PersonnelCard.class);
        tamal = builder.addCardAboardShipOrFacility("172_031", "Tamal", P1, outpost, PersonnelCard.class);
        deanna = builder.addCardAboardShipOrFacility("101_205", "Deanna Troi", P1, outpost, PersonnelCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void skillTest() throws Exception {
        initializeGame();

        assertFalse(geordi.hasSkill(SkillName.SCIENCE, _game));
        assertFalse(deanna.hasSkill(SkillName.SCIENCE, _game));
        assertEquals(0, geordi.getSkillLevel(_game, SkillName.SCIENCE));
        assertEquals(1, tamal.getSkillLevel(_game, SkillName.SCIENCE));
        playCard(P1, tricorder);

        assertTrue(_game.getGameState().cardsArePresentWithEachOther(tricorder, geordi, tamal, deanna));

        assertTrue(geordi.hasSkill(SkillName.SCIENCE, _game));
        assertFalse(deanna.hasSkill(SkillName.SCIENCE, _game));
        assertEquals(1, geordi.getSkillLevel(_game, SkillName.SCIENCE));
        assertEquals(2, tamal.getSkillLevel(_game, SkillName.SCIENCE));
    }

}