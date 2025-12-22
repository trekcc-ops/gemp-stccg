package com.gempukku.stccg;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.draft.DraftFormatLibrary;
import com.gempukku.stccg.formats.FormatLibrary;

public abstract class AbstractServerTest {
    protected static final CardBlueprintLibrary _cardLibrary;
    protected static final FormatLibrary _formatLibrary;
    protected static final DraftFormatLibrary _draftFormatLibrary;

    static {
        _cardLibrary = new CardBlueprintLibrary(true);
        _formatLibrary = new FormatLibrary(_cardLibrary);
        _draftFormatLibrary = new DraftFormatLibrary(_cardLibrary, _formatLibrary);
    }
}