package com.gempukku.stccg.common;

import com.gempukku.stccg.common.filterable.Culture;
import org.junit.Test;

import static org.junit.Assert.assertSame;


public class CultureTest {
    @Test
    public void urukHaiEnum() {
        Culture urukHai = Enum.valueOf(Culture.class, "URUK_HAI");
        assertSame(Culture.URUK_HAI, urukHai);
    }
}
