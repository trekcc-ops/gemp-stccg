package com.gempukku.stccg.cards;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.common.JSONDefs;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ICallback;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hjson.JsonValue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Semaphore;

public class CardBlueprintLibrary {
    private static final Logger LOGGER = LogManager.getLogger(CardBlueprintLibrary.class);

    private final Map<String, CardBlueprint> _blueprints = new HashMap<>();
    private final Map<String, String> _blueprintMapping = new HashMap<>();
    private final Map<String, Set<String>> _fullBlueprintMapping = new HashMap<>();
    private final Map<String, SetDefinition> _allSets = new LinkedHashMap<>();

    private final CardBlueprintFactory cardBlueprintBuilder = new CardBlueprintFactory();

    private final Semaphore collectionReady = new Semaphore(1);
    private final File _cardPath = AppConfig.getCardsPath();
    private final List<ICallback> _refreshCallbacks = new ArrayList<>();
    private boolean _blueprintLoadErrorEncountered;

    public CardBlueprintLibrary() {
        LOGGER.info("Locking blueprint library in constructor");
        //This will be released after the library has been initialized. Until then, all functional uses will be blocked.
        collectionReady.acquireUninterruptibly();
        _blueprintLoadErrorEncountered = false;

        loadSets();
        loadMappings();
        loadCards(_cardPath, true);

        LOGGER.info("Unlocking blueprint library in constructor");
        collectionReady.release();
    }

    public void SubscribeToRefreshes(ICallback callback) {
        if(!_refreshCallbacks.contains(callback))
            _refreshCallbacks.add(callback);
    }

    public PhysicalCard createPhysicalCard(DefaultGame game, String blueprintId, int cardId, String playerId)
            throws CardNotFoundException {
        return getCardBlueprint(blueprintId).createPhysicalCard(game, cardId, game.getGameState().getPlayer(playerId));
    }

    public Map<String, SetDefinition> getSetDefinitions() {
        return Collections.unmodifiableMap(_allSets);
    }

    public void reloadAllDefinitions() {
        reloadSets();
        reloadMappings();
        reloadCards();
            // TODO - Removing getErrata functionality. Undecided about long-term STCCG goals for this, but for now there are none in Gemp.
/*        errataMappings = null;
        getErrata(); */

        for(var callback : _refreshCallbacks) {
            callback.Invoke();
        }
    }

    private void reloadSets() {
        try {
            collectionReady.acquire();
            loadSets();
            collectionReady.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void reloadMappings() {
        try {
            collectionReady.acquire();
            loadMappings();
            collectionReady.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void reloadCards() {
        _blueprintLoadErrorEncountered = false;
        try {
            collectionReady.acquire();
            loadCards(_cardPath, false);
            collectionReady.release();
        } catch (InterruptedException e) {
            _blueprintLoadErrorEncountered = true;
            throw new RuntimeException(e);
        }
    }

    private void loadSets() {
        try {
            final InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(AppConfig.getSetDefinitionsPath()), StandardCharsets.UTF_8);
            try {
                String json = JsonValue.readHjson(reader).toString();
                JSONParser parser = new JSONParser();
                JSONArray object = (JSONArray) parser.parse(json);
                for (Object setDefinitionObj : object) {
                    JSONObject setJsonDef = (JSONObject) setDefinitionObj;

                    String setId = (String) setJsonDef.get("setId");
                    String setName = (String) setJsonDef.get("setName");

                    Set<String> flags = new HashSet<>();
                    determineOriginalSetFlag(setJsonDef, flags);
                    determineMerchantableFlag(setJsonDef, flags);
                    determineNeedsLoadingFlag(setJsonDef, flags);

                    SetDefinition setDefinition = new SetDefinition(setId, setName, flags);
                    _allSets.put(setId, setDefinition);
                }
            } finally {
                IOUtils.closeQuietly(reader);
            }
        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse setConfig.json file");
        } catch (IOException exp) {
            throw new RuntimeException("Unable to read card rarities: " + exp);
        }
    }

    private void loadMappings() {
        try {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(AppConfig.getMappingsPath()), StandardCharsets.UTF_8))) {
                String line;

                _blueprintMapping.clear();
                _fullBlueprintMapping.clear();

                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.startsWith("#")) {
                        String[] split = line.split(",");
                        _blueprintMapping.put(split[0], split[1]);
                        addAlternatives(split[0], split[1]);
                    }
                }
            }
        } catch (IOException exp) {
            throw new RuntimeException("Problem loading blueprintMapping.txt", exp);
        }
    }

    private void loadCards(File path, boolean initial) {
        if (path.isFile()) {
            loadCardsFromFile(path, initial);
        }
        else if (path.isDirectory()) {
            for (File file : Objects.requireNonNull(path.listFiles())) {
                loadCards(file, initial);
            }
        }
    }

    private void loadCardsFromFile(File file, boolean validateNew) {
        if (JsonUtils.IsInvalidHjsonFile(file))
            return;
        JSONParser parser = new JSONParser();
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            //This will read both json and hjson, producing standard json
            String json = JsonValue.readHjson(reader).toString();

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> testBlueprintMap = objectMapper.readValue(json, new TypeReference<>() {});
            JsonNode jsonNode = objectMapper.readTree(json);
            final JSONObject cardsFile = (JSONObject) parser.parse(json);
            final Set<Map.Entry<String, JSONObject>> cardsInFile = cardsFile.entrySet();

            List<String> blueprintIds = new ArrayList<>();
            jsonNode.fieldNames().forEachRemaining(blueprintIds::add);

            for (String blueprintId : blueprintIds) {
                try {
                    final CardBlueprint cardBlueprint =
                            cardBlueprintBuilder.buildFromJsonNew(blueprintId, jsonNode.get(blueprintId));
                    _blueprints.put(blueprintId, cardBlueprint);
                    String setNumber = blueprintId.substring(0, blueprintId.indexOf("_"));
                    _allSets.get(setNumber).addCard(blueprintId, cardBlueprint.getRarity());
                } catch (Exception exp){
                    // invaldcarddefinition
                    _blueprintLoadErrorEncountered = true;
                    LOGGER.error("Unable to load card ", exp);

                }
            }

            jsonNode.forEach((JsonNode node) -> {
                try {
                    boolean check;
                    String blueprintId;
                    JsonNode blueprintIdObj = node.get("blueprintId");
                    if (blueprintIdObj != null) {
                        blueprintId = blueprintIdObj.textValue();
                        if (Objects.equals(blueprintId, "105_015")) {
                            final CardBlueprint cardBlueprint = cardBlueprintBuilder.buildFromJsonNew(blueprintId, node);
                            _blueprints.put(blueprintId, cardBlueprint);
                            String setNumber = blueprintId.substring(0, blueprintId.indexOf("_"));
                            _allSets.get(setNumber).addCard(blueprintId, cardBlueprint.getRarity());
                        }
                    }
                } catch (Exception exp){
                // invaldcarddefinition
                    _blueprintLoadErrorEncountered = true;
                    LOGGER.error("Unable to load card ", exp);

                }
            }
            );
/*
            for (Map.Entry<String, JSONObject> cardEntry : cardsInFile) {
                String blueprintId = cardEntry.getKey();
                if (validateNew)
                    if (_blueprints.containsKey(blueprintId))
                        LOGGER.error(blueprintId + " - Replacing existing card definition!");
            }
*/
        } catch (Exception exp) {
            _blueprintLoadErrorEncountered = true;
            String errorMessage = switch (exp) {
                case FileNotFoundException fileNotFoundException -> "Failed to find file";
                case IOException ioException -> "Error while loading file";
                case ParseException parseException -> "Failed to parse file";
                default -> "Unexpected error while parsing file";
            };
            LOGGER.error(errorMessage + " " + file.getAbsolutePath(), exp);
        }
        LOGGER.debug("Loaded JSON card file " + file.getName());
    }

    public String getBaseBlueprintId(String blueprintId) {
        blueprintId = stripBlueprintModifiers(blueprintId);
        String base = _blueprintMapping.get(blueprintId);
        if (base != null)
            return base;
        return blueprintId;
    }

    private void addAlternatives(String newBlueprint, String existingBlueprint) {
        Set<String> existingAlternates = _fullBlueprintMapping.get(existingBlueprint);
        if (existingAlternates != null) {
            for (String existingAlternate : existingAlternates) {
                addAlternative(newBlueprint, existingAlternate);
                addAlternative(existingAlternate, newBlueprint);
            }
        }
        addAlternative(newBlueprint, existingBlueprint);
        addAlternative(existingBlueprint, newBlueprint);
    }

    private void addAlternative(String from, String to) {
        Set<String> list = _fullBlueprintMapping.computeIfAbsent(from, k -> new HashSet<>());
        list.add(to);
    }

    public Map<String, CardBlueprint> getBaseCards() {
        try {
            collectionReady.acquire();
            var data = Collections.unmodifiableMap(_blueprints);
            collectionReady.release();
            return data;
        } catch (InterruptedException exp) {
            throw new RuntimeException("CardBlueprintLibrary.getBaseCard() interrupted: ", exp);
        }
    }

    public Set<String> getAllAlternates(String blueprintId) {
        try {
            collectionReady.acquire();
            var data = _fullBlueprintMapping.get(blueprintId);
            collectionReady.release();
            return data;
        } catch (InterruptedException exp) {
            throw new RuntimeException("CardBlueprintLibrary.getAllAlternates() interrupted: ", exp);
        }
    }

    private Map<String, JSONDefs.ErrataInfo> errataMappings = null;
    public Map<String, JSONDefs.ErrataInfo> getErrata() {
        try {
            if(errataMappings == null) {
                collectionReady.acquire();
                errataMappings = new HashMap<>();
                for (String id : _blueprints.keySet()) {
                    var parts = id.split("_");
                    int setID = Integer.parseInt(parts[0]);
                    String cardID = parts[1];
                    JSONDefs.ErrataInfo card;
                    String base;
                    if(setID >= 50 && setID <= 69) {
                        base = (setID - 50) + "_" + cardID;
                    }
                    else if(setID >= 70 && setID <= 89) {
                        base = (setID - 70) + "_" + cardID;
                    }
                    else if(setID >= 150 && setID <= 199) {
                        base = (setID - 50) + "_" + cardID;
                    }
                    else
                        continue;

                    if(errataMappings.containsKey(base)) {
                        card = errataMappings.get(base);
                    }
                    else {
                        CardBlueprint blueprint = _blueprints.get(base);
                        card = new JSONDefs.ErrataInfo();
                        card.BaseID = base;
                        card.Name = blueprint.getFullName();
                        card.LinkText = blueprint.getCardLink();
                        card.ErrataIDs = new HashMap<>();
                        errataMappings.put(base, card);
                    }

                    card.ErrataIDs.put(JSONDefs.ErrataInfo.PC_Errata, id);
                }

                collectionReady.release();
            }
            return errataMappings;
        } catch (InterruptedException exp) {
            throw new RuntimeException("CardBlueprintLibrary.getErrata() interrupted: ", exp);
        }
    }

    public boolean hasAlternateInSet(String blueprintId, int setNo) {
        try {
            collectionReady.acquire();
            var alternatives = _fullBlueprintMapping.get(blueprintId);
            collectionReady.release();

            if (alternatives != null)
                for (String alternative : alternatives)
                    if (alternative.startsWith(setNo + "_"))
                        return true;

            return false;
        } catch (InterruptedException exp) {
            throw new RuntimeException("CardBlueprintLibrary.hasAlternateInSet() interrupted: ", exp);
        }
    }

    public String getCardFullName(String blueprintId) throws CardNotFoundException {
        return getCardBlueprint(blueprintId).getFullName();
    }

    public CardBlueprint getCardBlueprint(String blueprintId) throws CardNotFoundException {
        blueprintId = stripBlueprintModifiers(blueprintId);
        CardBlueprint bp = null;
        
        try {
            collectionReady.acquire();
            if (_blueprints.containsKey(blueprintId)) {
                bp = _blueprints.get(blueprintId);
            }
            collectionReady.release();

            if(bp != null)
                return bp;

        } catch (InterruptedException exp) {
            throw new RuntimeException("CardBlueprintLibrary.getCardBlueprint() interrupted: ", exp);
        }

        // Throw exception if card not found in blueprints
        throw new CardNotFoundException(blueprintId);
    }

    public String stripBlueprintModifiers(String blueprintId) {
        if (blueprintId.endsWith("*"))
            blueprintId = blueprintId.substring(0, blueprintId.length() - 1);
        if (blueprintId.endsWith("T"))
            blueprintId = blueprintId.substring(0, blueprintId.length() - 1);
        return blueprintId;
    }

    private void determineNeedsLoadingFlag(JSONObject setDefinition, Set<String> flags) {
        Boolean needsLoading = (Boolean) setDefinition.get("needsLoading");
        if (needsLoading == null)
            needsLoading = true;
        if (needsLoading)
            flags.add("needsLoading");
    }

    private void determineMerchantableFlag(JSONObject setDefinition, Set<String> flags) {
        Boolean merchantable = (Boolean) setDefinition.get("merchantable");
        if (merchantable == null)
            merchantable = true;
        if (merchantable)
            flags.add("merchantable");
    }

    private void determineOriginalSetFlag(JSONObject setDefinition, Set<String> flags) {
        Boolean originalSet = (Boolean) setDefinition.get("originalSet");
        if (originalSet == null)
            originalSet = true;
        if (originalSet)
            flags.add("originalSet");
    }

    public String getRandomBlueprintId() {
        return new ArrayList<>(_blueprints.keySet()).get(new Random().nextInt(_blueprints.size()));
    }

    public List<String> getAllBlueprintIds() {
        return new ArrayList<>(_blueprints.keySet());
    }

    public boolean checkLoadSuccess() {
        return !_blueprintLoadErrorEncountered;
    }

    public CardBlueprint get(String blueprintId) { return _blueprints.get(blueprintId); }

    public CardBlueprint getBlueprintByName(String title, String setName) throws CardNotFoundException {

        for (CardBlueprint blueprint : _blueprints.values()) {
            if (Objects.equals(blueprint.getTitle(), title))
                return blueprint;
        }

        throw new CardNotFoundException("Could not find card " + title + " in set " + setName);
    }
}
