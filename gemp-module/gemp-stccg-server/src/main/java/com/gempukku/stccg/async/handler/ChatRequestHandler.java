package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.SubscriptionExpiredException;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.LongPollingResource;
import com.gempukku.stccg.async.LongPollingSystem;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.chat.ChatCommandErrorException;
import com.gempukku.stccg.chat.ChatRoomMediator;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.chat.PrivateInformationException;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class ChatRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final ChatServer _chatServer;
    private final LongPollingSystem longPollingSystem;

    private static final Logger LOGGER = LogManager.getLogger(ChatRequestHandler.class);

    public ChatRequestHandler(ServerObjects objects) {
        super(objects);
        _chatServer = objects.getChatServer();
        this.longPollingSystem = objects.getLongPollingSystem();
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp)
            throws Exception {
        if (uri.startsWith("/")) {
            String roomName = URLDecoder.decode(uri.substring(1), StandardCharsets.UTF_8);
            if (request.method() == HttpMethod.GET) {
                getMessages(request, roomName, responseWriter);
            } else if (request.method() == HttpMethod.POST) {
                postMessages(request, roomName, responseWriter);
            } else {
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
            }
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private final Pattern QuoteExtender =
            Pattern.compile("^([ \t]*>[ \t]*.+)(?=\n[ \t]*[^>])", Pattern.MULTILINE);

    private void postMessages(HttpRequest request, String room, ResponseWriter responseWriter) throws Exception {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
            String message = getFormParameterSafely(postDecoder, FormParameter.message);

            User resourceOwner = getResourceOwnerSafely(request, participantId);

            ChatRoomMediator chatRoom = _chatServer.getChatRoom(room);
            if (chatRoom == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

            try {
                if (message != null && !message.trim().isEmpty()) {
                    String newMsg;
                    newMsg = message.trim().replaceAll("\n\n\n+", "\n\n\n");
                    newMsg = QuoteExtender.matcher(newMsg).replaceAll("$1\n");
                    //Escaping underscores so that URLs with lots of underscores (i.e. wiki links) aren't mangled
                    // Besides, who uses _this_ instead of *this*?
                    newMsg = newMsg.replace("_", "\\_");

                    //Need to preserve any commands being made
                    if(!newMsg.startsWith("/"))
                        newMsg = HTMLUtils.parseChatMessage(newMsg);
                    chatRoom.sendMessage(resourceOwner, newMsg);
                    responseWriter.writeXmlOkResponse();
                } else {
                    longPollingSystem.processLongPollingResource(
                            new ChatUpdateLongPollingResource(chatRoom, room, resourceOwner, responseWriter),
                            chatRoom.getChatRoomListener(resourceOwner)
                    );
                }
            } catch (SubscriptionExpiredException exp) {
                logHttpError(LOGGER, HttpURLConnection.HTTP_GONE, request.uri(), exp);
                throw new HttpProcessingException(HttpURLConnection.HTTP_GONE); // 410
            } catch (PrivateInformationException exp) {
                logHttpError(LOGGER, HttpURLConnection.HTTP_FORBIDDEN, request.uri(), exp);
                throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
            } catch (ChatCommandErrorException exp) {
                logHttpError(LOGGER, HttpURLConnection.HTTP_BAD_REQUEST, request.uri(), exp);
                throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
            }
        } finally {
            postDecoder.destroy();
        }
    }

    private class ChatUpdateLongPollingResource implements LongPollingResource {
        private final ChatRoomMediator chatRoom;
        private final String room;
        private final ResponseWriter responseWriter;
        private boolean processed;
        private final User user;

        private ChatUpdateLongPollingResource(ChatRoomMediator chatRoom, String room, User user,
                                              ResponseWriter responseWriter) {
            this.chatRoom = chatRoom;
            this.room = room;
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
                    responseWriter.writeJsonResponse(serializeChatRoom(chatRoom, user, room));
                } catch (SubscriptionExpiredException exp) {
                    logAndWriteError(HttpURLConnection.HTTP_GONE, "chat poller", exp); // 410
                } catch (Exception exp) {
                    logAndWriteError(HttpURLConnection.HTTP_INTERNAL_ERROR, "chat poller", exp); // 500
                }
                processed = true;
            }
        }

        private void logAndWriteError(int connectionResult, String uri, Exception exp) {
            logHttpError(LOGGER, connectionResult, uri, exp);
            responseWriter.writeError(connectionResult);
        }
    }

    private void getMessages(HttpRequest request, String room, ResponseWriter responseWriter) throws Exception {
        User resourceOwner = getUserIdFromCookiesOrUri(request);
        ChatRoomMediator chatRoom = _chatServer.getChatRoom(room);
        if (chatRoom == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        try {
            final boolean admin = resourceOwner.hasType(User.Type.ADMIN);
            chatRoom.joinUser(resourceOwner.getName(), admin);
            responseWriter.writeJsonResponse(serializeChatRoom(chatRoom, resourceOwner, room));
        } catch (PrivateInformationException exp) {
            logHttpError(LOGGER, HttpURLConnection.HTTP_FORBIDDEN, request.uri(), exp);
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
        }
    }

    private String serializeChatRoom(ChatRoomMediator chatRoom, User user, String roomName)
            throws SubscriptionExpiredException, JsonProcessingException {
        Map<Object, Object> chatMap = new HashMap<>();
        chatMap.put("roomName", roomName);
        chatMap.put("messages", chatRoom.getChatRoomListener(user).consumeMessages());

        Collection<String> users = new TreeSet<>(new CaseInsensitiveStringComparator());
        boolean userIsAdmin = user.hasType(User.Type.ADMIN);
        for (String userInRoom : chatRoom.getUsersInRoom(userIsAdmin))
            users.add(formatPlayerNameForChatList(userInRoom));
        chatMap.put("users", users);

        return _jsonMapper.writeValueAsString(chatMap);
    }


    private static class CaseInsensitiveStringComparator implements Comparator<String> {
        @Override
        public final int compare(String o1, String o2) {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
        }
    }

    private String formatPlayerNameForChatList(String userInRoom) {
        final User player = _playerDao.getPlayer(userInRoom);
        if (player != null) {
            if (player.hasType(User.Type.ADMIN))
                return "* "+userInRoom;
            else if (player.hasType(User.Type.LEAGUE_ADMIN))
                return "+ "+userInRoom;
        }
        return userInRoom;
    }
}