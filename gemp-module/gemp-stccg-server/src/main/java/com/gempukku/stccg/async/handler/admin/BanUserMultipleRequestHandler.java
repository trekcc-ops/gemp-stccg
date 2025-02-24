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
import java.util.List;

public class BanUserMultipleRequestHandler extends AdminRequestHandlerNew implements UriRequestHandler {

    // TODO - This doesn't work

    private final List<String> _userNames;

    BanUserMultipleRequestHandler(
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @JsonProperty(value = "userNames", required = true)
            List<String> userNames
    ) {
        _userNames = userNames;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {

        validateAdmin(request);
        AdminService adminService = serverObjects.getAdminService();

        for (String login : _userNames) {
            if (!adminService.banUser(login))
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
        responseWriter.writeJsonOkResponse();
    }
}