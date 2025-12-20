package com.gempukku.stccg;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.formats.FormatLibrary;

public abstract class AbstractServerTest {
    protected static final CardBlueprintLibrary _cardLibrary;
    protected static final FormatLibrary _formatLibrary;

    static {
        _cardLibrary = new CardBlueprintLibrary(true);
        _formatLibrary = new FormatLibrary(_cardLibrary);
    }
}