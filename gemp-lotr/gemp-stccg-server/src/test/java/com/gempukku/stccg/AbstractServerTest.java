package com.gempukku.stccg;

import com.gempukku.stccg.adventure.DefaultAdventureLibrary;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.game.DefaultUserFeedback;
import com.gempukku.stccg.game.LotroGame;

public abstract class AbstractServerTest {
    protected static final CardBlueprintLibrary _cardLibrary;
    protected static final FormatLibrary _formatLibrary;

    static {
        _cardLibrary = new CardBlueprintLibrary();
        _formatLibrary = new FormatLibrary(new DefaultAdventureLibrary(), _cardLibrary);
    }

    protected LotroGame _game;
    protected DefaultUserFeedback _userFeedback;
    public static final String P1 = "player1";
    public static final String P2 = "player2";

}
