package com.gempukku.stccg.async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.handler.ResponseWriter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class ResponseSender implements ResponseWriter {
    private static final Logger LOGGER = LogManager.getLogger(ResponseSender.class);
    private final Map<String, byte[]> _fileCache = Collections.synchronizedMap(new HashMap<>());
    private final static long SIX_MONTHS = 1000L * 60L * 60L * 24L * 30L * 6L;

    private final ChannelHandlerContext ctx;
    private final HttpRequest request;

    ResponseSender(ChannelHandlerContext ctx, HttpRequest request) {
        this.ctx = ctx;
        this.request = request;
    }

    @Override
    public final void writeError(int status) {
        sendResponse(HttpResponseStatus.valueOf(status), new byte[0], new DefaultHttpHeaders(), ctx, request);
    }

    @Override
    public final void writeError(int status, Map<String, String> headers) {
        sendResponse(HttpResponseStatus.valueOf(status), new byte[0], convertToHeaders(headers), ctx, request);
    }

    @Override
    public final void writeXmlResponseWithNoHeaders(Document document) {
        try {
            String contentType;
            String response1;
            if (document != null) {
                Source domSource = new DOMSource(document);
                StringWriter writer = new StringWriter();
                Result result = new StreamResult(writer);
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.transform(domSource, result);

                response1 = writer.toString();
                contentType = "application/xml; charset=UTF-8";
            } else {
                response1 = "OK";
                contentType = "text/plain";
            }
            HttpHeaders headers1 = convertToHeaders(null);
            headers1.set(CONTENT_TYPE, contentType);
            sendResponse(HttpResponseStatus.OK, response1.getBytes(CharsetUtil.UTF_8), headers1, ctx, request);
        } catch (Exception exp) {
            LOGGER.error("Error response for {}", request.uri(), exp);
            sendResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, new byte[0], null, ctx, request);
        }
    }


    @Override
    public final void writeXmlResponseWithHeaders(Document document, Map<? extends CharSequence, String> addHeaders) {
        try {
            String contentType;
            String response1;
            if (document != null) {
                Source domSource = new DOMSource(document);
                StringWriter writer = new StringWriter();
                Result result = new StreamResult(writer);
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.transform(domSource, result);

                response1 = writer.toString();
                contentType = "application/xml; charset=UTF-8";
            } else {
                response1 = "OK";
                contentType = "text/plain";
            }
            HttpHeaders headers1 = convertToHeaders(addHeaders);
            headers1.set(CONTENT_TYPE, contentType);
            sendResponse(HttpResponseStatus.OK, response1.getBytes(CharsetUtil.UTF_8), headers1, ctx, request);
        } catch (Exception exp) {
            LOGGER.error("Error response for {}", request.uri(), exp);
            sendResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, new byte[0], null, ctx, request);
        }
    }

    @Override
    public void writeXmlMarshalExceptionResponse(Exception e) throws ParserConfigurationException {
        writeXmlMarshalExceptionResponse(e.getMessage());
    }

    @Override
    public void writeXmlMarshalExceptionResponse(String errorMessage) throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element error = doc.createElement("error");
        error.setAttribute("message", errorMessage);
        doc.appendChild(error);
        writeXmlResponseWithNoHeaders(doc);
    }


    @Override
    public final void writeEmptyXmlResponseWithHeaders(Map<? extends CharSequence, String> addHeaders) {
        try {
            String response1 = "OK";
            HttpHeaders headers1 = convertToHeaders(addHeaders);
            headers1.set(CONTENT_TYPE, "text/plain");
            sendResponse(HttpResponseStatus.OK, response1.getBytes(CharsetUtil.UTF_8), headers1, ctx, request);
        } catch (Exception exp) {
            LOGGER.error("Error response for {}", request.uri(), exp);
            sendResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, new byte[0], null, ctx, request);
        }
    }

    @Override
    public final void writeXmlOkResponse() {
        try {
            String response1 = "OK";
            HttpHeaders headers1 = new DefaultHttpHeaders();
            headers1.set(CONTENT_TYPE, "text/plain");
            sendResponse(HttpResponseStatus.OK, response1.getBytes(CharsetUtil.UTF_8), headers1, ctx, request);
        } catch (Exception exp) {
            LOGGER.error("Error response for {}", request.uri(), exp);
            sendResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, new byte[0], null, ctx, request);
        }
    }



    @Override
    public final void writeHtmlResponse(String html) {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set(CONTENT_TYPE, "text/html; charset=UTF-8");
        if (html == null) html = "";
        sendResponse(HttpResponseStatus.OK, html.getBytes(CharsetUtil.UTF_8), headers, ctx, request);
    }

    @Override
    public void writeJsonOkResponse() throws JsonProcessingException {
        Map<String, String> result = new HashMap<>();
        result.put("response", "OK");
        writeJsonResponse(new ObjectMapper().writeValueAsString(result));
    }


    @Override
    public final void writeJsonResponse(String json) {
        writeJsonResponseWithHeaders(json, new DefaultHttpHeaders());
    }


    @Override
    public final void writeJsonResponseWithHeaders(String json, HttpHeaders headers) {
        headers.set(CONTENT_TYPE, "application/json; charset=UTF-8");

        if (json == null) json = "{}";

        if(!json.startsWith("{") && !json.startsWith("["))
            json = "{ \"response\": " + json + " }";

        sendResponse(HttpResponseStatus.OK, json.getBytes(CharsetUtil.UTF_8), headers, ctx, request);
    }


    @Override
    public final void writeByteResponse(byte[] bytes, Map<? extends CharSequence, String> headers) {
        HttpHeaders headers1 = convertToHeaders(headers);
        sendResponse(HttpResponseStatus.OK, bytes, headers1, ctx, request);
    }

    @Override
    public final void writeFile(File file, Map<String, String> headers) {
        try {
            String canonicalPath = file.getCanonicalPath();
            byte[] fileBytes = _fileCache.get(canonicalPath);
            if (fileBytes == null) {
                if (!file.exists() || !file.isFile()) {
                    HttpResponseStatus status =
                            HttpResponseStatus.valueOf(HttpURLConnection.HTTP_NOT_FOUND); // 404
                    sendResponse(status, new byte[0], convertToHeaders(null), ctx, request);
                    return;
                }

                FileInputStream fis = new FileInputStream(file);
                try {
                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                    IOUtils.copyLarge(fis, byteStream);
                    fileBytes = byteStream.toByteArray();
                    _fileCache.put(canonicalPath, fileBytes);
                } finally {
                    IOUtils.closeQuietly(fis);
                }
            }
            HttpHeaders headers1 = convertToHeaders(getHeadersForFile(headers, file));
            sendResponse(HttpResponseStatus.OK, fileBytes, headers1, ctx, request);
        } catch (IOException exp) {
            byte[] content = new byte[0];
            // Build the response object.
            LOGGER.error("Error response for {}", request.uri(), exp);
            HttpResponseStatus status = HttpResponseStatus.valueOf(HttpURLConnection.HTTP_INTERNAL_ERROR); // 500
            sendResponse(status, content, convertToHeaders(null), ctx, request);
        }
    }

    private static Map<String, String> getHeadersForFile(Map<String, String> headers, File file) {
        Map<String, String> fileHeaders = new HashMap<>(headers);

        boolean cache = false;

        String fileName = file.getName();
        String contentType;
        if (fileName.endsWith(".html")) {
            contentType = "text/html; charset=UTF-8";
        } else if (fileName.endsWith(".js")) {
            contentType = "application/javascript; charset=UTF-8";
        } else if (fileName.endsWith(".css")) {
            contentType = "text/css; charset=UTF-8";
        } else if (fileName.endsWith(".jpg")) {
            cache = true;
            contentType = "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            cache = true;
            contentType = "image/png";
        } else if (fileName.endsWith(".gif")) {
            cache = true;
            contentType = "image/gif";
        }
        else if (fileName.endsWith(".svg")) {
            cache = true;
            contentType = "image/svg+xml";
        }
        else if (fileName.endsWith(".wav")) {
            cache = true;
            contentType = "audio/wav";
        }
        else if (fileName.endsWith(".mp3")) {
            cache = true;
            contentType = "audio/mpeg";
        } else {
            contentType = "application/octet-stream";
        }

        if (cache) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
            long sixMonthsFromNow = System.currentTimeMillis() + SIX_MONTHS;
            fileHeaders.put(EXPIRES.toString(), dateFormat.format(new Date(sixMonthsFromNow)));
        }

        fileHeaders.put(CONTENT_TYPE.toString(), contentType);
        return fileHeaders;
    }

    private static HttpHeaders convertToHeaders(Map<? extends CharSequence, String> headersMap) {
        HttpHeaders headers = new DefaultHttpHeaders();
        if (headersMap != null) {
            for (Map.Entry<? extends CharSequence, String> headerEntry : headersMap.entrySet()) {
                headers.set(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        return headers;
    }

    private void sendResponse(HttpResponseStatus status, byte[] content, HttpHeaders headers,
                              ChannelOutboundInvoker context, HttpMessage message) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.wrappedBuffer(content), headers, EmptyHttpHeaders.INSTANCE);
        boolean keepAlive = HttpUtil.isKeepAlive(message);

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        context.write(response);
        context.flush();

        if (!keepAlive) {
            // If keep-alive is off, close the connection once the content is fully written.
            context.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }

    }

}