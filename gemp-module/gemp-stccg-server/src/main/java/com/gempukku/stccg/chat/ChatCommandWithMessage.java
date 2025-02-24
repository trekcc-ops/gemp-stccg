package com.gempukku.stccg.chat;

abstract class ChatCommandWithMessage implements ChatCommandCallback {
    final ChatRoomMediator _mediator;

    ChatCommandWithMessage(ChatRoomMediator mediator) {
        _mediator = mediator;
    }

    void sendChatMessage(String from, String to, String message) {
        _mediator.sendToUser(from, to, message);
    }

}