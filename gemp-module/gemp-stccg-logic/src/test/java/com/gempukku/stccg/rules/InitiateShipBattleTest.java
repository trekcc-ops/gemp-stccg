package com.gempukku.stccg.rules;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.battle.InitiateShipBattleAction;
import com.gempukku.stccg.actions.movecard.BeamCardsAction;
import com.gempukku.stccg.actions.playcard.SeedMissionCardAction;
import com.gempukku.stccg.actions.playcard.SeedOutpostAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.ShipBattleTargetDecision;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.st1e.ST1EFacilitySeedPhaseProcess;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InitiateShipBattleTest extends AbstractAtTest {

    private PhysicalShipCard attackingShip;
    private PhysicalShipCard defendingTarget;
    private PersonnelCard attackingLeader;
    private FacilityCard outpost1;

    private void setupGameState() throws CardNotFoundException, InvalidGameLogicException,
            InvalidGameOperationException, DecisionResultInvalidException, PlayerNotFoundException {
        setupGameState(false);
    }

    private void setupGameState(boolean includeDefendingLeader) throws CardNotFoundException, InvalidGameLogicException,
            InvalidGameOperationException, DecisionResultInvalidException, PlayerNotFoundException {
        MissionCard mission = (MissionCard) newCardForGame("101_194", P1); // Wormhole Negotiations

        if (attackingShip.isAffiliation(Affiliation.KLINGON)) {
            attackingLeader = (PersonnelCard) newCardForGame("101_270", P1); // Klag
        } else if (attackingShip.isAffiliation(Affiliation.FEDERATION)) {
            attackingLeader = (PersonnelCard) newCardForGame("101_215", P1); // Jean-Luc Picard
        }

        if (attackingShip.isAffiliation(Affiliation.KLINGON)) {
            outpost1 = (FacilityCard) newCardForGame("101_105", P1); // Klingon Outpost
        } else if (attackingShip.isAffiliation(Affiliation.FEDERATION)) {
            outpost1 = (FacilityCard) newCardForGame("101_104", P1); // Federation Outpost
        }

        FacilityCard outpost2 = (FacilityCard) newCardForGame("101_105", P2); // Klingon Outpost
        List<FacilityCard> outpostsToSeed = List.of(outpost1, outpost2);

        SeedMissionCardAction seedAction = new SeedMissionCardAction(mission);
        seedAction.setLocationZoneIndex(0);
        seedAction.seedCard(_game);

        for (FacilityCard facility : outpostsToSeed) {
            SeedOutpostAction seedOutpostAction = new SeedOutpostAction(facility);
            seedOutpostAction.setDestination(mission.getLocationDeprecatedOnlyUseForTests());
            seedOutpostAction.processEffect(_game, facility.getOwner());
        }

        this.attackingShip.reportToFacility(outpost1);
        attackingLeader.reportToFacility(outpost1);
        defendingTarget.reportToFacility(outpost2);

        assertTrue(this.attackingShip.isDocked());
        assertTrue(defendingTarget.isDocked());

        if (includeDefendingLeader) {
            PersonnelCard defendingLeader = (PersonnelCard) newCardForGame("101_270", P2); // Klag
            defendingLeader.reportToFacility(outpost2);
            BeamCardsAction beamAction = new BeamCardsAction(_game, _game.getPlayer(P2), defendingTarget);
            beamAction.setCardsToMove(List.of(defendingLeader));
            beamAction.setOrigin(outpost2);
            beamAction.setDestination(defendingTarget);
            beamAction.processEffect(_game);
        }

        _game.getGameState().initializePlayerOrder(new PlayerOrder(List.of(P1, P2)));
        _game.getGameState().setCurrentProcess(new ST1EFacilitySeedPhaseProcess(2));

        _game.startGame();

        beamCard(P1, this.attackingShip, attackingLeader, this.attackingShip);
        undockShip(P1, this.attackingShip);

        assertFalse(this.attackingShip.isDocked());
        assertTrue(this.attackingShip.getCrew().contains(attackingLeader));
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
    public void returnFireTest() throws DecisionResultInvalidException, PlayerNotFoundException, InvalidGameOperationException, InvalidGameLogicException, CardNotFoundException {

        // Defending ship has a leader aboard, so can return fire.
        // Both ships have WEAPONS > opponent's SHIELDS; battle should result in a tie.

        setupSimple1EGame(30);
        attackingShip = (PhysicalShipCard) newCardForGame("116_105", P1); // I.K.S. Lukara (7-7-7)
        defendingTarget = (PhysicalShipCard) newCardForGame("103_118", P2); // I.K.S. K'Ratak (6-8-6)
        setupGameState(true);
        defendingTarget.undockFromFacility();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());
        InitiateShipBattleAction battleAction = selectAction(InitiateShipBattleAction.class, null, P1);

        ShipBattleTargetDecision decision = (ShipBattleTargetDecision) _userFeedback.getAwaitingDecision(P1);
        decision.decisionMade(List.of(attackingShip), defendingTarget);
        _game.getGameState().playerDecisionFinished(P1, _userFeedback);
        _game.carryOutPendingActionsUntilDecisionNeeded();

        decision = (ShipBattleTargetDecision) _userFeedback.getAwaitingDecision(P2);
        decision.decisionMade(List.of(defendingTarget), attackingShip);
        _game.getGameState().playerDecisionFinished(P2, _userFeedback);
        _game.carryOutPendingActionsUntilDecisionNeeded();


        assertFalse(battleAction.wasWonBy(_game.getPlayer(P1)));
        assertTrue(attackingShip.isStopped());
        assertTrue(defendingTarget.isStopped());
        assertEquals(50, attackingShip.getHullIntegrity());
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
        beamCard(P1, this.attackingShip, attackingLeader, outpost1);
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

    @Test
    public void fedAttackTest() throws DecisionResultInvalidException, PlayerNotFoundException, InvalidGameOperationException, InvalidGameLogicException, CardNotFoundException {
        setupSimple1EGame(30);
        attackingShip = (PhysicalShipCard) newCardForGame("101_340", P1); // U.S.S. Oberth (6-4-7)
        defendingTarget = (PhysicalShipCard) newCardForGame("101_355", P2); // Yridian Shuttle (6-1-3)
        setupGameState();
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