package com.gempukku.stccg.rules;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.EquipmentCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.rules.st1e.ST1ERuleSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class ST1ERuleSetTest extends AbstractAtTest {
    @Test
    public void CompatibilityTest() throws Exception {
        initializeSimple1EGame(30);
        final ST1ERuleSet _rules = new ST1ERuleSet();
        final PersonnelCard picard = (PersonnelCard) newCardForGame("101_215", P1);
        final PersonnelCard sarek = (PersonnelCard) newCardForGame("101_233", P1);
        final PersonnelCard takket = (PersonnelCard) newCardForGame("101_320", P1);
        final EquipmentCard padd = (EquipmentCard) newCardForGame("101_057", P1);
        assertEquals("Jean-Luc Picard", picard.getTitle());
        assertFalse(_rules.areCardsCompatiblePerRules(takket, picard));
        assertFalse(_rules.areCardsCompatiblePerRules(takket, sarek));
        assertTrue(_rules.areCardsCompatiblePerRules(picard,sarek));
        assertTrue(_rules.areCardsCompatiblePerRules(picard,padd));
    }

}