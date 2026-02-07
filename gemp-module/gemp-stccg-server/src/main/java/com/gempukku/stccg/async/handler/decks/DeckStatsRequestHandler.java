package com.gempukku.stccg.async.handler.decks;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.HTMLUtils;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.formats.DefaultGameFormat;
import com.gempukku.stccg.formats.FormatLibrary;

import java.util.List;

public class DeckStatsRequestHandler extends DeckRequestHandler implements UriRequestHandler {

    private static final String YELLOW = "yellow";
    private static final String RED = "red";
    private static final String GREEN = "green";
    private final String _targetFormat;
    private final String _deckContents;
    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final FormatLibrary _formatLibrary;

    DeckStatsRequestHandler(
            @JsonProperty("targetFormat")
            String targetFormat,
            @JsonProperty("deckContents")
            String deckContents,
            @JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
            @JacksonInject FormatLibrary formatLibrary) {
        _targetFormat = targetFormat;
        _deckContents = deckContents;
        _cardBlueprintLibrary = cardBlueprintLibrary;
        _formatLibrary = formatLibrary;
    }

    static String makeColor(String text, String color) {
        return "<font color='" + color + "'>" + text + "</font>";
    }

    public static String getDeckValidation(CardBlueprintLibrary library, CardDeck deck, DefaultGameFormat format) {
        StringBuilder sb = new StringBuilder();

        StringBuilder valid = new StringBuilder();
        StringBuilder invalid = new StringBuilder();

        List<String> validation = format.validateDeck(library, deck);
        List<String> errataValidation = null;
        if (!format.getErrataCardMap().isEmpty()) {
            CardDeck deckWithErrata = format.applyErrata(library, deck);
            errataValidation = format.validateDeck(library, deckWithErrata);
        }

        String formatName = format.getName();
        String formatNameInBold = "<b>" + formatName + "</b>";

        if(validation.isEmpty()) {
            valid.append(formatNameInBold).append(": ").append(makeColor("Valid", GREEN)).append(HTMLUtils.NEWLINE);
        }
        else {
            String messageColor;
            if (errataValidation != null && errataValidation.isEmpty()) {
                valid.append(formatNameInBold).append(": ").append(makeColor("Valid", GREEN)).append(" ");
                valid.append(makeColor("(with errata automatically applied)", YELLOW)).append(HTMLUtils.NEWLINE);
                messageColor = YELLOW;
            } else {
                invalid.append(formatNameInBold).append(": ");
                messageColor = RED;
            }
            String output = HTMLUtils.replaceNewlines(String.join(HTMLUtils.NEWLINE, validation));
            invalid.append(makeColor(output, messageColor)).append(HTMLUtils.NEWLINE);
        }

        sb.append(valid);
        sb.append(invalid);
        return sb.toString();
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {

        DefaultGameFormat format = validateFormat(_targetFormat, _formatLibrary);
        CardDeck deck = new CardDeck("tempDeck", _deckContents, format);
        if(format == null || _targetFormat == null)
        {
            responseWriter.writeHtmlResponse("Invalid format: " + _targetFormat);
        }

        assert format != null;
        String response = getDeckValidation(_cardBlueprintLibrary, deck, format);
        responseWriter.writeHtmlResponse(response);
    }



}