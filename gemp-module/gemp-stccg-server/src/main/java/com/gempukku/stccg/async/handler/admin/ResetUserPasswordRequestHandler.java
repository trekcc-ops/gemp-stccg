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
import java.sql.SQLException;

public class ResetUserPasswordRequestHandler implements UriRequestHandler, AdminRequestHandler {

    private final String _userName;

    ResetUserPasswordRequestHandler(
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @JsonProperty(value = "userName", required = true)
            String userName
    ) {
        _userName = userName;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        validateAdmin(request);
        AdminService adminService = serverObjects.getAdminService();
        try {
            adminService.resetUserPassword(_userName);
        } catch(SQLException exp) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
        responseWriter.writeJsonOkResponse();
    }
}