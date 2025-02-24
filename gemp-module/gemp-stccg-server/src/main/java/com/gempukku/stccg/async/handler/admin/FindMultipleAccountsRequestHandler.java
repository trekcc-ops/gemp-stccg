package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.PlayerDAO;
import com.gempukku.stccg.database.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.HttpURLConnection;
import java.util.List;

public class FindMultipleAccountsRequestHandler implements UriRequestHandler, AdminRequestHandler {

    private final String _userName;

    FindMultipleAccountsRequestHandler(
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @JsonProperty(value = "userName", required = true)
            String userName
    ) {
        _userName = userName;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {

        validateAdmin(request);
        PlayerDAO playerDAO = serverObjects.getPlayerDAO();

        List<User> similarPlayers = playerDAO.findSimilarAccounts(_userName);
        if (similarPlayers.isEmpty())
            throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400

        Document doc = createNewDoc();
        Element players = doc.createElement("players");
        for (User similarPlayer : similarPlayers) {
            Element playerElem = doc.createElement("player");
            playerElem.setAttribute("id", String.valueOf(similarPlayer.getId()));
            playerElem.setAttribute("name", similarPlayer.getName());
            playerElem.setAttribute("password", similarPlayer.getPassword());
            playerElem.setAttribute("status", similarPlayer.getStatus());
            playerElem.setAttribute("createIp", similarPlayer.getCreateIp());
            playerElem.setAttribute("loginIp", similarPlayer.getLastIp());
            players.appendChild(playerElem);
        }
        doc.appendChild(players);
        responseWriter.writeXmlResponseWithNoHeaders(doc);
    }
}