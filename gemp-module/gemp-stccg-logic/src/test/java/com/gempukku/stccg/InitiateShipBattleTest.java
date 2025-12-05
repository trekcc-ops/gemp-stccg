package com.gempukku.stccg;

import com.gempukku.stccg.actions.battle.InitiateShipBattleAction;
import com.gempukku.stccg.actions.playcard.SeedMissionCardAction;
import com.gempukku.stccg.actions.playcard.SeedOutpostAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.ShipBattleTargetDecision;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.st1e.ST1EFacilitySeedPhaseProcess;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InitiateShipBattleTest extends AbstractAtTest {

    private PhysicalShipCard attackingShip;
    private PhysicalShipCard defendingTarget;
    private PersonnelCard klag1;
    private FacilityCard outpost1;

    private void setupGameState() throws CardNotFoundException, InvalidGameLogicException,
            InvalidGameOperationException, DecisionResultInvalidException, PlayerNotFoundException {
        MissionCard mission = (MissionCard) newCardForGame("101_194", P1); // Wormhole Negotiations

        klag1 = (PersonnelCard) newCardForGame("101_270", P1);
        PersonnelCard klag2 = (PersonnelCard) newCardForGame("101_270", P2);

        outpost1 = (FacilityCard) newCardForGame("101_105", P1); // Klingon Outpost
        FacilityCard outpost2 = (FacilityCard) newCardForGame("101_105", P2); // Klingon Outpost
        List<FacilityCard> outpostsToSeed = List.of(outpost1, outpost2);

        SeedMissionCardAction seedAction = new SeedMissionCardAction(_game, mission, 0);
        seedAction.seedCard(_game);

        for (FacilityCard facility : outpostsToSeed) {
            Player facilityOwner = _game.getPlayer(facility.getOwnerName());
            SeedOutpostAction seedOutpostAction = new SeedOutpostAction(_game, facility);
            seedOutpostAction.setDestination(mission);
            seedOutpostAction.processEffect(_game, facilityOwner);
        }

        this.attackingShip.reportToFacilityForTestingOnly(outpost1);
        klag1.reportToFacilityForTestingOnly(outpost1);
        defendingTarget.reportToFacilityForTestingOnly(outpost2);
        klag2.reportToFacilityForTestingOnly(outpost2);

        assertTrue(this.attackingShip.isDocked());
        assertTrue(defendingTarget.isDocked());

        _game.getGameState().initializePlayerOrder(new PlayerOrder(List.of(P1, P2)));
        _game.getGameState().setCurrentProcess(new ST1EFacilitySeedPhaseProcess(2));

        _game.startGame();

        beamCard(P1, this.attackingShip, klag1, this.attackingShip);
        undockShip(P1, this.attackingShip);

        assertFalse(this.attackingShip.isDocked());
        assertTrue(this.attackingShip.getCrew().contains(klag1));
    }

    @Test
    public void initiateBattleTest() throws DecisionResultInvalidException, PlayerNotFoundException, InvalidGameOperationException, InvalidGameLogicException, CardNotFoundException {
        setupSimple1EGame(30);
        attackingShip = (PhysicalShipCard) newCardForGame("116_105", P1); // I.K.S. Lukara (7-7-7)
        defendingTarget = (PhysicalShipCard) newCardForGame("103_118", P2); // I.K.S. K'Ratak (6-8-6)
        setupGameState();
        defendingTarget.undockFromFacility();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());
        InitiateShipBattleAction battleAction = selectAction(InitiateShipBattleAction.class, null, P1);
        ShipBattleTargetDecision decision = (ShipBattleTargetDecision) _userFeedback.getAwaitingDecision(P1);
        decision.decisionMade(List.of(attackingShip), defendingTarget);
        _game.getGameState().playerDecisionFinished(P1, _userFeedback);
        _game.carryOutPendingActionsUntilDecisionNeeded();
        assertTrue(battleAction.wasWonBy(_game.getPlayer(P1)));
        assertTrue(attackingShip.isStopped());
        assertTrue(defendingTarget.isStopped());
        assertEquals(100, attackingShip.getHullIntegrity());
        assertEquals(50, defendingTarget.getHullIntegrity());
    }

    @Test
    public void directHitBattleTest() throws DecisionResultInvalidException, PlayerNotFoundException, InvalidGameOperationException, InvalidGameLogicException, CardNotFoundException {
        setupSimple1EGame(30);
        attackingShip = (PhysicalShipCard) newCardForGame("116_105", P1); // I.K.S. Lukara (7-7-7)
        defendingTarget = (PhysicalShipCard) newCardForGame("101_355", P2); // Yridian Shuttle (6-1-3)
        setupGameState();
        defendingTarget.undockFromFacility();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());
        InitiateShipBattleAction battleAction = selectAction(InitiateShipBattleAction.class, null, P1);
        ShipBattleTargetDecision decision = (ShipBattleTargetDecision) _userFeedback.getAwaitingDecision(P1);
        decision.decisionMade(List.of(attackingShip), defendingTarget);
        _game.getGameState().playerDecisionFinished(P1, _userFeedback);
        _game.carryOutPendingActionsUntilDecisionNeeded();
        assertTrue(battleAction.wasWonBy(_game.getPlayer(P1)));
        assertTrue(attackingShip.isStopped());
        assertFalse(defendingTarget.isStopped());
        assertEquals(100, attackingShip.getHullIntegrity());
        assertEquals(0, defendingTarget.getHullIntegrity());
    }

    @Test
    public void noWeaponsTest() throws DecisionResultInvalidException, PlayerNotFoundException, InvalidGameOperationException, InvalidGameLogicException, CardNotFoundException {
        setupSimple1EGame(30);
        attackingShip = (PhysicalShipCard) newCardForGame("116_105", P1); // I.K.S. Lukara (7-7-7)
        defendingTarget = (PhysicalShipCard) newCardForGame("101_355", P2); // Yridian Shuttle (6-1-3)
        setupGameState();
        beamCard(P1, this.attackingShip, klag1, outpost1);
        defendingTarget.undockFromFacility();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        boolean actionNotFound = false;

        try {
            selectAction(InitiateShipBattleAction.class, null, P1);
        } catch(DecisionResultInvalidException exp) {
            actionNotFound = true;
        }
        assertTrue(actionNotFound);
    }

}