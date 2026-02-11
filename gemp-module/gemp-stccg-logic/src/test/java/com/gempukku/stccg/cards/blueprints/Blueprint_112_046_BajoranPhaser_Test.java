package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.EquipmentCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Blueprint_112_046_BajoranPhaser_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private EquipmentCard phaser;
    private PersonnelCard picard;
    private MissionCard mission;
    private PersonnelCard kallis;
    private PersonnelCard opposingKallis;
    private PersonnelCard sito;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission("161_021", "Advanced Combat Training", P1);
        outpost = builder.addFacility("112_078", P1, mission); // Bajoran Outpost
        phaser = builder.addCardInHand("112_046", "Bajoran Phaser", P1, EquipmentCard.class);
        kallis = builder.addCardAboardShipOrFacility("112_152", "Kallis Ven", P1, outpost, PersonnelCard.class);
        sito = builder.addCardAboardShipOrFacility("101_239", "Sito Jaxa", P1, outpost, PersonnelCard.class);
        opposingKallis = builder.addCardAboardShipOrFacility("112_152", "Kallis Ven", P2, outpost, PersonnelCard.class);
        picard = builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P1, outpost, PersonnelCard.class);
        builder.addCardAboardShipOrFacility("101_270", "Klag", P1, outpost, PersonnelCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }
    
    @Test
    public void strengthIncreaseTest() throws Exception {
        initializeGame();

        assertEquals(0, picard.getStrength(_game) - picard.getPrintedStrength());
        assertEquals(0, sito.getStrength(_game) - sito.getPrintedStrength());
        assertEquals(0, opposingKallis.getStrength(_game) - opposingKallis.getPrintedStrength());
        assertEquals(0, kallis.getStrength(_game) - kallis.getPrintedStrength());

        playCard(P1, phaser);

        // Verify that all P1 personnel on facility got STRENGTH bonus; no bonus for P2 cards
        assertEquals(2, kallis.getStrength(_game) - kallis.getPrintedStrength());
        assertEquals(2, picard.getStrength(_game) - picard.getPrintedStrength());
        assertEquals(2, sito.getStrength(_game) - sito.getPrintedStrength());
        assertEquals(0, opposingKallis.getStrength(_game) - opposingKallis.getPrintedStrength());

        beamCard(P1, outpost, kallis, mission);

        // Verify that STRENGTH bonus is still in effect thanks to Sito's species
        assertEquals(2, picard.getStrength(_game) - picard.getPrintedStrength());
        assertEquals(2, sito.getStrength(_game) - sito.getPrintedStrength());

        // Verify that if Sito beams down, the STRENGTH bonus goes away
        beamCard(P1, outpost, sito, mission);
        assertEquals(0, picard.getStrength(_game) - picard.getPrintedStrength());
        assertEquals(0, sito.getStrength(_game) - sito.getPrintedStrength());
    }
}