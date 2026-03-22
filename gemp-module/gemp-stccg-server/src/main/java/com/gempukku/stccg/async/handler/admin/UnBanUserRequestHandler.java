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

public class UnBanUserRequestHandler implements UriRequestHandler, AdminRequestHandler {

    private final String _userToBan;
    private final AdminService _adminService;

    UnBanUserRequestHandler(
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @JsonProperty(value = "userName", required = true)
            String userName,
            @JacksonInject AdminService adminService
    ) {
        _userToBan = userName;
        _adminService = adminService;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        validateAdmin(request);
        if (!_adminService.unBanUser(_userToBan))
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        responseWriter.writeJsonOkResponse();
    }
}