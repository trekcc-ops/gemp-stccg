package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.service.AdminService;

import java.net.HttpURLConnection;

public class BanUserTemporaryRequestHandler implements UriRequestHandler, AdminRequestHandler {

    private final String _userToBan;
    private final int _duration;

    BanUserTemporaryRequestHandler(
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @JsonProperty(value = "userName", required = true)
            String userName,
            @JsonProperty(value = "duration", required = true)
            int duration
    ) {
        _userToBan = userName;
        _duration = duration;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        validateAdmin(request);
        AdminService adminService = serverObjects.getAdminService();
        if (!adminService.banUserTemp(_userToBan, _duration))
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        responseWriter.writeJsonOkResponse();
    }
}