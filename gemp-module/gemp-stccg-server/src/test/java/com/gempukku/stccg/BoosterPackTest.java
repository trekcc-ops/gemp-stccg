package com.gempukku.stccg;

import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.packs.PackBox;
import com.gempukku.stccg.packs.ProductLibrary;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoosterPackTest extends AbstractServerTest {

    @Test
    public void openingPacks() {
        ProductLibrary _productLibrary = new ProductLibrary(_cardLibrary);
        Map<String, PackBox> products = _productLibrary.getAllProducts();
        final List<GenericCardItem> items = products.get("Premiere - Booster").openPack();
        assertEquals(15, items.size());
    }
}
