package com.gempukku.stccg.async.handler;

import io.netty.handler.codec.http.HttpHeaders;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.Map;

public interface ResponseWriter {
    void writeError(int status);
    void writeError(int status, Map<String, String> headers);

    void writeFile(File file, Map<String, String> headers);

    void writeEmptyXmlResponseWithHeaders(Map<? extends CharSequence, String> addHeaders);

    void writeXmlOkResponse();

    void writeHtmlResponse(String html);
    void writeJsonResponse(String json);

    void writeJsonResponseWithHeaders(String json, Map<String, String> headers);

    void writeJsonResponseWithHeaders(String json, HttpHeaders headers);

    void writeByteResponse(byte[] bytes, Map<? extends CharSequence, String> headers);

    void writeXmlResponseWithNoHeaders(Document document);

    void writeXmlResponseWithHeaders(Document document, Map<? extends CharSequence, String> addHeaders);

    void writeXmlMarshalExceptionResponse(Exception e) throws ParserConfigurationException;
    void writeXmlMarshalExceptionResponse(String errorMessage) throws ParserConfigurationException;

    void writeHtmlOkResponse();
}