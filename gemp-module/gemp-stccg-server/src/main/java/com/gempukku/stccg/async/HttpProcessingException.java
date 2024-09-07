package com.gempukku.stccg.async;

public class HttpProcessingException extends Exception {
    private final int _status;

    public HttpProcessingException(int status) {
        _status = status;
    }

    public int getStatus() {
        return _status;
    }
}
