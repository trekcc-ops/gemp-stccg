package com.gempukku.stccg.packs;

import com.gempukku.stccg.AbstractServerTest;
import org.junit.jupiter.api.Test;


public class PackTests extends AbstractServerTest {

    protected static final ProductLibrary _productLib = new ProductLibrary(_cardLibrary);

    @Test
    public void Test1()  {
        _productLib.GetProduct("Random Rare Foil");
    }
    //protected static ProductLibrary _productLib = new ProductLibrary(_cardLibrary);
}
