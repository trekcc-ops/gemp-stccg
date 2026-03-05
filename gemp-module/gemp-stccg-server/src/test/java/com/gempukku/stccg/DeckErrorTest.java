package com.gempukku.stccg;

import com.gempukku.stccg.common.CardDeck;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeckErrorTest extends AbstractServerTest {

    String deckContents = "DRAW_DECK|135_011,101_214,101_215,101_216,101_217,101_218,101_219,101_196,101_197,101_198,101_199,101_232,101_331,101_211,101_332,101_212,101_213,127_035,101_334,101_203,101_225,101_204,101_226,101_205,101_206,101_229,101_208,101_209,244_014,101_220,101_221,101_200,101_222,101_201,101_223,101_202|SEED_DECK|101_104|MISSIONS|101_194,101_150,101_151,101_153,101_155,101_146";

    @Test
    public void deckTest() {
        CardDeck deck = new CardDeck("bork", deckContents, "1E Modern Complete", "no notes");
        DeckValidation validation =
                new DeckValidation(deck, _cardLibrary, _formatLibrary.getFormatByName("1E Modern Complete"));
        assertEquals(0, validation.getAllErrors().size());
    }

}