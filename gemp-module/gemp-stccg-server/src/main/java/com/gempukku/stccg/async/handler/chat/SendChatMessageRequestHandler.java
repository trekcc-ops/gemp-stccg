package com.gempukku.stccg.async.handler.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.HTMLUtils;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.chat.ChatCommandErrorException;
import com.gempukku.stccg.chat.ChatRoomMediator;
import com.gempukku.stccg.chat.PrivateInformationException;
import com.gempukku.stccg.database.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.util.regex.Pattern;

public class SendChatMessageRequestHandler implements UriRequestHandlerNew {

    private final String _roomName;
    private final String _message;
    private final Pattern QuoteExtender =
            Pattern.compile("^([ \t]*>[ \t]*.+)(?=\n[ \t]*[^>])", Pattern.MULTILINE);

    private static final Logger LOGGER = LogManager.getLogger(SendChatMessageRequestHandler.class);

    SendChatMessageRequestHandler(
            @JsonProperty("roomName")
            String roomName,
            @JsonProperty("message")
            String message
    ) {
        _roomName = roomName;
        _message = message;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws HttpProcessingException {

        User resourceOwner = request.user();
        ChatRoomMediator chatRoom = serverObjects.getChatServer().getChatRoom(_roomName);
        if (chatRoom == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

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
                    newMsg = HTMLUtils.parseChatMessage(newMsg);
                chatRoom.sendChatMessage(resourceOwner, newMsg);
                responseWriter.writeXmlOkResponse();
            }
        } catch (PrivateInformationException exp) {
            logHttpError(LOGGER, HttpURLConnection.HTTP_FORBIDDEN, request.uri(), exp);
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
        } catch (ChatCommandErrorException exp) {
            logHttpError(LOGGER, HttpURLConnection.HTTP_BAD_REQUEST, request.uri(), exp);
            throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
        }
    }


}