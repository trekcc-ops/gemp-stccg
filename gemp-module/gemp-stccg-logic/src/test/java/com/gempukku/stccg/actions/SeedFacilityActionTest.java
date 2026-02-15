package com.gempukku.stccg.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.actions.playcard.SeedFacilityAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeedFacilityActionTest extends AbstractAtTest {

    @Test
    public void serializePerformedActionTest() throws InvalidGameOperationException, CardNotFoundException,
            DecisionResultInvalidException, JsonProcessingException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.addMission("219_022", "Evade Sensors", P1);
        PhysicalCard husnockOutpost = builder.addSeedDeckCard("111_009", "Husnock Outpost", P1);
        builder.setPhase(Phase.SEED_FACILITY);
        builder.startGame();
        Action seedAction = seedFacility(P1, husnockOutpost);
        JsonNode actionNode = getJsonForPerformedAction(_game.getGameState(), P1, seedAction);
        assertEquals(9, actionNode.size());
        assertEquals(".playcard.SeedFacilityAction", actionNode.get("className").textValue());
        assertEquals(seedAction.getActionId(), actionNode.get("actionId").intValue());
        assertEquals("completed_success", actionNode.get("status").textValue());
        assertEquals("AT_LOCATION", actionNode.get("destinationZone").textValue());
        assertEquals(husnockOutpost.getCardId(), actionNode.get("seededCardId").intValue());
        assertEquals(husnockOutpost.getCardId(), actionNode.get("targetCardId").intValue());
        assertEquals(husnockOutpost.getCardId(), actionNode.get("performingCardId").intValue());
        assertEquals(P1, actionNode.get("performingPlayerId").textValue());
        assertEquals(seedAction.getActionType().name(), actionNode.get("actionType").textValue());
    }

    @Test
    public void serializeSelectableActionTest() throws InvalidGameOperationException, CardNotFoundException,
            DecisionResultInvalidException, JsonProcessingException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.addMission("219_022", "Evade Sensors", P1);
        PhysicalCard husnockOutpost = builder.addSeedDeckCard("111_009", "Husnock Outpost", P1);
        builder.setPhase(Phase.SEED_FACILITY);
        builder.startGame();
        SeedFacilityAction seedAction =
                Iterables.getOnlyElement(getSelectableActionsOfClass(P1, SeedFacilityAction.class));
        JsonNode actionNode = getJsonForSelectableAction(_game.getGameState(), P1, seedAction);
        assertEquals(9, actionNode.size());
        assertEquals(".playcard.SeedFacilityAction", actionNode.get("className").textValue());
        assertEquals(seedAction.getActionId(), actionNode.get("actionId").intValue());
        assertEquals("virtual", actionNode.get("status").textValue());
        assertEquals("AT_LOCATION", actionNode.get("destinationZone").textValue());
        assertEquals(husnockOutpost.getCardId(), actionNode.get("seededCardId").intValue());
        assertEquals(husnockOutpost.getCardId(), actionNode.get("targetCardId").intValue());
        assertEquals(husnockOutpost.getCardId(), actionNode.get("performingCardId").intValue());
        assertEquals(P1, actionNode.get("performingPlayerId").textValue());
        assertEquals(seedAction.getActionType().name(), actionNode.get("actionType").textValue());
    }
}