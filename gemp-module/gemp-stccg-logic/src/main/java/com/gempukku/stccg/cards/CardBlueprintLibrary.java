package com.gempukku.stccg.cards;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintDeserializer;
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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Semaphore;

public class CardBlueprintLibrary {
    private static final Logger LOGGER = LogManager.getLogger(CardBlueprintLibrary.class);
    private final Map<String, CardBlueprint> _blueprints = new HashMap<>();
    private final Map<String, String> _blueprintMapping = new HashMap<>();
    private final Map<String, Set<String>> _alternateBlueprintMapping = new HashMap<>();
    private final Map<String, SetDefinition> _allSets = new LinkedHashMap<>();

    private final CardBlueprintFactory cardBlueprintBuilder = new CardBlueprintFactory();

    private final Semaphore collectionReady = new Semaphore(1);
    private final File _cardPath = AppConfig.getCardsPath();
    private final List<ICallback> _refreshCallbacks = new ArrayList<>();
    private boolean _blueprintLoadErrorEncountered;
    private final ObjectMapper _objectMapper = new ObjectMapper();

    public CardBlueprintLibrary() {
        LOGGER.info("Locking blueprint library in constructor");

        SimpleModule module = new SimpleModule();
        module.addDeserializer(CardBlueprint.class, new CardBlueprintDeserializer());
        _objectMapper.registerModule(module);

        //This will be released after the library has been initialized. Until then, all functional uses will be blocked.
        collectionReady.acquireUninterruptibly();
        _blueprintLoadErrorEncountered = false;

        loadSets();
        loadCards(_cardPath, true);
        loadMappings();

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
        reloadCards();
        reloadMappings();
        _refreshCallbacks.forEach(ICallback::Invoke);
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

    public void reloadMappings() {
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
                for (JsonNode setDefinitionObj : _objectMapper.readTree(JsonUtils.readJson(reader))) {

                    String setId = setDefinitionObj.get("setId").textValue();
                    String setName = setDefinitionObj.get("setName").textValue();

                    Set<String> flags = new HashSet<>();
                    determineNeedsLoadingFlag(setDefinitionObj, flags);

                    SetDefinition setDefinition = new SetDefinition(setId, setName, flags);
                    _allSets.put(setId, setDefinition);
                }
        } finally {
                IOUtils.closeQuietly(reader);
            }
        } catch (IOException exp) {
            throw new RuntimeException("Unable to read card rarities: " + exp);
        }
    }

    private void loadMappings() {
        _blueprintMapping.clear();
        _alternateBlueprintMapping.clear();

        for (CardBlueprint blueprint : _blueprints.values()) {
            String childBlueprintId = blueprint.getBlueprintId();
            String parentBlueprintId = blueprint.getBaseBlueprintId();

            // If the blueprint has a parent ID, associate the blueprint with that parent and all its other children
            if (parentBlueprintId != null) {
                _blueprintMapping.put(childBlueprintId, parentBlueprintId);

                // If the parent blueprint already has other children, they are considered alternates for the new child
                if (_alternateBlueprintMapping.get(parentBlueprintId) != null)
                    for (String existingAlternate : _alternateBlueprintMapping.get(parentBlueprintId))
                        addAsAlternates(existingAlternate, childBlueprintId);

                // The parent and new child are also alternates for each other
                addAsAlternates(parentBlueprintId, childBlueprintId);
            }
        }
    }

    private void addAsAlternates(String blueprint1, String blueprint2) {
        _alternateBlueprintMapping.computeIfAbsent(blueprint1, k -> new HashSet<>()).add(blueprint2);
        _alternateBlueprintMapping.computeIfAbsent(blueprint2, k -> new HashSet<>()).add(blueprint1);
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
        try {
            JsonNode jsonNode = JsonUtils.readJsonFromFile(file);
            List<String> blueprintIds = new ArrayList<>();
            jsonNode.fieldNames().forEachRemaining(blueprintIds::add);

            for (String blueprintId : blueprintIds) {
                try {
                    if (blueprintId.equals("155_080")) {
                        int x = 5;
                        int y = x + 3;
                    }
                        // TODO - Trying both methods below. Ultimate goal is to use loadCardFromDeserializer.
                    final CardBlueprint cardBlueprint = loadCardFromDeserializer(blueprintId, jsonNode);
//                    final CardBlueprint cardBlueprint = loadCardFromFactory(blueprintId, jsonNode);
                    _blueprints.put(blueprintId, cardBlueprint);
                    String setNumber = blueprintId.substring(0, blueprintId.indexOf("_"));
                    _allSets.get(setNumber).addCard(blueprintId, cardBlueprint.getRarity());
                } catch (Exception exp) {
                    _blueprintLoadErrorEncountered = true;
                    LOGGER.error("Unable to load card ", exp);
                }
            }
        } catch (Exception exp) {
            _blueprintLoadErrorEncountered = true;
            String errorMessage = switch (exp) {
                case FileNotFoundException fileNotFoundException -> "Failed to find file";
                case IOException ioException -> "Error while loading file";
                default -> "Unexpected error while parsing file";
            };
            LOGGER.error(errorMessage + " " + file.getAbsolutePath(), exp);
        }
        LOGGER.debug("Loaded JSON card file " + file.getName());
    }

    private CardBlueprint loadCardFromFactory(String blueprintId, JsonNode node)
            throws InvalidCardDefinitionException {
        return cardBlueprintBuilder.buildFromJsonNew(blueprintId, node.get(blueprintId));
    }
    private CardBlueprint loadCardFromDeserializer(String blueprintId, JsonNode node)
            throws JsonProcessingException, InvalidCardDefinitionException {
        JsonNode cardNode = node.get(blueprintId);
        if (cardNode == null)
            throw new InvalidCardDefinitionException("Could not find node for blueprintId " + blueprintId);
        if (!cardNode.has("blueprintId"))
            ((ObjectNode) cardNode).put("blueprintId", blueprintId);
        else if (!cardNode.get("blueprintId").textValue().equals(blueprintId))
            throw new InvalidCardDefinitionException("Non-matching card blueprint property 'blueprintId' " +
                    cardNode.get("blueprintId").textValue() + " for blueprint " + blueprintId);
        return _objectMapper.readValue(cardNode.toString(), CardBlueprint.class);
    }

    public String getBaseBlueprintId(String blueprintId) {
        String rawBlueprintId = stripBlueprintModifiers(blueprintId);
        String base = _blueprintMapping.get(rawBlueprintId);
        return (base != null) ? base : rawBlueprintId;
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
            Set<String> data = _alternateBlueprintMapping.get(blueprintId);
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
            var alternatives = _alternateBlueprintMapping.get(blueprintId);
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

    private void determineNeedsLoadingFlag(JsonNode setDefinition, Set<String> flags) {
        boolean needsLoading = setDefinition.get("needsLoading") == null ||
                !setDefinition.get("needsLoading").isBoolean() ||
                setDefinition.get("needsLoading").asBoolean();
        if (needsLoading)
            flags.add("needsLoading");
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
