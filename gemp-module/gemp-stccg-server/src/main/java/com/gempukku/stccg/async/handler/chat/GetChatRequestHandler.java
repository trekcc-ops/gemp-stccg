package com.gempukku.stccg.async.handler.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.chat.ChatCommunicationChannel;
import com.gempukku.stccg.chat.ChatRoomMediator;
import com.gempukku.stccg.chat.PrivateInformationException;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;

public class GetChatRequestHandler implements UriRequestHandlerNew {

    private static final Logger LOGGER = LogManager.getLogger(GetChatRequestHandler.class);
    private final String _roomName;

    GetChatRequestHandler(
        @JsonProperty("roomName")
        String roomName
    ) {
        _roomName = roomName;
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp,
                                    ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = getResourceOwnerSafely(request, serverObjects);
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