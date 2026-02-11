package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_112_167_ShakaarEdon_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private PersonnelCard shakaar;
    private MissionCard mission;
    private PersonnelCard sitoOnPlanet;
    private PersonnelCard picardOnPlanet;
    private PersonnelCard opposingKallisOnPlanet;
    private PersonnelCard kallisOnFacility;
    private PersonnelCard shakaar2;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission("161_021", "Advanced Combat Training", P1);
        outpost = builder.addFacility("112_078", P1, mission); // Bajoran Outpost
        shakaar = builder.addCardInHand("112_167", "Shakaar Edon", P1, PersonnelCard.class);
        shakaar2 = builder.addCardInHand("112_167", "Shakaar Edon", P1, PersonnelCard.class);
        sitoOnPlanet = builder.addCardOnPlanetSurface("101_239", "Sito Jaxa", P1, mission, PersonnelCard.class);
        opposingKallisOnPlanet = builder.addCardOnPlanetSurface("112_152", "Kallis Ven", P2, mission, PersonnelCard.class);
        picardOnPlanet = builder.addCardOnPlanetSurface("101_215", "Jean-Luc Picard", P1, mission, PersonnelCard.class);
        kallisOnFacility = builder.addCardAboardShipOrFacility("112_152", "Kallis Ven", P1, outpost, PersonnelCard.class);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }
    
    @Test
    public void strengthIncreaseTest() throws Exception {
        initializeGame();
        assertEquals(7, kallisOnFacility.getStrength(_game));

        // report Shakaar to facility
        playCard(P1, shakaar);
        assertTrue(outpost.hasCardInCrew(shakaar));

        // Verify Shakaar has no impact on cards on planet or himself, but does increase STRENGTH for Bajoran on facility
        assertEquals(6, sitoOnPlanet.getStrength(_game));
        assertEquals(7, opposingKallisOnPlanet.getStrength(_game));
        assertEquals(6, picardOnPlanet.getStrength(_game));
        assertEquals(8, shakaar.getStrength(_game));
        assertEquals(9, kallisOnFacility.getStrength(_game));

        // beam Shakaar to planet; verify that other Bajorans on planet are now STRENGTH +2, and Bajoran on facility has returned to normal
        beamCard(P1, outpost, shakaar, mission);
        assertEquals(8, sitoOnPlanet.getStrength(_game));
        assertEquals(9, opposingKallisOnPlanet.getStrength(_game));
        assertEquals(8, shakaar.getStrength(_game));
        assertEquals(6, picardOnPlanet.getStrength(_game)); // Picard not modified because he is not Bajoran
        assertEquals(7, kallisOnFacility.getStrength(_game));
    }

    @Test
    public void uniquenessTest() throws Exception {
        initializeGame();
        assertEquals(1, _game.getGameState().getCurrentTurnNumber());
        playCard(P1, shakaar);
        assertEquals(1, _game.getGameState().getCurrentTurnNumber());
        skipToNextTurnAndPhase(P1, Phase.CARD_PLAY);
        assertEquals(3, _game.getGameState().getCurrentTurnNumber());
        assertTrue(shakaar2.isInHand(_game));
        assertThrows(DecisionResultInvalidException.class, () -> playCard(P1, shakaar2));
    }

}