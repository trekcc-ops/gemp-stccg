package com.gempukku.stccg;

import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FormatLibraryTest extends AbstractAtTest {

    @Test
    public void libraryTest() {
        FormatLibrary formatLibrary = new FormatLibrary(_cardLibrary);

        for (GameFormat format : formatLibrary.getAllFormats().values()) {
            System.out.println(format.getName() + " missions = " + format.getMissions());
        }

        GameFormat debugFormat = formatLibrary.get("debug1e");
        assertNotNull(debugFormat);
    }
}