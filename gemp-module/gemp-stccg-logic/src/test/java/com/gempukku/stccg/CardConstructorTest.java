package com.gempukku.stccg;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CardConstructorTest extends AbstractAtTest {

    @Test
    void testConstructors() {
        List<String> blueprintIds = _cardLibrary.getAllBlueprintIds();
        List<String> blueprintsNotCreated = new ArrayList<>();
        for (String blueprintId : blueprintIds) {
            PhysicalCard card = _cardLibrary.get(blueprintId).createPhysicalCard(1, P1);
            if (card == null) {
                blueprintsNotCreated.add(blueprintId);
            }
        }
        assertTrue(blueprintsNotCreated.isEmpty());
    }

}