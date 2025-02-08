package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.JSONData;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.database.DeckDAO;
import com.gempukku.stccg.database.DeckNotFoundException;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DeckRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final DeckDAO _deckDao;
    private final FormatLibrary _formatLibrary;

    DeckRequestHandler(ServerObjects objects) {
        super(objects);
        _deckDao = objects.getDeckDAO();
        _formatLibrary = objects.getFormatLibrary();
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
        } else if (uri.startsWith("/import/") && request.method() == HttpMethod.GET) {
            importCollection(request, responseWriter);
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
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String includeEventsStr = getFormParameterSafely(postDecoder, FormParameter.includeEvents);
            String json;

            if("true".equalsIgnoreCase(includeEventsStr)) {
                json = _jsonMapper.writeValueAsString(_formatLibrary);
            } else {
                Map<String, GameFormat> formats = _formatLibrary.getHallFormats();
                Object[] output = formats.entrySet().stream()
                        .map(x -> new JSONData.ItemStub(x.getKey(), x.getValue().getName()))
                        .toArray();
                json = _jsonMapper.writeValueAsString(output);
            }
            responseWriter.writeJsonResponse(json);
        }
    }

    private void getSets(HttpRequest request, ResponseWriter responseWriter) throws IOException {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String format = getFormParameterSafely(postDecoder, FormParameter.format);
            GameFormat currentFormat = _formatLibrary.get(format);

            Map<String, String> sets = currentFormat.getValidSetsAndTheirCards(_cardBlueprintLibrary);
            Object[] output = sets.entrySet().stream()
                    .map(x -> new JSONData.ItemStub(x.getKey(), x.getValue()))
                    .toArray();

            responseWriter.writeJsonResponse(_jsonMapper.writeValueAsString(output));
        }
    }

    private void getDeckStats(HttpRequest request, ResponseWriter responseWriter)
            throws IOException, HttpProcessingException {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
            String targetFormat = getFormParameterSafely(postDecoder, FormParameter.targetFormat);
            String contents = getFormParameterSafely(postDecoder, FormParameter.deckContents);

            //check for valid access
            getResourceOwnerSafely(request, participantId);

            CardDeck deck = new CardDeck("tempDeck", contents, targetFormat);
            GameFormat format = validateFormat(targetFormat);
            if(format == null || targetFormat == null)
            {
                responseWriter.writeHtmlResponse("Invalid format: " + targetFormat);
            }

            assert format != null;
            String response = HTMLUtils.getDeckValidation(_cardBlueprintLibrary, deck, format);
            responseWriter.writeHtmlResponse(response);
        }
    }

    private void deleteDeck(HttpRequest request, ResponseWriter responseWriter)
            throws IOException, HttpProcessingException {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
            User resourceOwner = getResourceOwnerSafely(request, participantId);
            String deckName = getFormParameterSafely(postDecoder, FormParameter.deckName);
            _deckDao.deleteDeckForPlayer(resourceOwner, deckName);
            responseWriter.writeXmlOkResponse();
        }
    }

    private void importCollection(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        //noinspection SpellCheckingInspection
        List<GenericCardItem> importResult = processImport(
                getQueryParameterSafely(new QueryStringDecoder(request.uri()), FormParameter.decklist),
                _cardBlueprintLibrary
        );

        Document doc = createNewDoc();
        Element collectionElem = doc.createElement("collection");
        collectionElem.setAttribute(FormParameter.count.name(), String.valueOf(importResult.size()));
        doc.appendChild(collectionElem);

        for (GenericCardItem item : importResult) {
            appendCardElement(doc, collectionElem, item, true);
        }

        Map<String, String> headers = new HashMap<>();

        responseWriter.writeXmlResponseWithHeaders(doc, headers);
    }

    private static List<GenericCardItem> processImport(String rawDeckList, CardBlueprintLibrary cardLibrary) {
        Map<String, SubDeck> lackeySubDeckMap = new HashMap<>();
        for (SubDeck subDeck : SubDeck.values()) {
            lackeySubDeckMap.put(subDeck.getLackeyName() + ":", subDeck);
        }
        // Assumes formatting from Lackey txt files. "Draw deck" is not called out explicitly.
        SubDeck currentSubDeck = SubDeck.DRAW_DECK;

        List<GenericCardItem> result = new ArrayList<>();
        for (CardCount cardCount : getDecklist(rawDeckList)) {
            SubDeck newSubDeck = lackeySubDeckMap.get(cardCount.name);
            if (newSubDeck != null) currentSubDeck = newSubDeck;
            else {
                for (Map.Entry<String, CardBlueprint> cardBlueprint : cardLibrary.getBaseCards().entrySet()) {
                    String id = cardBlueprint.getKey();
                    try {
                        // If set is not a nonzero number, the card is not from a supported set
                        int set = Integer.parseInt(id.split("_")[0]);
                        if (set >= 0) {
                            CardBlueprint blueprint = cardBlueprint.getValue();

                            if (blueprint != null &&
                                    SortAndFilterCards.replaceSpecialCharacters(blueprint.getFullName().toLowerCase())
                                            .equals(cardCount.name())
                            ) {
                                result.add(GenericCardItem.createItem(id, cardCount.count(), currentSubDeck));
                                break;
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return result;
    }

    private record CardCount(String name, int count) { }


    private void appendCardElement(Document doc, Node collectionElem, GenericCardItem item,
                                   boolean setSubDeckAttribute) throws Exception {
        Element card = doc.createElement("card");
        if (setSubDeckAttribute) {
            String subDeck = item.getSubDeckString();
            if (subDeck != null)
                card.setAttribute("subDeck", subDeck);
        }
        card.setAttribute("count", String.valueOf(item.getCount()));
        card.setAttribute("blueprintId", item.getBlueprintId());
        CardBlueprint blueprint = _cardBlueprintLibrary.getCardBlueprint(item.getBlueprintId());
        card.setAttribute("imageUrl", blueprint.getImageUrl());
        collectionElem.appendChild(card);
    }

    private static List<CardCount> getDecklist(String rawDeckList) {
        int quantity;
        String cardLine;

        List<CardCount> result = new ArrayList<>();
        for (String line : rawDeckList.split("~")) {
            if (line.isEmpty())
                continue;

            String line1 = line.toLowerCase();
            try {
                var matches = Pattern.compile("^(x?\\s*\\d+\\s*x?)?\\s*(.*?)\\s*(x?\\d+x?)?\\s*$").matcher(line1);

                if(matches.matches()) {
                    if(!StringUtils.isEmpty(matches.group(1))) {
                        quantity = Integer.parseInt(matches.group(1).replaceAll("\\D+", ""));
                    }
                    else if(!StringUtils.isEmpty(matches.group(3))) {
                        quantity = Integer.parseInt(matches.group(3).replaceAll("\\D+", ""));
                    }
                    else {
                        quantity = 1;
                    }

                    cardLine = matches.group(2).trim();
                    result.add(new CardCount(SortAndFilterCards.replaceSpecialCharacters(cardLine).trim(), quantity));
                }
            } catch (Exception exp) {
                System.out.println("blah");
            }
        }
        return result;
    }





    private void renameDeck(HttpRequest request, ResponseWriter responseWriter)
            throws IOException, HttpProcessingException {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
            User resourceOwner = getResourceOwnerSafely(request, participantId);
            String deckName = getFormParameterSafely(postDecoder, FormParameter.deckName);
            String oldDeckName = getFormParameterSafely(postDecoder, FormParameter.oldDeckName);
            CardDeck deck = _deckDao.renameDeck(resourceOwner, oldDeckName, deckName);
            if (deck == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
            String jsonString = _jsonMapper.writeValueAsString(new JsonSerializedDeck(deck));
            responseWriter.writeJsonResponse(jsonString);
        } catch (DeckNotFoundException e) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private void saveDeck(HttpRequest request, ResponseWriter responseWriter)
            throws IOException, HttpProcessingException {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
            String deckName = getFormParameterSafely(postDecoder, FormParameter.deckName);
            String targetFormat = getFormParameterSafely(postDecoder, FormParameter.targetFormat);
            String notes = getFormParameterSafely(postDecoder, FormParameter.notes);
            String contents = getFormParameterSafely(postDecoder, FormParameter.deckContents);
            User resourceOwner = getResourceOwnerSafely(request, participantId);
            GameFormat validatedFormat = validateFormat(targetFormat);
            CardDeck deck = new CardDeck(deckName, contents, validatedFormat.getName(), notes);
            _deckDao.saveDeckForPlayer(deck, resourceOwner);
            responseWriter.writeXmlOkResponse();
        }
    }

    private void shareDeck(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String deckName = getQueryParameterSafely(queryDecoder, FormParameter.deckName);
        User resourceOwner = getUserIdFromCookiesOrUri(request);
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
            throws HttpProcessingException, JsonProcessingException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String deckName = getQueryParameterSafely(queryDecoder, FormParameter.deckName);
        User resourceOwner = getUserIdFromCookiesOrUri(request);
        CardDeck deck = _deckDao.getDeckForPlayer(resourceOwner, deckName);
        String jsonString = _jsonMapper.writeValueAsString(new JsonSerializedDeck(deck));
        responseWriter.writeJsonResponse(jsonString);
    }

    private void getLibraryDeck(HttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException, JsonProcessingException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String deckName = getQueryParameterSafely(queryDecoder, FormParameter.deckName);
        CardDeck deck = _deckDao.getDeckForPlayer(getLibrarian(), deckName);
        String jsonString = _jsonMapper.writeValueAsString(new JsonSerializedDeck(deck));
        responseWriter.writeJsonResponse(jsonString);
    }

    private void listDecks(HttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException, JsonProcessingException {
        User resourceOwner = getUserIdFromCookiesOrUri(request);
        List<Map.Entry<GameFormat, String>> decks = GetDeckNamesAndFormats(resourceOwner);
        SortDecks(decks);
        String jsonString = ConvertDeckNamesToJson(decks);
        responseWriter.writeJsonResponse(jsonString);
    }

    private String ConvertDeckNamesToJson(Iterable<? extends Map.Entry<GameFormat, String>> deckNames)
            throws JsonProcessingException {
        List<Map<String, String>> result = new ArrayList<>();
        for (Map.Entry<GameFormat, String> pair : deckNames) {
            HashMap<String, String> map = new HashMap<>();
            map.put("deckName", pair.getValue());
            map.put("targetFormat", pair.getKey().getName());
            result.add(map);
        }
        return _jsonMapper.writeValueAsString(result);
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
            throws HttpProcessingException, JsonProcessingException {
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
        String jsonString = ConvertDeckNamesToJson(starterDecks);
        responseWriter.writeJsonResponse(jsonString);
    }

    private GameFormat validateFormat(String name)
    {
        GameFormat validatedFormat = _formatLibrary.get(name);
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

    private class JsonSerializedDeck {

        private final CardDeck _cardDeck;

        JsonSerializedDeck(CardDeck cardDeck) {
            _cardDeck = cardDeck;
        }

        @JsonProperty("notes")
        String getNotes() {
            return _cardDeck.getNotes();
        }

        @JsonProperty("targetFormat")
        Map<String, String> getFormat() {
            GameFormat format = validateFormat(_cardDeck.getTargetFormat());
            Map<String, String> result = new HashMap<>();
            result.put("formatName", format.getName());
            result.put("formatCode", format.getCode());
            return result;
        }

        @JsonProperty("cards")
        List<Map<String, String>> getCards() throws CardNotFoundException {
            List<Map<String, String>> result = new ArrayList<>();
            for (SubDeck subDeck : _cardDeck.getSubDecks().keySet()) {
                for (String card : _cardDeck.getSubDecks().get(subDeck)) {
                    Map<String, String> cardInfo = new HashMap<>();
                    cardInfo.put("blueprintId", card);
                    cardInfo.put("subDeck", subDeck.name());
                    cardInfo.put("imageUrl", _cardBlueprintLibrary.getCardBlueprint(card).getImageUrl());
                    result.add(cardInfo);
                }
            }
            return result;
        }
    }


}