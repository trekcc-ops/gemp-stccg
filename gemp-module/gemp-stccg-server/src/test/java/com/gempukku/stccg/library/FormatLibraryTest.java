package com.gempukku.stccg.library;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.adventure.DefaultAdventureLibrary;
import com.gempukku.stccg.formats.FormatLibrary;
import org.junit.Test;

public class FormatLibraryTest {
    @Test
    public void testLoad() {
        FormatLibrary library = new FormatLibrary(new DefaultAdventureLibrary(), new CardBlueprintLibrary());
    }
}
