package com.gempukku.stccg.async.handler.chat;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.SubscriptionExpiredException;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.LongPollingResource;
import com.gempukku.stccg.async.LongPollingSystem;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.chat.ChatCommunicationChannel;
import com.gempukku.stccg.chat.ChatRoomMediator;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.database.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;

public class PostChatRequestHandler implements UriRequestHandler {

    private final String _roomName;
    private final ChatRoomMediator _chatRoom;
    private final LongPollingSystem _longPollingSystem;

    private static final Logger LOGGER = LogManager.getLogger(PostChatRequestHandler.class);

    PostChatRequestHandler(
            @JsonProperty(value = "roomName", required = true)
            String roomName,
            @JacksonInject ChatServer chatServer,
            @JacksonInject LongPollingSystem longPollingSystem) throws HttpProcessingException {
        _roomName = roomName;
        _chatRoom = chatServer.getChatRoom(roomName);
        if (_chatRoom == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        _longPollingSystem = longPollingSystem;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException {

        User resourceOwner = request.user();

        try {
            ChatCommunicationChannel listener = _chatRoom.getChatRoomListener(resourceOwner);
            LongPollingResource pollingResource =
                    new ChatUpdateLongPollingResource(_chatRoom, resourceOwner, responseWriter, listener);
            pollingResource.processInSystem(_longPollingSystem);
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
        private final ChatCommunicationChannel listener;

        private ChatUpdateLongPollingResource(ChatRoomMediator chatRoom, User user,
                                              ResponseWriter responseWriter,
                                              ChatCommunicationChannel listener) {
            this.chatRoom = chatRoom;
            this.user = user;
            this.responseWriter = responseWriter;
            this.listener = listener;
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

        @Override
        public void processInSystem(LongPollingSystem system) {
            system.processLongPollingResource(this, this.listener);
        }
    }

}