package com.gempukku.stccg.async.handler.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.database.PlayerDAO;
import com.gempukku.stccg.database.User;

public class SetTesterFlagRequestHandler implements UriRequestHandlerNew {

    private final boolean _testerFlag;

    public SetTesterFlagRequestHandler(
            @JsonProperty(value = "testerFlag", required = true)
            boolean testerFlag
    ) {
        _testerFlag = testerFlag;
    }

    @Override
    public final void handleRequest(GempHttpRequest gempRequest, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {

        User player = gempRequest.user();
        PlayerDAO playerDAO = serverObjects.getPlayerDAO();

        if (_testerFlag)
            playerDAO.addPlayerFlag(player.getName(), User.Type.PLAY_TESTER);
        else playerDAO.removePlayerFlag(player.getName(), User.Type.PLAY_TESTER);

        responseWriter.writeJsonOkResponse();
    }

}