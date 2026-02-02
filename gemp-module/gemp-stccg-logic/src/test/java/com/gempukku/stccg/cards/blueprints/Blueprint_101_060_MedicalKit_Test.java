package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.EquipmentCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_060_MedicalKit_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private EquipmentCard medicalKit;
    private PersonnelCard picard;
    private PersonnelCard taris;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        outpost = builder.addFacility("101_104", P1); // Federation Outpost
        medicalKit = builder.addCardInHand("101_060", "Medical Kit", P1, EquipmentCard.class);
        picard = builder.addCardInHand("101_215", "Jean-Luc Picard", P1, PersonnelCard.class);
        taris = builder.addCardInHand("105_085", "Taris", P1, PersonnelCard.class);
    }

    @Test
    public void skillTest() throws Exception {
        initializeGame();

        reportCardsToFacility(List.of(picard, taris), outpost);
        assertEquals(0, picard.getSkillLevel(_game, SkillName.MEDICAL));
        assertEquals(1, taris.getSkillLevel(_game, SkillName.MEDICAL));

        reportCardToFacility(medicalKit, outpost);
        assertTrue(_game.getGameState().cardsArePresentWithEachOther(picard, medicalKit, taris));
        assertEquals(1, picard.getSkillLevel(_game, SkillName.MEDICAL));
        assertEquals(2, taris.getSkillLevel(_game, SkillName.MEDICAL));
    }
}