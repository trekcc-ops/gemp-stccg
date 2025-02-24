package com.gempukku.stccg.chat;

public class IncognitoCommandCallback extends ChatCommandWithMessage {
    private final boolean _isSettingIncognito;

    IncognitoCommandCallback(ChatRoomMediator mediator, boolean isSettingIncognito) {
        super(mediator);
        _isSettingIncognito = isSettingIncognito;
    }

    @Override
    public void commandReceived(String from, String parameters, boolean admin) {
        _mediator.setIncognito(from, _isSettingIncognito);
        String message = _isSettingIncognito ?
                "You are now incognito (do not appear in user list)" : "You are no longer incognito";
        sendChatMessage(ChatStrings.SYSTEM_USER_ID, from, message);
    }
}