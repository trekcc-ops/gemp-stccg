package com.gempukku.stccg.packs;

import com.gempukku.stccg.AbstractServerTest;
import org.junit.jupiter.api.Test;


class PackTests extends AbstractServerTest {

    private static final ProductLibrary _productLib = new ProductLibrary(_cardLibrary);

    @Test
    final void Test1()  {
        _productLib.GetProduct("Random Rare Foil");
    }
    //protected static ProductLibrary _productLib = new ProductLibrary(_cardLibrary);
}