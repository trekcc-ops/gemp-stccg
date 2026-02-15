package com.gempukku.stccg.async.handler.chat;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.HTMLUtils;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.chat.ChatCommandErrorException;
import com.gempukku.stccg.chat.ChatRoomMediator;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.chat.PrivateInformationException;
import com.gempukku.stccg.database.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import java.net.HttpURLConnection;
import java.util.*;
import java.util.regex.Pattern;

public class SendChatMessageRequestHandler implements UriRequestHandler {

    private final static List<Extension> _adminExt =
            Arrays.asList(StrikethroughExtension.create(), AutolinkExtension.create());
    final private static Renderer _renderer = HtmlRenderer.builder()
            .nodeRendererFactory(LinkShredder::new)
            .extensions(_adminExt)
            .escapeHtml(true)
            .sanitizeUrls(true)
            .softbreak(HTMLUtils.NEWLINE)
            .build();
    final private static Parser _parser = Parser.builder().extensions(_adminExt).build();
    private final String _message;
    private final Pattern QuoteExtender =
            Pattern.compile("^([ \t]*>[ \t]*.+)(?=\n[ \t]*[^>])", Pattern.MULTILINE);

    private final ChatRoomMediator _chatRoom;

    private static final Logger LOGGER = LogManager.getLogger(SendChatMessageRequestHandler.class);

    SendChatMessageRequestHandler(
            @JsonProperty("roomName")
            String roomName,
            @JsonProperty("message")
            String message,
            @JacksonInject ChatServer chatServer
            ) throws HttpProcessingException {
        _message = message;
        _chatRoom = chatServer.getChatRoom(roomName);
        if (_chatRoom == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
    }

    private static String parseChatMessage(String message) {
        String newMsg = _renderer.render(_parser.parse(message));
        // Prevent quotes with newlines from displaying side-by-side
        newMsg = newMsg.replaceAll(
                "</blockquote>[\n \t]*<blockquote>", "</blockquote>" + HTMLUtils.NEWLINE + "<blockquote>");
        //Make all links open in a new tab
        newMsg = newMsg.replaceAll("<(a href=\".*?\")>", "<$1 target=\"blank\">");
        return newMsg;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException {

        User resourceOwner = request.user();
        String uri = request.uri();

        try {
            if (_message != null && !_message.trim().isEmpty()) {
                String newMsg;
                newMsg = _message.trim().replaceAll("\n\n\n+", "\n\n\n");
                newMsg = QuoteExtender.matcher(newMsg).replaceAll("$1\n");
                //Escaping underscores so that URLs with lots of underscores (i.e. wiki links) aren't mangled
                // Besides, who uses _this_ instead of *this*?
                newMsg = newMsg.replace("_", "\\_");

                //Need to preserve any commands being made
                if (!newMsg.startsWith("/"))
                    newMsg = parseChatMessage(newMsg);
                _chatRoom.sendChatMessage(resourceOwner, newMsg);
                responseWriter.writeXmlOkResponse();
            }
        } catch (PrivateInformationException exp) {
            logHttpError(LOGGER, HttpURLConnection.HTTP_FORBIDDEN, uri, exp);
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
        } catch (ChatCommandErrorException exp) {
            logHttpError(LOGGER, HttpURLConnection.HTTP_BAD_REQUEST, uri, exp);
            throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
        }
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