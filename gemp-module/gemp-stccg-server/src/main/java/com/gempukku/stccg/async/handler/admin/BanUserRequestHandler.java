package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.service.AdminService;

import java.net.HttpURLConnection;

public class BanUserRequestHandler extends AdminRequestHandlerNew implements UriRequestHandlerNew {

    private final String _userToBan;

    BanUserRequestHandler(
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @JsonProperty(value = "userName", required = true)
            String userName
    ) {
        _userToBan = userName;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        validateAdmin(request);
        AdminService adminService = serverObjects.getAdminService();
        if (!adminService.banUser(_userToBan))
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        responseWriter.writeJsonOkResponse();
    }
}