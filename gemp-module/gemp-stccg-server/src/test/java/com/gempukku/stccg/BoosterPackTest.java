package com.gempukku.stccg;

import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.packs.ProductLibrary;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("MagicNumber")
public class BoosterPackTest extends AbstractServerTest {

    @Test
    public void openingPacks() {
        ProductLibrary productLibrary = new ProductLibrary();
        final List<GenericCardItem> items = productLibrary.get("Premiere - Booster").openPack(_cardLibrary);
        assertEquals(15, items.size());
    }
}