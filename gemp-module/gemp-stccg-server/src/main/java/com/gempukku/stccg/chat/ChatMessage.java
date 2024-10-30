package com.gempukku.stccg.chat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ChatMessage {
    private final String _from;
    private final String _message;
    private final boolean _fromAdmin;
    private final String _messageDate;

    ChatMessage(String from, String message, boolean fromAdmin) {
        long currentTime = System.currentTimeMillis();
        _messageDate = String.valueOf(currentTime);
        _from = from;
        _message = message;
        _fromAdmin = fromAdmin;
    }

    public final String getFrom() {
        return _from;
    }

    final boolean isFromAdmin() {
        return _fromAdmin;
    }

    public Element serializeForDocument(Document doc, String elemName) {
        Element messageElem = doc.createElement(elemName);
        messageElem.setAttribute("from", _from);
        messageElem.setAttribute("date", _messageDate);
        messageElem.appendChild(doc.createTextNode(_message));
        return messageElem;
    }
}