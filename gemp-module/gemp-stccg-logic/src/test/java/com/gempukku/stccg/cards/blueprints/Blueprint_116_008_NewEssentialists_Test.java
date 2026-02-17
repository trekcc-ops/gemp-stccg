package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_116_008_NewEssentialists_Test extends AbstractAtTest {

    private PersonnelCard picard;
    private MissionCard _mission;
    private PhysicalCard newEssentialists;
    private PersonnelCard troi;
    private PersonnelCard hobson;
    private PersonnelCard data;
    private PersonnelCard sharat;
    private List<PersonnelCard> attemptingPersonnel;

    private void initializeGame(String... extraCards) throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_154", "Excavation", P1);
        newEssentialists = builder.addSeedCardUnderMission("116_008", "New Essentialists", P2, _mission);
        data = builder.addCardOnPlanetSurface("101_204", "Data", P1, _mission, PersonnelCard.class);
        troi = builder.addCardOnPlanetSurface("101_205", "Deanna Troi", P1, _mission, PersonnelCard.class);
        hobson = builder.addCardOnPlanetSurface("101_202", "Christopher Hobson", P1, _mission, PersonnelCard.class);
        picard = builder.addCardOnPlanetSurface("101_215", "Jean-Luc Picard", P1, _mission, PersonnelCard.class);
        sharat = builder.addCardOnPlanetSurface("112_235", "Sharat", P1, _mission, PersonnelCard.class);
        attemptingPersonnel = new ArrayList<>(List.of(data, troi, hobson, picard, sharat));

        if (List.of(extraCards).contains("Tam Elbrun")) {
            PersonnelCard elbrun = builder.addCardOnPlanetSurface("101_243", "Tam Elbrun", P1, _mission, PersonnelCard.class);
            attemptingPersonnel.add(elbrun);
        }
        if (List.of(extraCards).contains("Lopez")) {
            PersonnelCard lopez = builder.addCardOnPlanetSurface("155_063", "Lopez", P1, _mission, PersonnelCard.class);
            attemptingPersonnel.add(lopez);
        }

        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void failDilemmaTest() throws Exception {
        // INTEGRITY>40 + Honor x2 OR CUNNING>40 + Treachery x2

        // Picard = Honor + INTEGRITY 9 + CUNNING 8
        // Data = INTEGRITY 8 + CUNNING 12
        // Christopher Hobson = INTEGRITY 6 + CUNNING 7
        // Deanna Troi = INTEGRITY 8 + CUNNING 7
        // Sharat = Treachery + INTEGRITY 4 + CUNNING 5

        // Total Honor + Treachery + INTEGRITY 35 + CUNNING 39

        initializeGame();
        attemptMission(P1, _mission);

        int stoppedPersonnel = 0;
        int personnelInDrawDeck = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedPersonnel++;
            } else if (personnel.getZone() == Zone.DRAW_DECK) {
                personnelInDrawDeck++;
            }
        }

        assertEquals(4, stoppedPersonnel);
        assertEquals(1, personnelInDrawDeck);
        assertNotEquals(Zone.REMOVED, newEssentialists.getZone());

    }

    @Test
    public void failDilemmaWithHonorTest() throws Exception {
        // INTEGRITY>40 + Honor x2 OR CUNNING>40 + Treachery x2

        // Picard = Honor + INTEGRITY 9 + CUNNING 8
        // Data = INTEGRITY 8 + CUNNING 12
        // Christopher Hobson = INTEGRITY 6 + CUNNING 7
        // Deanna Troi = INTEGRITY 8 + CUNNING 7
        // Sharat = Treachery + INTEGRITY 4 + CUNNING 5
        // Tam Elbrun = Honor + INTEGRITY 5 + CUNNING 7

        // Total Honor x2 + Treachery + INTEGRITY 40 + CUNNING 46

        initializeGame("Tam Elbrun");
        assertEquals(40, getAwayTeamTotalAttribute(CardAttribute.INTEGRITY));
        attemptMission(P1, _mission);

        int stoppedPersonnel = 0;
        int personnelInDrawDeck = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedPersonnel++;
            } else if (personnel.getZone() == Zone.DRAW_DECK) {
                personnelInDrawDeck++;
            }
        }

        assertEquals(5, stoppedPersonnel);
        assertEquals(1, personnelInDrawDeck);
        assertNotEquals(Zone.REMOVED, newEssentialists.getZone());

    }

    @Test
    public void passDilemmaWithHonorTest() throws Exception {
        initializeGame("Lopez");
        assertEquals(42, getAwayTeamTotalAttribute(CardAttribute.INTEGRITY));
        attemptMission(P1, _mission);

        int stoppedPersonnel = 0;
        int personnelInDrawDeck = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedPersonnel++;
            } else if (personnel.getZone() == Zone.DRAW_DECK) {
                personnelInDrawDeck++;
            }
        }

        assertEquals(0, stoppedPersonnel);
        assertEquals(0, personnelInDrawDeck);
        assertEquals(Zone.REMOVED, newEssentialists.getZone());

    }

    @Test
    public void allBorgTest() throws Exception {
        // if Away Team only has Borg, they won't go back to the draw deck
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_170", "Investigate Raid", P1);
        PersonnelCard crosis = builder.addCardOnPlanetSurface("163_044", "Lieutenant Crosis", P1, _mission, PersonnelCard.class);
        newEssentialists = builder.addSeedCardUnderMission("116_008", "New Essentialists", P2, _mission);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
        attemptMission(P1, _mission);
        assertTrue(crosis.isStopped());
        assertNotEquals(Zone.DRAW_DECK, crosis.getZone());
        assertNotEquals(Zone.REMOVED, newEssentialists.getZone());
    }

    private int getAwayTeamTotalAttribute(CardAttribute attribute) {
        int total = 0;
        for (PersonnelCard personnel : attemptingPersonnel) {
            total = total + personnel.getAttribute(attribute, _game);
        }
        return total;
    }


}