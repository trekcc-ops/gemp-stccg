package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.JSONData;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.database.DeckDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.draft.SoloDraftDefinitions;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.formats.SealedEventDefinition;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class DeckRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final DeckDAO _deckDao;
    private final FormatLibrary _formatLibrary;
    private final SoloDraftDefinitions _draftLibrary;

    DeckRequestHandler(ServerObjects objects) {
        super(objects);
        _deckDao = objects.getDeckDAO();
        _formatLibrary = objects.getFormatLibrary();
        _draftLibrary = objects.getSoloDraftDefinitions();
    }

    private static CardDeck createDeckWithValidate(String deckName, String contents, String targetFormat,
                                                   String notes) {
        return new CardDeck(deckName, contents, targetFormat, notes);
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request,
                                    ResponseWriter responseWriter, String remoteIp) throws Exception {
        if ("/list".equals(uri) && request.method() == HttpMethod.GET) {
            listDecks(request, responseWriter);
        } else if ("/libraryList".equals(uri) && request.method() == HttpMethod.GET) {
            listLibraryDecks(responseWriter);
        } else if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            getDeck(request, responseWriter);
        } else if (uri.isEmpty() && request.method() == HttpMethod.POST) {
            saveDeck(request, responseWriter);
        } else if ("/library".equals(uri) && request.method() == HttpMethod.GET) {
            getLibraryDeck(request, responseWriter);
        } else if ("/share".equals(uri) && request.method() == HttpMethod.GET) {
            shareDeck(request, responseWriter);
        } else if ("/html".equals(uri) && request.method() == HttpMethod.GET) {
            getDeckInHtml(request, responseWriter);
        } else if ("/libraryHtml".equals(uri) && request.method() == HttpMethod.GET) {
            getLibraryDeckInHtml(request, responseWriter);
        } else if ("/rename".equals(uri) && request.method() == HttpMethod.POST) {
            renameDeck(request, responseWriter);
        } else if ("/delete".equals(uri) && request.method() == HttpMethod.POST) {
            deleteDeck(request, responseWriter);
        } else if ("/stats".equals(uri) && request.method() == HttpMethod.POST) {
            getDeckStats(request, responseWriter);
        } else if ("/formats".equals(uri) && request.method() == HttpMethod.POST) {
            getAllFormats(request, responseWriter);
        } else if ("/sets".equals(uri) && request.method() == HttpMethod.POST) {
            getSets(request, responseWriter);
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }



    private void getAllFormats(HttpRequest request, ResponseWriter responseWriter) throws IOException {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String includeEventsStr = getFormParameterSafely(postDecoder, FormParameter.includeEvents);
            String json;

            if("true".equalsIgnoreCase(includeEventsStr))
            {
                JSONData.FullFormatReadout data = new JSONData.FullFormatReadout();
                data.Formats = _formatLibrary.getAllFormats().values().stream()
                        .map(GameFormat::Serialize)
                        .collect(Collectors.toMap(x-> x.code, x-> x));
                data.SealedTemplates = _formatLibrary.GetAllSealedTemplates().values().stream()
                        .map(SealedEventDefinition::Serialize)
                        .collect(Collectors.toMap(x-> x.name, x-> x));
                data.DraftTemplates = _draftLibrary.getAllSoloDrafts().values().stream()
                        .map(soloDraft -> new JSONData.ItemStub(soloDraft.getCode(), soloDraft.getFormat()))
                        .collect(Collectors.toMap(x-> x.code, x-> x));

                json = JsonUtils.toJsonString(data);
            }
            else {
                Map<String, GameFormat> formats = _formatLibrary.getHallFormats();

                Object[] output = formats.entrySet().stream()
                        .map(x -> new JSONData.ItemStub(x.getKey(), x.getValue().getName()))
                        .toArray();

                json = JsonUtils.toJsonString(output);
            }

            responseWriter.writeJsonResponse(json);
        } finally {
            postDecoder.destroy();
        }
    }

    private void getSets(HttpRequest request, ResponseWriter responseWriter) throws IOException {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String format = getFormParameterSafely(postDecoder, FormParameter.format);
            GameFormat currentFormat = _formatLibrary.getFormat(format);

            Map<String, String> sets = currentFormat.getValidSetsAndTheirCards(_cardBlueprintLibrary);
            Object[] output = sets.entrySet().stream()
                    .map(x -> new JSONData.ItemStub(x.getKey(), x.getValue()))
                    .toArray();

            responseWriter.writeJsonResponse(JsonUtils.toJsonString(output));
        } finally {
            postDecoder.destroy();
        }
    }

    private void getDeckStats(HttpRequest request, ResponseWriter responseWriter)
            throws IOException, HttpProcessingException {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
            String targetFormat = getFormParameterSafely(postDecoder, FormParameter.targetFormat);
            String contents = getFormParameterSafely(postDecoder, FormParameter.deckContents);

            //check for valid access
            getResourceOwnerSafely(request, participantId);

            CardDeck deck = createDeckWithValidate("tempDeck", contents, targetFormat, "");
            GameFormat format = validateFormat(targetFormat);
            if(format == null || targetFormat == null)
            {
                responseWriter.writeHtmlResponse("Invalid format: " + targetFormat);
            }

            assert format != null;
            String response = HTMLUtils.getDeckValidation(deck, format);
            responseWriter.writeHtmlResponse(response);
        } finally {
            postDecoder.destroy();
        }
    }

    private void deleteDeck(HttpRequest request, ResponseWriter responseWriter)
            throws IOException, HttpProcessingException {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
            String deckName = getFormParameterSafely(postDecoder, FormParameter.deckName);
            User resourceOwner = getResourceOwnerSafely(request, participantId);

            _deckDao.deleteDeckForPlayer(resourceOwner, deckName);

            responseWriter.writeXmlResponse(null);
        } finally {
            postDecoder.destroy();
        }
    }

    private void renameDeck(HttpRequest request, ResponseWriter responseWriter)
            throws IOException, HttpProcessingException, ParserConfigurationException {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
            String deckName = getFormParameterSafely(postDecoder, FormParameter.deckName);
            String oldDeckName = getFormParameterSafely(postDecoder, FormParameter.oldDeckName);

            User resourceOwner = getResourceOwnerSafely(request, participantId);

            CardDeck deck = _deckDao.renameDeck(resourceOwner, oldDeckName, deckName);
            if (deck == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

            responseWriter.writeXmlResponse(serializeDeck(deck));
        } finally {
            postDecoder.destroy();
        }
    }

    private void saveDeck(HttpRequest request, ResponseWriter responseWriter)
            throws IOException, HttpProcessingException, ParserConfigurationException {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
            String deckName = getFormParameterSafely(postDecoder, FormParameter.deckName);
            String targetFormat = getFormParameterSafely(postDecoder, FormParameter.targetFormat);
            String notes = getFormParameterSafely(postDecoder, FormParameter.notes);
            String contents = getFormParameterSafely(postDecoder, FormParameter.deckContents);

            User resourceOwner = getResourceOwnerSafely(request, participantId);

            GameFormat validatedFormat = validateFormat(targetFormat);

            CardDeck deck = createDeckWithValidate(deckName, contents, validatedFormat.getName(), notes);

            _deckDao.saveDeckForPlayer(resourceOwner, deckName, validatedFormat.getName(), notes, deck);

            Document doc = createNewDoc();
            Element deckElem = doc.createElement("ok");
            doc.appendChild(deckElem);

            responseWriter.writeXmlResponse(doc);
        } finally {
            postDecoder.destroy();
        }
    }

    private void shareDeck(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, FormParameter.participantId);
        String deckName = getQueryParameterSafely(queryDecoder, FormParameter.deckName);
        User resourceOwner = getResourceOwnerSafely(request, participantId);

        String code = resourceOwner.getName() + "|" + deckName;

        String base64 = Base64.getEncoder().encodeToString(code.getBytes(StandardCharsets.UTF_8));
        String result = URLEncoder.encode(base64, StandardCharsets.UTF_8);

        responseWriter.writeHtmlResponse(result);
    }

    private void getDeckInHtml(HttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException, CardNotFoundException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, FormParameter.participantId);
        String deckName = getQueryParameterSafely(queryDecoder, FormParameter.deckName);
        String shareCode = getQueryParameterSafely(queryDecoder, FormParameter.id);

        User resourceOwner;
        CardDeck deck;

        if (shareCode != null)
        {
            String code = new String(Base64.getDecoder().decode(shareCode), StandardCharsets.UTF_8);
            String[] fields = code.split("\\|");
            if(fields.length != 2)
                throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400

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
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        String result = HTMLUtils.convertDeckToHTML(deck, resourceOwner.getName(), _formatLibrary, _cardBlueprintLibrary);

        responseWriter.writeHtmlResponse(result);
    }

    private void getLibraryDeckInHtml(HttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException, CardNotFoundException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String deckName = getQueryParameterSafely(queryDecoder, FormParameter.deckName);

        CardDeck deck = _deckDao.getDeckForPlayer(getLibrarian(), deckName);

        if (deck == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        String result = HTMLUtils.convertDeckToHTML(deck, null, _formatLibrary, _cardBlueprintLibrary);

        responseWriter.writeHtmlResponse(result);
    }


    private void getDeck(HttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException, ParserConfigurationException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, FormParameter.participantId);
        String deckName = getQueryParameterSafely(queryDecoder, FormParameter.deckName);

        User resourceOwner = getResourceOwnerSafely(request, participantId);

        responseWriter.writeXmlResponse(serializeDeck(resourceOwner, deckName));
    }

    private void getLibraryDeck(HttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException, ParserConfigurationException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String deckName = getQueryParameterSafely(queryDecoder, FormParameter.deckName);

        responseWriter.writeXmlResponse(serializeDeck(getLibrarian(), deckName));
    }

    private void listDecks(HttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException, ParserConfigurationException {
        User resourceOwner = getResourceOwner(request);
        List<Map.Entry<GameFormat, String>> decks = GetDeckNamesAndFormats(resourceOwner);
        SortDecks(decks);
        Document doc = ConvertDeckNamesToXML(decks);
        responseWriter.writeXmlResponse(doc);
    }

    private static Document ConvertDeckNamesToXML(Iterable<? extends Map.Entry<GameFormat, String>> deckNames)
            throws ParserConfigurationException {
        Document doc = createNewDoc();
        Element decksElem = doc.createElement(FormParameter.decks.name());

        for (Map.Entry<GameFormat, String> pair : deckNames) {
            Element deckElem = doc.createElement(FormParameter.deck.name());
            deckElem.setTextContent(pair.getValue());
            deckElem.setAttribute("targetFormat", pair.getKey().getName());
            decksElem.appendChild(deckElem);
        }
        doc.appendChild(decksElem);
        return doc;
    }

    private List<Map.Entry<GameFormat, String>> GetDeckNamesAndFormats(User player)
    {
        Collection<Map.Entry<String, String>> names = new HashSet<>(_deckDao.getPlayerDeckNames(player));

        return names.stream()
                .map(pair -> {
                    GameFormat format = _formatLibrary.getFormatByName(pair.getKey());
                    return new AbstractMap.SimpleEntry<>(format, pair.getValue());
                })
                .collect(Collectors.toList());
    }

    private static void SortDecks(List<? extends Map.Entry<GameFormat, String>> decks)
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
        Document doc = createNewDoc();
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
                    cardElement.setAttribute("imageUrl", _cardBlueprintLibrary.getCardBlueprint(card).getImageUrl());
                } catch (CardNotFoundException e) {
                    throw new RuntimeException("Blueprints not found: " + card);
                }
                deckElem.appendChild(cardElement);
            }
        }

        return doc;
    }

}