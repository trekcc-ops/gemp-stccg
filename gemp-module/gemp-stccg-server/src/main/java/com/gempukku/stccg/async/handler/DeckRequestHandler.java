package com.gempukku.stccg.async.handler;

import com.alibaba.fastjson.JSON;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ResponseWriter;
import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.common.JSONDefs;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.db.DeckDAO;
import com.gempukku.stccg.draft.SoloDraftDefinitions;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.GameServer;
import com.gempukku.stccg.game.SortAndFilterCards;
import com.gempukku.stccg.game.User;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.SealedLeagueDefinition;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class DeckRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final DeckDAO _deckDao;
    private final SortAndFilterCards _sortAndFilterCards;
    private final FormatLibrary _formatLibrary;
    private final SoloDraftDefinitions _draftLibrary;
    private final GameServer _gameServer;

    public DeckRequestHandler(Map<Type, Object> context) {
        super(context);
        _deckDao = extractObject(context, DeckDAO.class);
        _sortAndFilterCards = new SortAndFilterCards();
        _formatLibrary = extractObject(context, FormatLibrary.class);
        _gameServer = extractObject(context, GameServer.class);
        _draftLibrary = extractObject(context, SoloDraftDefinitions.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("/list") && request.method() == HttpMethod.GET) {
            listDecks(request, responseWriter);
        } else if (uri.equals("/libraryList") && request.method() == HttpMethod.GET) {
            listLibraryDecks(responseWriter);
        } else if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            getDeck(request, responseWriter);
        } else if (uri.isEmpty() && request.method() == HttpMethod.POST) {
            saveDeck(request, responseWriter);
        } else if (uri.equals("/library") && request.method() == HttpMethod.GET) {
            getLibraryDeck(request, responseWriter);
        } else if (uri.equals("/share") && request.method() == HttpMethod.GET) {
            shareDeck(request, responseWriter);
        } else if (uri.equals("/html") && request.method() == HttpMethod.GET) {
            getDeckInHtml(request, responseWriter);
        } else if (uri.equals("/libraryHtml") && request.method() == HttpMethod.GET) {
            getLibraryDeckInHtml(request, responseWriter);
        } else if (uri.equals("/rename") && request.method() == HttpMethod.POST) {
            renameDeck(request, responseWriter);
        } else if (uri.equals("/delete") && request.method() == HttpMethod.POST) {
            deleteDeck(request, responseWriter);
        } else if (uri.equals("/stats") && request.method() == HttpMethod.POST) {
            getDeckStats(request, responseWriter);
        } else if (uri.equals("/formats") && request.method() == HttpMethod.POST) {
            getAllFormats(request, responseWriter);
        } else if (uri.equals("/sets") && request.method() == HttpMethod.POST) {
            getSets(request, responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }



    private void getAllFormats(HttpRequest request, ResponseWriter responseWriter) throws IOException {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String includeEventsStr = getFormParameterSafely(postDecoder, "includeEvents");
            String json;

            if(includeEventsStr != null && includeEventsStr.equalsIgnoreCase("true"))
            {
                JSONDefs.FullFormatReadout data = new JSONDefs.FullFormatReadout();
                data.Formats = _formatLibrary.getAllFormats().values().stream()
                        .map(GameFormat::Serialize)
                        .collect(Collectors.toMap(x-> x.code, x-> x));
                data.SealedTemplates = _formatLibrary.GetAllSealedTemplates().values().stream()
                        .map(SealedLeagueDefinition::Serialize)
                        .collect(Collectors.toMap(x-> x.Name, x-> x));
                data.DraftTemplates = _draftLibrary.getAllSoloDrafts().values().stream()
                        .map(soloDraft -> new JSONDefs.ItemStub(soloDraft.getCode(), soloDraft.getFormat()))
                        .collect(Collectors.toMap(x-> x.code, x-> x));

                json = JSON.toJSONString(data);
            }
            else {
                Map<String, GameFormat> formats = _formatLibrary.getHallFormats();

                Object[] output = formats.entrySet().stream()
                        .map(x -> new JSONDefs.ItemStub(x.getKey(), x.getValue().getName()))
                        .toArray();

                json = JSON.toJSONString(output);
            }

            responseWriter.writeJsonResponse(json);
        } finally {
            postDecoder.destroy();
        }
    }

    private void getSets(HttpRequest request, ResponseWriter responseWriter) throws IOException {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String format = getFormParameterSafely(postDecoder, "format");
            GameFormat currentFormat = _formatLibrary.getFormat(format);

            String json;
            Map<String, String> sets = currentFormat.getValidSets();
            Object[] output = sets.entrySet().stream()
                    .map(x -> new JSONDefs.ItemStub(x.getKey(), x.getValue()))
                    .toArray();

            json = JSON.toJSONString(output);

            responseWriter.writeJsonResponse(json);
        } finally {
            postDecoder.destroy();
        }
    }

    private void getDeckStats(HttpRequest request, ResponseWriter responseWriter) throws IOException, HttpProcessingException {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String targetFormat = getFormParameterSafely(postDecoder, "targetFormat");
            String contents = getFormParameterSafely(postDecoder, "deckContents");

            //check for valid access
            getResourceOwnerSafely(request, participantId);

            CardDeck deck = _gameServer.createDeckWithValidate("tempDeck", contents, targetFormat, "");
            if (deck == null)
                throw new HttpProcessingException(400);

            StringBuilder sb = new StringBuilder();

            StringBuilder valid = new StringBuilder();
            StringBuilder invalid = new StringBuilder();

            GameFormat format = validateFormat(targetFormat);
            if(format == null || targetFormat == null)
            {
                responseWriter.writeHtmlResponse("Invalid format: " + targetFormat);
            }

            assert format != null;
            List<String> validation = format.validateDeck(deck);
            List<String> errataValidation = null;
            if (!format.getErrataCardMap().isEmpty()) {
                CardDeck deckWithErrata = format.applyErrata(deck);
                errataValidation = format.validateDeck(deckWithErrata);
            }
            if(validation.isEmpty()) {
                valid.append("<b>").append(format.getName()).append("</b>: <font color='green'>Valid</font><br/>");
            }
            else if(errataValidation != null && errataValidation.isEmpty()) {
                valid.append("<b>").append(format.getName()).append("</b>: ");
                valid.append("<font color='green'>Valid</font> ");
                valid.append("<font color='yellow'>(with errata automatically applied)</font><br/>");
                String output = String.join("<br>", validation).replace("\n", "<br>");
                invalid.append("<font color='yellow'>").append(output).append("</font><br/>");
            }
            else {
                String output = String.join("<br>", validation).replace("\n", "<br>");
                invalid.append("<b>").append(format.getName()).append("</b>: ");
                invalid.append("<font color='red'>").append(output).append("</font><br/>");
            }

            sb.append(valid);
            sb.append(invalid);

            responseWriter.writeHtmlResponse(sb.toString());
        } finally {
            postDecoder.destroy();
        }
    }

    private void deleteDeck(HttpRequest request, ResponseWriter responseWriter) throws IOException, HttpProcessingException {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String deckName = getFormParameterSafely(postDecoder, "deckName");
            User resourceOwner = getResourceOwnerSafely(request, participantId);

            _deckDao.deleteDeckForPlayer(resourceOwner, deckName);

            responseWriter.writeXmlResponse(null);
        } finally {
            postDecoder.destroy();
        }
    }

    private void renameDeck(HttpRequest request, ResponseWriter responseWriter) throws IOException, HttpProcessingException, ParserConfigurationException {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String deckName = getFormParameterSafely(postDecoder, "deckName");
            String oldDeckName = getFormParameterSafely(postDecoder, "oldDeckName");

            User resourceOwner = getResourceOwnerSafely(request, participantId);

            CardDeck deck = _deckDao.renameDeck(resourceOwner, oldDeckName, deckName);
            if (deck == null)
                throw new HttpProcessingException(404);

            responseWriter.writeXmlResponse(serializeDeck(deck));
        } finally {
            postDecoder.destroy();
        }
    }

    private void saveDeck(HttpRequest request, ResponseWriter responseWriter) throws IOException, HttpProcessingException, ParserConfigurationException {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String deckName = getFormParameterSafely(postDecoder, "deckName");
            String targetFormat = getFormParameterSafely(postDecoder, "targetFormat");
            String notes = getFormParameterSafely(postDecoder, "notes");
            String contents = getFormParameterSafely(postDecoder, "deckContents");

            User resourceOwner = getResourceOwnerSafely(request, participantId);

            GameFormat validatedFormat = validateFormat(targetFormat);

            CardDeck deck = _gameServer.createDeckWithValidate(deckName, contents, validatedFormat.getName(), notes);
            if (deck == null)
                throw new HttpProcessingException(400);

            _deckDao.saveDeckForPlayer(resourceOwner, deckName, validatedFormat.getName(), notes, deck);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document doc = documentBuilder.newDocument();
            Element deckElem = doc.createElement("ok");
            doc.appendChild(deckElem);

            responseWriter.writeXmlResponse(doc);
        } finally {
            postDecoder.destroy();
        }
    }

    private void shareDeck(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");
        String deckName = getQueryParameterSafely(queryDecoder, "deckName");
        User resourceOwner = getResourceOwnerSafely(request, participantId);

        String code = resourceOwner.getName() + "|" + deckName;

        String base64 = Base64.getEncoder().encodeToString(code.getBytes(StandardCharsets.UTF_8));
        String result = URLEncoder.encode(base64, StandardCharsets.UTF_8);

        responseWriter.writeHtmlResponse(result);
    }

    private void getDeckInHtml(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, CardNotFoundException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");
        String deckName = getQueryParameterSafely(queryDecoder, "deckName");
        String shareCode = getQueryParameterSafely(queryDecoder, "id");

        User resourceOwner;
        CardDeck deck;

        if (shareCode != null)
        {
            String code = new String(Base64.getDecoder().decode(shareCode), StandardCharsets.UTF_8);
            String[] fields = code.split("\\|");
            if(fields.length != 2)
                throw new HttpProcessingException(400);

            String user = fields[0];
            String deckName2 = fields[1];

            resourceOwner = _playerDao.getPlayer(user);
            deck = _deckDao.getDeckForPlayer(resourceOwner, deckName2);
        }
        else {
            resourceOwner = getResourceOwnerSafely(request, participantId);
            deck = _deckDao.getDeckForPlayer(resourceOwner, deckName);
        }

        if (deck == null)
            throw new HttpProcessingException(404);

        String result = convertDeckToHTML(deck, resourceOwner.getName());

        responseWriter.writeHtmlResponse(result);
    }

    private void getLibraryDeckInHtml(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, CardNotFoundException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String deckName = getQueryParameterSafely(queryDecoder, "deckName");

        CardDeck deck = _deckDao.getDeckForPlayer(getLibrarian(), deckName);

        if (deck == null)
            throw new HttpProcessingException(404);

        String result = convertDeckToHTML(deck, null);

        responseWriter.writeHtmlResponse(result);
    }

    public String convertDeckToHTML(CardDeck deck, String author) throws CardNotFoundException {

        if (deck == null)
            return null;

        StringBuilder result = new StringBuilder();
        result.append("""
<html>
    <style>
        body {
            margin:50;
        }
        
        .tooltip {
          border-bottom: 1px dotted black; /* If you want dots under the hoverable text */
          color:#0000FF;
        }
        
        .tooltip span, .tooltip title {
            display:none;
        }
        .tooltip:hover span:not(.click-disabled),.tooltip:active span:not(.click-disabled) {
            display:block;
            position:fixed;
            overflow:hidden;
            background-color: #FAEBD7;
            width:auto;
            z-index:9999;
            top:20%;
            left:350px;
        }
        /* This prevents tooltip images from automatically shrinking if they are near the window edge.*/
        .tooltip span > img {
            max-width:none !important;
            overflow:hidden;
        }
                        
    </style>
    <body>""");
        result.append("<h1>").append(StringEscapeUtils.escapeHtml(deck.getDeckName())).append("</h1>");
        result.append("<h2>Format: ").append(StringEscapeUtils.escapeHtml(deck.getTargetFormat())).append("</h2>");
        if(author != null) {
            result.append("<h2>Author: ").append(StringEscapeUtils.escapeHtml(author)).append("</h2>");
        }

        result.append(getHTMLDeck(deck, true, _sortAndFilterCards, _formatLibrary));
        result.append("<h3>Notes</h3><br>").append(deck.getNotes().replace("\n", "<br/>"));
        result.append("</body></html>");

        return result.toString();
    }

    private void getDeck(HttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException, ParserConfigurationException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");
        String deckName = getQueryParameterSafely(queryDecoder, "deckName");

        User resourceOwner = getResourceOwnerSafely(request, participantId);

        responseWriter.writeXmlResponse(serializeDeck(resourceOwner, deckName));
    }

    private void getLibraryDeck(HttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException, ParserConfigurationException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String deckName = getQueryParameterSafely(queryDecoder, "deckName");

        responseWriter.writeXmlResponse(serializeDeck(getLibrarian(), deckName));
    }

    private void listDecks(HttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException, ParserConfigurationException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");

        User resourceOwner = getResourceOwnerSafely(request, participantId);

        List<Map.Entry<GameFormat, String>> decks = GetDeckNamesAndFormats(resourceOwner);
        SortDecks(decks);

        Document doc = ConvertDeckNamesToXML(decks);
        responseWriter.writeXmlResponse(doc);
    }

    private Document ConvertDeckNamesToXML(List<Map.Entry<GameFormat, String>> deckNames)
            throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.newDocument();
        Element decksElem = doc.createElement("decks");

        for (Map.Entry<GameFormat, String> pair : deckNames) {
            Element deckElem = doc.createElement("deck");
            deckElem.setTextContent(pair.getValue());
            deckElem.setAttribute("targetFormat", pair.getKey().getName());
            decksElem.appendChild(deckElem);
        }
        doc.appendChild(decksElem);
        return doc;
    }

    private List<Map.Entry<GameFormat, String>> GetDeckNamesAndFormats(User player)
    {
        Set<Map.Entry<String, String>> names = new HashSet<>(_deckDao.getPlayerDeckNames(player));

        return names.stream()
                .map(pair -> new AbstractMap.SimpleEntry<>(_formatLibrary.getFormatByName(pair.getKey()), pair.getValue()))
                .collect(Collectors.toList());
    }

    private void SortDecks(List<Map.Entry<GameFormat, String>> decks)
    {
        decks.sort(Comparator.comparing((deck) -> {
            GameFormat format = deck.getKey();
            return String.format("%02d", format.getOrder()) + format.getName() + deck.getValue();
        }));
    }

    private void listLibraryDecks(ResponseWriter responseWriter)
            throws HttpProcessingException, ParserConfigurationException {
        List<Map.Entry<GameFormat, String>> starterDecks = new ArrayList<>();
        List<Map.Entry<GameFormat, String>> championshipDecks = new ArrayList<>();

        List<Map.Entry<GameFormat, String>> decks = GetDeckNamesAndFormats(getLibrarian());

        for (Map.Entry<GameFormat, String> pair : decks) {

            if (pair.getValue().contains("Starter"))
                starterDecks.add(pair);
            else
                championshipDecks.add(pair);
        }

        SortDecks(starterDecks);
        SortDecks(championshipDecks);

        //Keeps all the championship decks at the bottom of the list
        starterDecks.addAll(championshipDecks);

        Document doc = ConvertDeckNamesToXML(starterDecks);

        // Write the XML response
        responseWriter.writeXmlResponse(doc);
    }

    private Document serializeDeck(User player, String deckName) throws ParserConfigurationException {
        CardDeck deck = _deckDao.getDeckForPlayer(player, deckName);

        return serializeDeck(deck);
    }

    private GameFormat validateFormat(String name)
    {
        GameFormat validatedFormat = _formatLibrary.getFormat(name);
        if(validatedFormat == null)
        {
            try {
                validatedFormat = _formatLibrary.getFormatByName(name);
            }
            catch(Exception ex)
            {
                validatedFormat = _formatLibrary.getFormatByName("Anything Goes");
            }
        }

        return validatedFormat;
    }

    private Document serializeDeck(CardDeck deck) throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();
        Element deckElem = doc.createElement("deck");
        doc.appendChild(deckElem);

        if (deck == null)
            return doc;

        GameFormat validatedFormat = validateFormat(deck.getTargetFormat());

        Element targetFormat = doc.createElement("targetFormat");
        targetFormat.setAttribute("formatName", validatedFormat.getName());
        targetFormat.setAttribute("formatCode", validatedFormat.getCode());
        deckElem.appendChild(targetFormat);

        Element notes = doc.createElement("notes");
        notes.setTextContent(deck.getNotes());
        deckElem.appendChild(notes);

        for (SubDeck subDeck : deck.getSubDecks().keySet()) {
            for (String card : deck.getSubDecks().get(subDeck)) {
                Element cardElement = doc.createElement("card");
                cardElement.setAttribute("blueprintId", card);
                cardElement.setAttribute("subDeck", subDeck.name());
                try {
                    cardElement.setAttribute("imageUrl", deck.getLibrary().getCardBlueprint(card).getImageUrl());
                } catch (CardNotFoundException e) {
                    throw new RuntimeException("Blueprints not found: " + card);
                }
                deckElem.appendChild(cardElement);
            }
        }

        return doc;
    }

}
