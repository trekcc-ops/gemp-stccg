package com.gempukku.stccg.async.handler.admin;

import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.AdminRequestHandler;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.league.LeagueData;
import com.gempukku.stccg.league.LeagueSeriesData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public abstract class AdminRequestHandlerNew {

    protected void validateAdmin(GempHttpRequest request) throws HttpProcessingException {
        User user = request.user();
        if (!user.isAdmin())
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
    }

    protected void validateLeagueAdmin(GempHttpRequest request) throws HttpProcessingException {
        User user = request.user();
        if (!user.isLeagueAdmin())
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
    }

}