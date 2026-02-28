package com.gempukku.stccg.cards;

import com.gempukku.stccg.formats.FormatLibrary;
import org.junit.jupiter.api.Test;

public class FormatLibraryTest {

    @Test
    public void formatlibraryTest() {
        // Should throw an error if there are problems loading formats
        CardBlueprintLibrary bpLibrary = new CardBlueprintLibrary();
        FormatLibrary formatLibrary = new FormatLibrary(bpLibrary);
    }

}