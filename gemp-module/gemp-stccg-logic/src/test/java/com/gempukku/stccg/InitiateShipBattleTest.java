package com.gempukku.stccg;

import com.gempukku.stccg.actions.playcard.SeedMissionCardAction;
import com.gempukku.stccg.actions.playcard.SeedOutpostAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.st1e.ST1EFacilitySeedPhaseProcess;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InitiateShipBattleTest extends AbstractAtTest {

    public InitiateShipBattleTest() throws InvalidGameOperationException, DecisionResultInvalidException,
            PlayerNotFoundException, CardNotFoundException, InvalidGameLogicException {
        setupSimple1EGame(30);

        PhysicalShipCard kvort = (PhysicalShipCard) newCardForGame("101_347", P1); // 8-6-6
        PhysicalShipCard kratak = (PhysicalShipCard) newCardForGame("103_118", P2); // 6-8-6
        PhysicalShipCard lukara = (PhysicalShipCard) newCardForGame("116_105", P1); // 7-7-7
        MissionCard mission = (MissionCard) newCardForGame("101_194", P1); // Wormhole Negotiations

        PersonnelCard klag1 = (PersonnelCard) newCardForGame("101_270", P1);
        PersonnelCard klag2 = (PersonnelCard) newCardForGame("101_270", P2);

        FacilityCard outpost1 = (FacilityCard) newCardForGame("101_105", P1);
        FacilityCard outpost2 = (FacilityCard) newCardForGame("101_105", P2);
        List<FacilityCard> outpostsToSeed = List.of(outpost1, outpost2);

        SeedMissionCardAction seedAction = new SeedMissionCardAction(mission);
        seedAction.setLocationZoneIndex(0);
        seedAction.seedCard(_game);

        for (FacilityCard facility : outpostsToSeed) {
            SeedOutpostAction seedOutpostAction = new SeedOutpostAction(facility);
            seedOutpostAction.setDestination(mission.getLocationDeprecatedOnlyUseForTests());
            seedOutpostAction.processEffect(_game, facility.getOwner());
        }

        kvort.reportToFacility(outpost1);
        klag1.reportToFacility(outpost1);
        kratak.reportToFacility(outpost2);
        klag2.reportToFacility(outpost2);

        assertTrue(kvort.isDocked());
        assertTrue(kratak.isDocked());

        _game.getGameState().initializePlayerOrder(new PlayerOrder(List.of(P1, P2)));
        _game.getGameState().setCurrentProcess(new ST1EFacilitySeedPhaseProcess(2));

        _game.startGame();
    }

    @Test
    public void initiateBattleTest() throws DecisionResultInvalidException, InvalidGameLogicException, PlayerNotFoundException, InvalidGameOperationException {
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());
    }

}