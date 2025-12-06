package com.gempukku.stccg.rules;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.EquipmentCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.rules.st1e.ST1ERuleSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class ST1ERuleSetTest extends AbstractAtTest {
    @Test
    public void CompatibilityTest() throws CardNotFoundException, PlayerNotFoundException {
        initializeSimple1EGame(30);
        final ST1ERuleSet _rules = new ST1ERuleSet();
        Player player = _game.getCurrentPlayer();
        final PersonnelCard picard = new PersonnelCard(_game, 101, player, _cardLibrary.getCardBlueprint("101_215"));
        final PersonnelCard sarek = new PersonnelCard(_game, 102, player, _cardLibrary.getCardBlueprint("101_233"));
        final PersonnelCard takket = new PersonnelCard(_game, 103, player, _cardLibrary.getCardBlueprint("101_320"));
        final EquipmentCard padd = new EquipmentCard(_game, 104, player, _cardLibrary.getCardBlueprint("101_057"));
        assertEquals("Jean-Luc Picard", picard.getTitle());
        assertFalse(_rules.areCardsCompatiblePerRules(takket, picard));
        assertFalse(_rules.areCardsCompatiblePerRules(takket, sarek));
        assertTrue(_rules.areCardsCompatiblePerRules(picard,sarek));
        assertTrue(_rules.areCardsCompatiblePerRules(picard,padd));
    }

}