package com.gempukku.stccg.cards;

import com.gempukku.stccg.AbstractLogicTest;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CardBlueprintLibraryTest extends AbstractLogicTest {

    @Test
    public void AllBlueprintsAreBuilt() {
        assertTrue(_cardLibrary.checkLoadSuccess());
    }
}
