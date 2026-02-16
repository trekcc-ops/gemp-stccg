package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_101_054_WindDancer_Test extends AbstractAtTest {

    private enum DilemmaRequirement {
        LWAXANA, MUSIC, YOUTH, STRENGTH, NONE
    };
    
    private FacilityCard outpost;
    private MissionCard _mission;
    private PhysicalCard _windDancer;
    private PersonnelCard troi;
    private Collection<PersonnelCard> attemptingPersonnel = new ArrayList<>();

    private void initializeGame(DilemmaRequirement requirement) throws InvalidGameOperationException, CardNotFoundException {
        attemptingPersonnel.clear();
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("155_038", "Encounter at Farpoint", P1);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1);
        _windDancer = builder.addSeedCardUnderMission("101_054",  "Wind Dancer", P2, _mission);
        troi = builder.addCardOnPlanetSurface("155_058", "Deanna Troi", P1, _mission, PersonnelCard.class);
        attemptingPersonnel.add(troi);

        PersonnelCard secondPersonnel = (PersonnelCard) switch(requirement) {
            case LWAXANA -> builder.addCardOnPlanetSurface("101_221", "Lwaxana Troi", P1, _mission);
            case MUSIC -> builder.addCardOnPlanetSurface("101_215", "Jean-Luc Picard", P1, _mission);
            case YOUTH -> builder.addCardOnPlanetSurface("101_196", "Alexander Rozhenko", P1, _mission);
            case STRENGTH -> builder.addCardOnPlanetSurface("101_251", "Worf", P1, _mission);
            case NONE -> builder.addCardOnPlanetSurface("163_044", "Lieutenant Crosis", P1, _mission);
        };
        attemptingPersonnel.add(secondPersonnel);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void passWithLwaxanaTest() throws Exception {
        initializeGame(DilemmaRequirement.LWAXANA);
        attemptMission(P1, _mission);
        assertFalse(troi.isStopped());
        assertEquals(Zone.REMOVED, _windDancer.getZone());
    }

    @Test
    public void passWithYouthTest() throws Exception {
        initializeGame(DilemmaRequirement.YOUTH);
        attemptMission(P1, _mission);
        assertFalse(troi.isStopped());
        assertEquals(Zone.REMOVED, _windDancer.getZone());
    }

    @Test
    public void passWithMusicTest() throws Exception {
        initializeGame(DilemmaRequirement.MUSIC);
        attemptMission(P1, _mission);
        assertFalse(troi.isStopped());
        assertEquals(Zone.REMOVED, _windDancer.getZone());
    }

    @Test
    public void passWithStrengthTest() throws Exception {
        initializeGame(DilemmaRequirement.STRENGTH);
        attemptMission(P1, _mission);
        assertFalse(troi.isStopped());
        assertEquals(Zone.REMOVED, _windDancer.getZone());
    }

    @Test
    public void failDilemmaTest() throws Exception {
        // Adding Crosis gives total STRENGTH>9, but from multiple personnel, so the dilemma is failed
        initializeGame(DilemmaRequirement.NONE);
        attemptMission(P1, _mission);
        assertTrue(troi.isStopped());
        assertNotEquals(Zone.REMOVED, _windDancer.getZone());
    }


}