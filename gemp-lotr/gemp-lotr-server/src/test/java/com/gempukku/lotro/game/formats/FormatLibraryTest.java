package com.gempukku.lotro.game.formats;

import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.adventure.DefaultAdventureLibrary;
import org.junit.Test;

public class FormatLibraryTest {
    @Test
    public void testLoad() {
        FormatLibrary library = new FormatLibrary(new DefaultAdventureLibrary(), new CardBlueprintLibrary());
    }
}
