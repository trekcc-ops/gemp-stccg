package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_103_042_LowerDecks_Test extends AbstractAtTest {

    private PersonnelCard syrus;
    private PhysicalCard lowerDecks1;
    private PhysicalCard lowerDecks2;
    private FacilityCard outpost;
    private PersonnelCard picard;
    private PersonnelCard larson;
    private PersonnelCard hologram;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.addMission("101_154", "Excavation", P1);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1);
        lowerDecks1 = builder.addCardInHand("103_042", "Lower Decks", P1);
        lowerDecks2 = builder.addCardInHand("103_042", "Lower Decks", P1);
        syrus = builder.addCardInHand("155_092", "Dr. Syrus", P1, PersonnelCard.class);
        picard = builder.addCardAboardShipOrFacility(
                "101_215", "Jean-Luc Picard", P1, outpost, PersonnelCard.class);
        larson = builder.addCardAboardShipOrFacility(
                "101_220", "Linda Larson", P1, outpost, PersonnelCard.class);
        hologram = builder.addCardAboardShipOrFacility(
                "991_005", "Dummy 1E Hologram Personnel", P1, outpost, PersonnelCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void attributeTest()
            throws InvalidGameOperationException, CardNotFoundException, DecisionResultInvalidException {
        initializeGame();
        playCard(P1, lowerDecks1);
        
        assertTrue(personnelHasAttributeIncrease(larson, 2));
        assertTrue(personnelHasAttributeIncrease(syrus, 0));
        assertTrue(personnelHasAttributeIncrease(picard, 0));
        assertTrue(personnelHasAttributeIncrease(hologram, 0));

        skipToNextTurnAndPhase(P1, Phase.CARD_PLAY);
        playCard(P1, lowerDecks2);

        assertTrue(personnelHasAttributeIncrease(larson, 2));
        assertTrue(personnelHasAttributeIncrease(syrus, 0));
        assertTrue(personnelHasAttributeIncrease(picard, 0));
        assertTrue(personnelHasAttributeIncrease(hologram, 0));
    }
    
    private boolean personnelHasAttributeIncrease(PersonnelCard personnel, int amount) {
        return (personnel.getIntegrity(_game) - personnel.getPrintedIntegrity() == amount) &&
                (personnel.getCunning(_game) - personnel.getPrintedCunning() == amount) &&
                (personnel.getStrength(_game) - personnel.getPrintedStrength() == amount);
    }



}