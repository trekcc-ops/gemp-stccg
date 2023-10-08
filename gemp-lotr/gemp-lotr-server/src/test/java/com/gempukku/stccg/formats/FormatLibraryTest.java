package com.gempukku.stccg.formats;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.adventure.DefaultAdventureLibrary;
import org.junit.Test;

public class FormatLibraryTest {
    @Test
    public void testLoad() {
        FormatLibrary library = new FormatLibrary(new DefaultAdventureLibrary(), new CardBlueprintLibrary());
    }
}
