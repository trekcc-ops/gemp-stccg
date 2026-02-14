package com.gempukku.stccg;

import com.gempukku.stccg.actions.battle.InitiateShipBattleAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.ShipBattleTargetDecision;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InitiateShipBattleTest extends AbstractAtTest {

    private ShipCard attackingShip;
    private ShipCard defendingTarget;

    private void initializeGame(String defendingCardTitle, boolean includeLeaderOnAttackingShip)
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        MissionCard _mission = builder.addMission("101_194", "Wormhole Negotiations", P1);
        attackingShip = builder.addShipInSpace("116_105", "I.K.C. Lukara", P1, _mission); // 7-7-7

        defendingTarget = switch(defendingCardTitle) {
            case "K'Ratak" ->
                    builder.addShipInSpace("103_118", "I.K.C. K'Ratak", P2, _mission); // 6-8-6
            case "Yridian Shuttle" ->
                    builder.addShipInSpace("101_355", "Yridian Shuttle", P2, _mission); // 6-1-3
            default -> throw new CardNotFoundException(
                    "Test not designed to work with defending target " + defendingCardTitle);
        };

        if (includeLeaderOnAttackingShip) {
            builder.addCardAboardShipOrFacility(
                    "101_270", "Klag", P1, attackingShip, PersonnelCard.class);
        }

        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }


    @Test
    public void initiateBattleTest() throws DecisionResultInvalidException, InvalidGameOperationException,
            CardNotFoundException {

        // Initiate battle: Lukara vs. K'Ratak
        // Should result in a "hit" with 50% HULL reduction
        initializeGame("K'Ratak", true);
        InitiateShipBattleAction battleAction = initiateBattle(P1);

        ShipBattleTargetDecision decision = (ShipBattleTargetDecision) _game.getAwaitingDecision(P1);
        decision.decisionMade(List.of(attackingShip), defendingTarget);
        _game.removeDecision(P1);
        _game.carryOutPendingActionsUntilDecisionNeeded();
        assertTrue(battleAction.wasWonBy(_game.getPlayer(P1)));
        assertTrue(attackingShip.isStopped());
        assertTrue(defendingTarget.isStopped());
        assertEquals(100, attackingShip.getHullIntegrity());
        assertEquals(50, defendingTarget.getHullIntegrity());
    }

    @Test
    public void directHitBattleTest() throws DecisionResultInvalidException, InvalidGameOperationException,
            CardNotFoundException {

        // Initiate battle: Lukara vs. Yridian Shuttle
        // Should result in a "direct hit" with 100% HULL reduction
        initializeGame("Yridian Shuttle", true);

        InitiateShipBattleAction battleAction = initiateBattle(P1);
        ShipBattleTargetDecision decision = (ShipBattleTargetDecision) _game.getAwaitingDecision(P1);
        decision.decisionMade(List.of(attackingShip), defendingTarget);
        _game.removeDecision(P1);
        _game.carryOutPendingActionsUntilDecisionNeeded();
        assertTrue(battleAction.wasWonBy(_game.getPlayer(P1)));
        assertTrue(attackingShip.isStopped());
        assertFalse(defendingTarget.isStopped());
        assertEquals(100, attackingShip.getHullIntegrity());
        assertEquals(0, defendingTarget.getHullIntegrity());
    }

    @Test
    public void noWeaponsTest() throws DecisionResultInvalidException, InvalidGameOperationException,
            CardNotFoundException {

        // Initiate battle: Lukara vs. Yridian Shuttle
        // Beaming Klag off the ship means Lukara's WEAPONS are disabled and battle can't be initiated
        initializeGame("Yridian Shuttle", false);

        assertThrows(DecisionResultInvalidException.class, () -> initiateBattle(P1));
    }

}