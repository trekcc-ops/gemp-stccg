package com.gempukku.stccg.chat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Date;

public class ChatMessage {
    private final Date _when;
    private final String _from;
    private final String _message;
    private final boolean fromAdmin;

    public ChatMessage(Date when, String from, String message, boolean fromAdmin) {
        _when = when;
        _from = from;
        _message = message;
        this.fromAdmin = fromAdmin;
    }

    public final String getFrom() {
        return _from;
    }

    public final boolean isFromAdmin() {
        return fromAdmin;
    }

    public Element serializeForDocument(Document doc, String elemName) {
        Element messageElem = doc.createElement(elemName);
        messageElem.setAttribute("from", _from);
        messageElem.setAttribute("date", String.valueOf(_when.getTime()));
        messageElem.appendChild(doc.createTextNode(_message));
        return messageElem;
    }
}