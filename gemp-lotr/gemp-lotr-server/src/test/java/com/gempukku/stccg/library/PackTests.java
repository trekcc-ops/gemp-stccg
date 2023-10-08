package com.gempukku.stccg.library;

import com.gempukku.stccg.AbstractServerTest;
import com.gempukku.stccg.packs.ProductLibrary;
import org.junit.Test;


public class PackTests extends AbstractServerTest {

    protected static final ProductLibrary _productLib = new ProductLibrary(_cardLibrary);

    @Test
    public void Test1()  {
        _productLib.GetProduct("Random Rare Foil");
    }
    //protected static ProductLibrary _productLib = new ProductLibrary(_cardLibrary);
}
