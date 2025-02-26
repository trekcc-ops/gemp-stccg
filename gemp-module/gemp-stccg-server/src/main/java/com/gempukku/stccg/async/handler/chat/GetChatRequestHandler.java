package com.gempukku.stccg.async.handler.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.chat.ChatCommunicationChannel;
import com.gempukku.stccg.chat.ChatRoomMediator;
import com.gempukku.stccg.chat.PrivateInformationException;
import com.gempukku.stccg.database.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;

public class GetChatRequestHandler implements UriRequestHandler {

    private static final Logger LOGGER = LogManager.getLogger(GetChatRequestHandler.class);
    private final String _roomName;

    GetChatRequestHandler(
        @JsonProperty("roomName")
        String roomName
    ) {
        _roomName = roomName;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        ChatRoomMediator chatRoom = serverObjects.getChatServer().getChatRoom(_roomName);
        if (chatRoom == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        try {
            chatRoom.joinUser(resourceOwner);
            ChatCommunicationChannel listener = chatRoom.getChatRoomListener(resourceOwner);
            String jsonString = new ObjectMapper().writeValueAsString(listener);
            responseWriter.writeJsonResponse(jsonString);
        } catch (PrivateInformationException exp) {
            logHttpError(LOGGER, HttpURLConnection.HTTP_FORBIDDEN, request.uri(), exp);
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
        }
    }

}