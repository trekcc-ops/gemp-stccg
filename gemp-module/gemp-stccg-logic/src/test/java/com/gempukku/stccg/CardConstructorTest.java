package com.gempukku.stccg;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CardConstructorTest extends AbstractAtTest {

    @Test
    void testConstructors() {
        List<String> blueprintIds = _cardLibrary.getAllBlueprintIds();
        for (String blueprintId : blueprintIds) {
            PhysicalCard card = _cardLibrary.get(blueprintId).createPhysicalCard(1, P1);
            if (card == null) {
                System.out.println(blueprintId);
            }
        }
    }

}