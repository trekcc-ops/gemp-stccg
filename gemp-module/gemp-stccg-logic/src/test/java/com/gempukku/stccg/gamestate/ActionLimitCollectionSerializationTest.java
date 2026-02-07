package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.modifiers.DefaultLimitCounter;
import com.gempukku.stccg.modifiers.LimitCounter;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ActionLimitCollectionSerializationTest extends AbstractAtTest {
    @Test
    public void limitCounterTest() throws CardNotFoundException, JsonProcessingException {
        CardBlueprint getItDoneBlueprint = _cardLibrary.getCardBlueprint("155_026");
        ActionBlueprint getItDoneAction = Iterables.getOnlyElement(getItDoneBlueprint.getActionBlueprintsForTestingOnly());
        PhysicalCard getItDone = getItDoneBlueprint.createPhysicalCard(1, P1);
        assertNotNull(getItDoneAction);

        ActionLimitCollection actionLimits = new ActionLimitCollection();
        LimitCounter limitCounter = actionLimits.getUntilEndOfTurnLimitCounter(getItDone, getItDoneAction);
        assertEquals(limitCounter.getUsedLimit(), 0);
        limitCounter.incrementToLimit(1, 1);
        assertEquals(limitCounter.getUsedLimit(), 1);

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(limitCounter);
        LimitCounter limitCounterCopy = mapper.readValue(jsonString, DefaultLimitCounter.class);
        assertEquals(limitCounterCopy.getUsedLimit(), 1);

    }

    @Test
    public void limitCollectionTest() throws CardNotFoundException, JsonProcessingException {
        CardBlueprint getItDoneBlueprint = _cardLibrary.getCardBlueprint("155_026");
        ActionBlueprint getItDoneAction = Iterables.getOnlyElement(getItDoneBlueprint.getActionBlueprintsForTestingOnly());
        PhysicalCard getItDone = getItDoneBlueprint.createPhysicalCard(1, P1);
        assertNotNull(getItDoneAction);

        ActionLimitCollection actionLimits = new ActionLimitCollection();
        LimitCounter limitCounter = actionLimits.getUntilEndOfTurnLimitCounter(getItDone, getItDoneAction);
        assertEquals(limitCounter.getUsedLimit(), 0);
        limitCounter.incrementToLimit(1, 1);
        assertEquals(limitCounter.getUsedLimit(), 1);

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(actionLimits);
        ActionLimitCollection collectionCopy = mapper.readValue(jsonString, ActionLimitCollection.class);
        LimitCounter limitCounterCopy = collectionCopy.getUntilEndOfTurnLimitCounter(getItDone, getItDoneAction);
        assertEquals(limitCounterCopy.getUsedLimit(), 1);

    }
}