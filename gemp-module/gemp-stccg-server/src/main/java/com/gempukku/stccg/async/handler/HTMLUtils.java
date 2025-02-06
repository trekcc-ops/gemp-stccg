package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.collection.MutableCardCollection;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import org.apache.commons.lang.StringEscapeUtils;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.Renderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.HtmlWriter;

import java.util.*;

public class HTMLUtils {

    public static final String NEWLINE = "<br/>";
    private static final String YELLOW = "yellow";
    private static final String RED = "red";
    private static final String GREEN = "green";

    public static final String HALL_WELCOME_MESSAGE = "You're now in the Game Hall, " +
            "use /help to get a list of available commands." + NEWLINE +
            "Don't forget to check out the new Discord chat integration! " +
            "Click the 'Switch to Discord' button in the lower right ---->";

    public static final String SINGLE_ELIMINATION_ON_DEMAND_PRIZES = "<div class='prizeHint' value='" +
            "2 wins - 2 boosters and a random promo, 1 win - 2 boosters, 0 wins - 1 booster'>(2+promo)-2-1</div>";

    private final static List<Extension> _adminExt =
            Arrays.asList(StrikethroughExtension.create(), AutolinkExtension.create());
    final private static Parser _parser = Parser.builder().extensions(_adminExt).build();
    final private static Renderer _renderer = HtmlRenderer.builder()
            .nodeRendererFactory(LinkShredder::new)
            .extensions(_adminExt)
            .escapeHtml(true)
            .sanitizeUrls(true)
            .softbreak(NEWLINE)
            .build();

    private HTMLUtils() {
    }

    static String generateCardTooltip(CardBlueprint blueprint) {
        return "<span class=\"tooltip\">" + blueprint.getFullName()
                + "<span><img class=\"ttimage\" src=\"" + blueprint.getImageUrl() + "\"></span></span>";
    }

    public static String makeBold(String text) {
        return ("<b>" + text + "</b>");
    }

    static String makeColor(String text, String color) {
        return "<font color='" + color + "'>" + text + "</font>";
    }

    static final String listCards(String deckName, String filter, CardCollection deckCards, boolean countCards,
                                  FormatLibrary formatLibrary, boolean showToolTip, CardBlueprintLibrary cardLibrary)
            throws CardNotFoundException {
        StringBuilder sb = new StringBuilder();
        sb.append(NEWLINE).append(makeBold(deckName)).append(":").append(NEWLINE);
        for (GenericCardItem item :
                SortAndFilterCards.process(filter, deckCards.getAll(), cardLibrary, formatLibrary)) {
            if (countCards)
                sb.append(item.getCount()).append("x ");
            String blueprintId = item.getBlueprintId();
            CardBlueprint blueprint = cardLibrary.getCardBlueprint(blueprintId);
            String cardText;
            if (showToolTip) {
                cardText = generateCardTooltip(blueprint);
            } else {
                cardText = blueprint.getFullName();
            }
            sb.append(cardText).append(NEWLINE);
        }
        return sb.toString();
    }

    public static final String getHTMLDeck(CardDeck deck, boolean showToolTip, FormatLibrary formatLibrary,
                                           CardBlueprintLibrary cardBlueprintLibrary)
            throws CardNotFoundException {

        StringBuilder result = new StringBuilder();

        MutableCardCollection deckCards = new DefaultCardCollection();
        for (String card : deck.getDrawDeckCards())
            deckCards.addItem(cardBlueprintLibrary.getBaseBlueprintId(card), 1);

        for (SubDeck subDeck : SubDeck.values()) {
            if (deck.getSubDeck(subDeck) != null && !deck.getSubDeck(subDeck).isEmpty()) {

            }
        }

/*        result.append(listCards("Adventure Deck","cardType:SITE sort:twilight",
                deckCards,false, formatLibrary, showToolTip, cardBlueprintLibrary));
        result.append(listCards("Free Peoples Draw Deck","sort:cardType,name",
                deckCards,true, formatLibrary, showToolTip, cardBlueprintLibrary));
        result.append(listCards("Shadow Draw Deck","sort:cardType,name",
                deckCards,true, formatLibrary, showToolTip, cardBlueprintLibrary)); */

        return result.toString();
    }

    static final String convertDeckToHTML(CardDeck deck, String author, FormatLibrary formatLibrary,
                                          CardBlueprintLibrary blueprintLibrary) throws CardNotFoundException {

        if (deck == null)
            return null;

        StringBuilder result = new StringBuilder();
        result.append("""
<html>
    <style>
        body {
            margin:50;
        }
        
        .tooltip {
          border-bottom: 1px dotted black; /* If you want dots under the hoverable text */
          color:#0000FF;
        }
        
        .tooltip span, .tooltip title {
            display:none;
        }
        .tooltip:hover span:not(.click-disabled),.tooltip:active span:not(.click-disabled) {
            display:block;
            position:fixed;
            overflow:hidden;
            background-color: #FAEBD7;
            width:auto;
            z-index:9999;
            top:20%;
            left:350px;
        }
        /* This prevents tooltip images from automatically shrinking if they are near the window edge.*/
        .tooltip span > img {
            max-width:none !important;
            overflow:hidden;
        }
                        
    </style>
    <body>""");
        result.append("<h1>").append(StringEscapeUtils.escapeHtml(deck.getDeckName())).append("</h1>");
        result.append("<h2>Format: ").append(StringEscapeUtils.escapeHtml(deck.getTargetFormat())).append("</h2>");
        if(author != null) {
            result.append("<h2>Author: ").append(StringEscapeUtils.escapeHtml(author)).append("</h2>");
        }

        result.append(getHTMLDeck(deck, true, formatLibrary, blueprintLibrary));
        result.append("<h3>Notes</h3>").append(NEWLINE).append(replaceNewlines(deck.getNotes()));
        result.append("</body></html>");

        return result.toString();
    }

    static String getDeckValidation(CardBlueprintLibrary library, CardDeck deck, GameFormat format) {
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
        String formatNameInBold = makeBold(formatName);

        if(validation.isEmpty()) {
            valid.append(formatNameInBold).append(": ").append(makeColor("Valid", GREEN)).append(NEWLINE);
        }
        else {
            String messageColor;
            if (errataValidation != null && errataValidation.isEmpty()) {
                valid.append(formatNameInBold).append(": ").append(makeColor("Valid", GREEN)).append(" ");
                valid.append(makeColor("(with errata automatically applied)", YELLOW)).append(NEWLINE);
                messageColor = YELLOW;
            } else {
                invalid.append(formatNameInBold).append(": ");
                messageColor = RED;
            }
            String output = replaceNewlines(String.join(NEWLINE, validation));
            invalid.append(makeColor(output, messageColor)).append(NEWLINE);
        }

        sb.append(valid);
        sb.append(invalid);
        return sb.toString();
    }

    static String getTournamentDeck(CardDeck deck, String playerName, FormatLibrary formatLibrary,
                                    CardBlueprintLibrary cardBlueprintLibrary) throws CardNotFoundException {
        return "<html><body>" +
                "<h1>" + StringEscapeUtils.escapeHtml(deck.getDeckName()) + "</h1>" +
                "<h2>by " + playerName + "</h2>" +
                getHTMLDeck(deck, false, formatLibrary, cardBlueprintLibrary) +
                "</body></html>";
    }

    public static String getPlayTestMessage(Map<String, String> playerRecordingId, String winnerName,
                                            String loserName) {
        String url = AppConfig.getPlaytestUrl() +
                AppConfig.getPlaytestPrefixUrl() + winnerName + "$" +
                playerRecordingId.get(winnerName) + "%20" +
                AppConfig.getPlaytestPrefixUrl() + loserName + "$" + playerRecordingId.get(loserName);
        return "Thank you for playtesting!  " +
                "If you have any feedback, bugs, or other issues to report about this match, <a href= '" +
                url + "'>please do so using this form.</a>";
    }

    static String parseChatMessage(String message) {
        String newMsg = _renderer.render(_parser.parse(message));
        // Prevent quotes with newlines from displaying side-by-side
        newMsg = newMsg.replaceAll(
                "</blockquote>[\n \t]*<blockquote>", "</blockquote>" + NEWLINE + "<blockquote>");
        //Make all links open in a new tab
        newMsg = newMsg.replaceAll("<(a href=\".*?\")>", "<$1 target=\"blank\">");
        return newMsg;
    }

    public static String serializeFormatForHall(GameFormat format, CardBlueprintLibrary library)
            throws CardNotFoundException {
        StringBuilder result = new StringBuilder();
        result.append(makeBold(format.getName()));
        result.append("<ul>");
        result.append("<li>valid sets: ");
        for (String setId : format.getValidSetIdsAsStrings())
            result.append(setId).append(", ");
        result.append("</li>");
        if (!format.getBannedCards().isEmpty()) {
            result.append("<li>Banned cards (can't be played): ");
            appendFormatCards(result, format.getBannedCards(), library);
            result.append("</li>");
        }
        if (!format.getRestrictedCardNames().isEmpty()) {
            result.append("<li>Restricted by card name: ");
            boolean first = true;
            for (String cardName : format.getRestrictedCardNames()) {
                if (!first)
                    result.append(", ");
                result.append(cardName);
                first = false;
            }
            result.append("</li>");
        }
        if (!format.getErrataCardMap().isEmpty()) {
            result.append("<li>Errata: ");
            appendFormatCards(result, new ArrayList<>(new LinkedHashSet<>(format.getErrataCardMap().values())),
                    library);
            result.append("</li>");
        }
        if (!format.getValidCards().isEmpty()) {
            result.append("<li>Additional valid: ");
            List<String> additionalValidCards = format.getValidCards();
            appendFormatCards(result, additionalValidCards, library);
            result.append("</li>");
        }
        result.append("</ul>");
        return result.toString();
    }

    private static void appendFormatCards(StringBuilder result, Collection<String> additionalValidCards,
                                          CardBlueprintLibrary blueprintLibrary)
            throws CardNotFoundException {
        if (!additionalValidCards.isEmpty()) {
            for (String blueprintId : additionalValidCards)
                result.append(blueprintLibrary.getCardBlueprint(blueprintId).getCardLink()).append(", ");
            if (additionalValidCards.isEmpty())
                result.append("none,");
        }
    }

    public static String replaceNewlines(String message) {
        return message.replace("\n", NEWLINE);
    }


    //Processing to implement:
    // + quotes restricted to one line
    // - triple quote to avoid this??
    // + remove url text processing
    // + remove image processing
    // - re-enable bare url linking
    static class LinkShredder implements NodeRenderer {

        private final HtmlWriter html;

        LinkShredder(HtmlNodeRendererContext context) {
            this.html = context.getWriter();
        }

        @Override
        public final Set<Class<? extends Node>> getNodeTypes() {
            // Return the node types we want to use this renderer for.
            return new HashSet<>(Arrays.asList(
               Link.class,
               Image.class
            ));
        }

        @Override
        public final void render(Node node) {
            if(node instanceof Link link) {
                if(link.getTitle() != null) {
                    html.text(link.getTitle() + ": " + link.getDestination());
                }
                else {
                    if(link.getFirstChild() != null
                            && link.getFirstChild() instanceof Text text
                            && !text.getLiteral().equals(link.getDestination()))
                    {
                        html.text(text.getLiteral() + ": " + link.getDestination());
                    }
                    else {
                        html.tag("a", Collections.singletonMap("href", link.getDestination()));
                        html.text(link.getDestination());
                        html.tag("/a");
                    }
                }

            }
            else if(node instanceof Image image){
                html.text(image.getTitle() + ": " + image.getDestination());
            }
        }
    }
}