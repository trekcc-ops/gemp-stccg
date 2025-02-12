package com.gempukku.stccg.async.handler.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.SubscriptionExpiredException;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.LongPollingResource;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.chat.ChatCommunicationChannel;
import com.gempukku.stccg.chat.ChatRoomMediator;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;

public class PostChatRequestHandler implements UriRequestHandlerNew {

    private final String _roomName;

    private static final Logger LOGGER = LogManager.getLogger(PostChatRequestHandler.class);

    PostChatRequestHandler(
            @JsonProperty(value = "roomName", required = true)
            String roomName
    ) {
        _roomName = roomName;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws HttpProcessingException {

        User resourceOwner = request.user();
        ChatRoomMediator chatRoom = serverObjects.getChatServer().getChatRoom(_roomName);
        if (chatRoom == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        try {
            ChatCommunicationChannel listener = chatRoom.getChatRoomListener(resourceOwner);
            LongPollingResource pollingResource =
                    new ChatUpdateLongPollingResource(chatRoom, resourceOwner, responseWriter);
            serverObjects.getLongPollingSystem().processLongPollingResource(pollingResource, listener);
        } catch (SubscriptionExpiredException exp) {
            logHttpError(LOGGER, HttpURLConnection.HTTP_GONE, request.uri(), exp);
            throw new HttpProcessingException(HttpURLConnection.HTTP_GONE); // 410
        }
    }


    private class ChatUpdateLongPollingResource implements LongPollingResource {
        private final ChatRoomMediator chatRoom;
        private final ResponseWriter responseWriter;
        private boolean processed;
        private final User user;

        private ChatUpdateLongPollingResource(ChatRoomMediator chatRoom, User user,
                                              ResponseWriter responseWriter) {
            this.chatRoom = chatRoom;
            this.user = user;
            this.responseWriter = responseWriter;
        }


        @Override
        public final synchronized boolean wasProcessed() {
            return processed;
        }

        @Override
        public final synchronized void processIfNotProcessed() {
            if (!processed) {
                try {
                    ChatCommunicationChannel channel = chatRoom.getChatRoomListener(user);
                    responseWriter.writeJsonResponse(new ObjectMapper().writeValueAsString(channel));
                } catch (SubscriptionExpiredException exp) {
                    logAndWriteError(HttpURLConnection.HTTP_GONE, exp); // 410
                } catch (Exception exp) {
                    logAndWriteError(HttpURLConnection.HTTP_INTERNAL_ERROR, exp); // 500
                }
                processed = true;
            }
        }

        private void logAndWriteError(int connectionResult, Exception exp) {
            logHttpError(LOGGER, connectionResult, "chat poller", exp);
            responseWriter.writeError(connectionResult);
        }
    }

}