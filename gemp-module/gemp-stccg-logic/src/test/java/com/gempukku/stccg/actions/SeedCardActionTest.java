package com.gempukku.stccg.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeedCardActionTest extends AbstractAtTest {

    @Test
    public void serializePerformedActionTest() throws InvalidGameOperationException, CardNotFoundException,
            DecisionResultInvalidException, JsonProcessingException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        PhysicalCard door = builder.addSeedDeckCard("155_022", "Continuing Mission", P1);
        builder.setPhase(Phase.SEED_FACILITY);
        builder.startGame();
        Action seedAction = Iterables.getOnlyElement(getSelectableActionsOfClass(P1, SeedCardAction.class));
        selectAction(P1, seedAction);

        JsonNode actionNode = getJsonForPerformedAction(_game.getGameState(), P1, seedAction);
        assertEquals(9, actionNode.size());
        assertEquals(".playcard.SeedCardAction", actionNode.get("className").textValue());
        assertEquals(seedAction.getActionId(), actionNode.get("actionId").intValue());
        assertEquals("completed_success", actionNode.get("status").textValue());
        assertEquals("CORE", actionNode.get("destinationZone").textValue());
        assertEquals(door.getCardId(), actionNode.get("seededCardId").intValue());
        assertEquals(door.getCardId(), actionNode.get("targetCardId").intValue());
        assertEquals(door.getCardId(), actionNode.get("performingCardId").intValue());
        assertEquals(P1, actionNode.get("performingPlayerId").textValue());
        assertEquals(seedAction.getActionType().name(), actionNode.get("actionType").textValue());
    }

    @Test
    public void serializeSelectableActionTest() throws InvalidGameOperationException, CardNotFoundException,
            JsonProcessingException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        PhysicalCard door = builder.addSeedDeckCard("155_022", "Continuing Mission", P1);
        builder.setPhase(Phase.SEED_FACILITY);
        builder.startGame();
        Action seedAction = Iterables.getOnlyElement(getSelectableActionsOfClass(P1, SeedCardAction.class));

        JsonNode actionNode = getJsonForSelectableAction(_game.getGameState(), P1, seedAction);
        assertEquals(9, actionNode.size());
        assertEquals(".playcard.SeedCardAction", actionNode.get("className").textValue());
        assertEquals(seedAction.getActionId(), actionNode.get("actionId").intValue());
        assertEquals("virtual", actionNode.get("status").textValue());
        assertEquals("CORE", actionNode.get("destinationZone").textValue());
        assertEquals(door.getCardId(), actionNode.get("seededCardId").intValue());
        assertEquals(door.getCardId(), actionNode.get("targetCardId").intValue());
        assertEquals(door.getCardId(), actionNode.get("performingCardId").intValue());
        assertEquals(P1, actionNode.get("performingPlayerId").textValue());
        assertEquals(seedAction.getActionType().name(), actionNode.get("actionType").textValue());
    }
}