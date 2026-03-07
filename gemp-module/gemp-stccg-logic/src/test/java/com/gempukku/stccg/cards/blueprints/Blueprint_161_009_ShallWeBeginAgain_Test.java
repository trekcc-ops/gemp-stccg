package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_161_009_ShallWeBeginAgain_Test extends AbstractAtTest {
    private MissionCard _mission;
    private PhysicalCard beginAgain;
    private List<PersonnelCard> attemptingPersonnel;

    private void initializeGame(int cardsInOpponentDeck, int commandInOpponentHand, int personnelPresent) throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players, List.of(30, cardsInOpponentDeck));
        _mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        beginAgain = builder.addSeedCardUnderMission("161_009", "Shall We Begin Again?", P1, _mission);

        attemptingPersonnel = new ArrayList<>();

        for (int i = 0; i < commandInOpponentHand; i++) {
            builder.addCardInHand("101_204", "Data", P2);
        }

        for (int i = 0; i < personnelPresent; i++) {
            attemptingPersonnel.add(builder.addCardOnPlanetSurface(
                    "101_236", "Simon Tarses", P1, _mission, PersonnelCard.class));
        }

        builder.setPhase(Phase.EXECUTE_ORDERS);
        _game = builder.startGame();
    }


    @Test
    public void stopSomeTest() throws Exception {

        initializeGame(20, 2, 6);
        int handSize = getHandSize(P2);

        attemptMission(P1, _mission);
        assertEquals(handSize + 3, getHandSize(P2));
        playerDecided(P1,"");

        int stoppedPersonnel = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedPersonnel++;
            }
        }

        assertEquals(2, stoppedPersonnel);
        assertEquals(Zone.REMOVED, beginAgain.getZone());
    }

    @Test
    public void stopAllTest() throws Exception {

        initializeGame(20, 6, 2);
        int handSize = getHandSize(P2);

        attemptMission(P1, _mission);
        assertEquals(handSize + 3, getHandSize(P2));
        playerDecided(P1,"");

        int stoppedPersonnel = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedPersonnel++;
            }
        }

        assertEquals(2, stoppedPersonnel);
        assertEquals(Zone.REMOVED, beginAgain.getZone());
    }

    @Test
    public void notEnoughCardsToDrawTest() throws Exception {

        initializeGame(8, 6, 2);
        int handSize = getHandSize(P2);
        int drawDeckSize = _game.getGameState().getCardGroup(P2, Zone.DRAW_DECK).size();
        assertEquals(1, drawDeckSize);

        attemptMission(P1, _mission);
        assertEquals(handSize + 1, getHandSize(P2));
        playerDecided(P1,"");

        int stoppedPersonnel = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedPersonnel++;
            }
        }

        assertEquals(2, stoppedPersonnel);
        assertEquals(Zone.REMOVED, beginAgain.getZone());
    }


}