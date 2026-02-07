package com.gempukku.stccg.game;

import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.hall.GameCreationListener;

public class GameChatCreationListener implements GameCreationListener {

    private final ChatServer _chatServer;

    public GameChatCreationListener(ChatServer chatServer) {
        _chatServer = chatServer;
    }

    @Override
    public void process(CardGameMediator mediator) {
        _chatServer.addChatRoom(mediator.getChat());
    }
}