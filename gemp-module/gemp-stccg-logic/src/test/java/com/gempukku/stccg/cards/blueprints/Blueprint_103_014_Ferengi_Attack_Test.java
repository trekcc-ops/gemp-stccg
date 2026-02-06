package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.MissionLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_103_014_Ferengi_Attack_Test extends AbstractAtTest {
    
    private FacilityCard outpost;
    private PersonnelCard picard;
    private MissionCard _mission;
    private PhysicalCard _ferengiAttack;
    private PersonnelCard troi;
    private PersonnelCard hobson;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_154", "Excavation", P1);
        outpost = builder.addFacility("101_104", P1); // Federation Outpost
        _ferengiAttack = builder.addSeedCardUnderMission("103_014", "Ferengi Attack", P2, _mission);
        troi = builder.addCardAboardShipOrFacility("101_205", "Deanna Troi", P1, outpost, PersonnelCard.class);
        hobson = builder.addCardAboardShipOrFacility("101_202", "Christopher Hobson", P1, outpost, PersonnelCard.class);
        picard = builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P1, outpost, PersonnelCard.class);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }


    @Test
    public void failDilemmaTest() throws Exception {
        initializeGame();

        List<PersonnelCard> personnelBeaming = List.of(troi, hobson, picard);

        beamCards(P1, outpost, personnelBeaming, _mission);
        for (PersonnelCard card : personnelBeaming) {
            assertFalse(outpost.hasCardInCrew(card));
        }

        attemptMission(P1, _game.getGameState().getAwayTeamForCard(troi), _mission);
        assertNotNull(_game.getAwaitingDecision(P2));
        assertInstanceOf(ArbitraryCardsSelectionDecision.class, _game.getAwaitingDecision(P2));

        assertTrue(_game.getGameState().getAwayTeamForCard(troi).getAttemptingPersonnel(_game).contains(hobson));

        selectCard(P2, hobson);
        assertEquals(Zone.DISCARD, hobson.getZone());
        assertTrue(_mission.getGameLocation(_game) instanceof MissionLocation missionLocation
                && missionLocation.isCompleted());
        assertEquals(Zone.REMOVED, _ferengiAttack.getZone());
    }

}