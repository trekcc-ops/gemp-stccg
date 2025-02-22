package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.formats.DefaultGameFormat;
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

    public static String makeBold(String text) {
        return ("<b>" + text + "</b>");
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

    public static String parseChatMessage(String message) {
        String newMsg = _renderer.render(_parser.parse(message));
        // Prevent quotes with newlines from displaying side-by-side
        newMsg = newMsg.replaceAll(
                "</blockquote>[\n \t]*<blockquote>", "</blockquote>" + NEWLINE + "<blockquote>");
        //Make all links open in a new tab
        newMsg = newMsg.replaceAll("<(a href=\".*?\")>", "<$1 target=\"blank\">");
        return newMsg;
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