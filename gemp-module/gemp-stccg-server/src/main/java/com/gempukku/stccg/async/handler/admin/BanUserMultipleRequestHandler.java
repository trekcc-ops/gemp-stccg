package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.service.AdminService;

import java.net.HttpURLConnection;
import java.util.List;

public class BanUserMultipleRequestHandler implements UriRequestHandler, AdminRequestHandler {

    // TODO - This doesn't work

    private final List<String> _userNames;
    private final AdminService _adminService;

    BanUserMultipleRequestHandler(
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @JsonProperty(value = "userNames", required = true)
            List<String> userNames,
            @JacksonInject AdminService adminService
    ) {
        _userNames = userNames;
        _adminService = adminService;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {

        validateAdmin(request);

        for (String login : _userNames) {
            if (!_adminService.banUser(login))
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
        responseWriter.writeJsonOkResponse();
    }
}