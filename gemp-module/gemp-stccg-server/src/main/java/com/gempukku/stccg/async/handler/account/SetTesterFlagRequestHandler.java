package com.gempukku.stccg.async.handler.account;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.service.AdminService;

public class SetTesterFlagRequestHandler implements UriRequestHandler {

    private final boolean _testerFlag;
    private final AdminService _adminService;

    public SetTesterFlagRequestHandler(
            @JsonProperty(value = "testerFlag", required = true)
            boolean testerFlag,
            @JacksonInject AdminService adminService
            ) {
        _testerFlag = testerFlag;
        _adminService = adminService;
    }

    @Override
    public final void handleRequest(GempHttpRequest gempRequest, ResponseWriter responseWriter)
            throws Exception {

        String userName = gempRequest.userName();

        if (_testerFlag) {
            _adminService.addPlayerFlag(userName, User.Type.PLAY_TESTER);
        } else {
            _adminService.removePlayerFlag(userName, User.Type.PLAY_TESTER);
        }
        responseWriter.writeJsonOkResponse();
    }

}