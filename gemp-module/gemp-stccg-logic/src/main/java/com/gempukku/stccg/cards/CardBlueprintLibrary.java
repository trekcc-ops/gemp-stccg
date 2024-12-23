package com.gempukku.stccg.cards;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintDeserializer;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCardDeserializer;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.common.JSONData;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.GameType;
import com.gempukku.stccg.game.ICallback;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class CardBlueprintLibrary {
    private static final Logger LOGGER = LogManager.getLogger(CardBlueprintLibrary.class);
    private final Map<String, CardBlueprint> _blueprints = new HashMap<>();
    private final Map<String, String> _blueprintMapping = new HashMap<>();
    private final Map<String, Set<String>> _alternateBlueprintMapping = new HashMap<>();
    private final Map<String, SetDefinition> _allSets = new LinkedHashMap<>();

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

        loadSetsWithCards(_cardPath);
        loadMappings();

        LOGGER.info("Unlocking blueprint library in constructor");
        collectionReady.release();
    }

    public void SubscribeToRefreshes(ICallback callback) {
        if(!_refreshCallbacks.contains(callback))
            _refreshCallbacks.add(callback);
    }

    public PhysicalCard createST1EPhysicalCard(ST1EGame game, String blueprintId, int cardId, String playerId)
            throws CardNotFoundException {
        CardBlueprint cardBlueprint = getCardBlueprint(blueprintId);
        Player owner = game.getPlayer(playerId);
        return cardBlueprint.createPhysicalCard(game, cardId, owner);
    }

    public PhysicalCard createST1EPhysicalCard(ST1EGame game, JsonNode node) throws CardNotFoundException {
        String blueprintId = node.get("blueprintId").textValue();
        int cardId = node.get("cardId").intValue();
        String playerId = node.get("owner").textValue();
        PhysicalCard newCard = createST1EPhysicalCard(game, blueprintId, cardId, playerId);
        PhysicalCardDeserializer.deserialize(newCard, node);
        return newCard;
    }


    public Map<String, SetDefinition> getSetDefinitions() {
        return Collections.unmodifiableMap(_allSets);
    }

    public void reloadAllDefinitions() {
        reloadSetsWithCards();
        reloadMappings();
        _refreshCallbacks.forEach(ICallback::Invoke);
    }

    private void reloadSetsWithCards() {
        try {
            collectionReady.acquire();
            loadSetsWithCards(_cardPath);
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

    private void loadSetsWithCards(File path) {
        if (path.isFile()) {
            loadSetWithCardsFromFile(path);
        }
        else if (path.isDirectory()) {
            for (File file : Objects.requireNonNull(path.listFiles())) {
                loadSetsWithCards(file);
            }
        }
    }

    private void loadSetWithCardsFromFile(File file) {
        if (JsonUtils.isNotAValidHJSONFile(file))
            return;
        try {
            JsonNode jsonNode = JsonUtils.readJsonFromFile(file);

            // Add set
            String setId = jsonNode.get("setId").textValue();
            String setName = jsonNode.get("setName").textValue();
            GameType gameType = BlueprintUtils.getEnum(GameType.class, jsonNode, "gameType");
            Set<String> flags = new HashSet<>();
            determineNeedsLoadingFlag(jsonNode, flags);
            SetDefinition setDefinition = new SetDefinition(setId, setName, gameType, flags);
            _allSets.put(setId, setDefinition);

            // Add cards
            JsonNode setCardsNode = jsonNode.get("cards");

            for (JsonNode cardNode : setCardsNode) {
                String blueprintId = cardNode.get("blueprintId").textValue();
                if (blueprintId == null || !blueprintId.startsWith(setId + "_"))
                    throw new InvalidCardDefinitionException(
                            "Card blueprintId " + blueprintId + " invalid for set " + setId);
                try {
                    final CardBlueprint cardBlueprint = loadCardFromDeserializer(blueprintId, gameType, cardNode);
                    _blueprints.put(blueprintId, cardBlueprint);
                    setDefinition.addCard(blueprintId, cardBlueprint.getRarity());
                } catch (Exception exp) {
                    _blueprintLoadErrorEncountered = true;
                    LOGGER.error("Unable to load card {}", blueprintId, exp);
                }
            }
        } catch (Exception exp) {
            _blueprintLoadErrorEncountered = true;
            String errorMessage = switch (exp) {
                case FileNotFoundException fileNotFoundException -> "Failed to find file";
                case IOException ioException -> "Error while loading file";
                default -> "Unexpected error while parsing file";
            };
            LOGGER.error("{} {}", errorMessage, file.getAbsolutePath(), exp);
        }
        LOGGER.debug("Loaded JSON card file {}", file.getName());
    }

    private void validateConsistency(CardBlueprint blueprint, GameType gameType) throws InvalidCardDefinitionException {
        if (blueprint.getTitle() == null)
            throw new InvalidCardDefinitionException("Card has to have a title");
        if (blueprint.getCardType() == null)
            throw new InvalidCardDefinitionException("Card has to have a type");
        if (blueprint.getUniqueness() == null)
            throw new InvalidCardDefinitionException("Card has to have a uniqueness");
        if (blueprint.getRarity() == null)
            throw new InvalidCardDefinitionException("Card has to have a rarity");

        if (blueprint.getCardType() == CardType.MISSION) {
            if (blueprint.getPropertyLogo() != null)
                throw new InvalidCardDefinitionException("Mission card should not have a property logo");
            if (blueprint.getLocation() == null && !blueprint.getTitle().equals("Space") &&
                    gameType != GameType.SECOND_EDITION)
                throw new InvalidCardDefinitionException("Mission card should have a location");
            if (blueprint.getQuadrant() == null)
                throw new InvalidCardDefinitionException("Mission card should have a quadrant");
        } else if (blueprint.getCardType() == CardType.TRIBBLE) {
            if (blueprint.getTribblePower() == null)
                throw new InvalidCardDefinitionException("Tribble card has to have a Tribble power");
            if (!Arrays.asList(1, 10, 100, 1000, 10000, 100000).contains(blueprint.getTribbleValue()))
                throw new InvalidCardDefinitionException("Tribble card does not have a valid Tribble value");
        } else if (blueprint.getPropertyLogo() == null && gameType != GameType.SECOND_EDITION)
            // Technically tribbles have property logos too, they're just never relevant
            throw new InvalidCardDefinitionException("Non-mission card has to have a property logo");
    }


    private CardBlueprint loadCardFromDeserializer(String blueprintId, GameType gameType, JsonNode cardNode)
            throws JsonProcessingException, InvalidCardDefinitionException {
        if (cardNode == null)
            throw new InvalidCardDefinitionException("Could not find node for blueprintId " + blueprintId);
        if (!cardNode.has("blueprintId"))
            ((ObjectNode) cardNode).put("blueprintId", blueprintId);
        else if (!cardNode.get("blueprintId").textValue().equals(blueprintId))
            throw new InvalidCardDefinitionException("Non-matching card blueprint property 'blueprintId' " +
                    cardNode.get("blueprintId").textValue() + " for blueprint " + blueprintId);
        CardBlueprint result = _objectMapper.readValue(cardNode.toString(), CardBlueprint.class);
        validateConsistency(result, gameType);
        return result;
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

    private Map<String, JSONData.ErrataInfo> errataMappings = null;
    public Map<String, JSONData.ErrataInfo> getErrata() {
        try {
            if(errataMappings == null) {
                collectionReady.acquire();
                errataMappings = new HashMap<>();
                for (String id : _blueprints.keySet()) {
                    var parts = id.split("_");
                    int setID = Integer.parseInt(parts[0]);
                    String cardID = parts[1];
                    JSONData.ErrataInfo card;
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
                        card = new JSONData.ErrataInfo();
                        card.BaseID = base;
                        card.Name = blueprint.getFullName();
                        card.LinkText = blueprint.getCardLink();
                        card.ErrataIDs = new HashMap<>();
                        errataMappings.put(base, card);
                    }

                    card.ErrataIDs.put(JSONData.ErrataInfo.PC_Errata, id);
                }

                collectionReady.release();
            }
            return errataMappings;
        } catch (InterruptedException exp) {
            throw new RuntimeException("CardBlueprintLibrary.getErrata() interrupted: ", exp);
        }
    }

    public boolean hasAlternateInSet(String blueprintId, String setId) {
        try {
            collectionReady.acquire();
            var alternatives = _alternateBlueprintMapping.get(blueprintId);
            collectionReady.release();

            if (alternatives != null)
                for (String alternative : alternatives)
                    if (alternative.startsWith(setId + "_"))
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
        String blueprintId1 = stripBlueprintModifiers(blueprintId);
        CardBlueprint bp = null;

        try {
            collectionReady.acquire();
            if (_blueprints.containsKey(blueprintId1)) {
                bp = _blueprints.get(blueprintId1);
            }
            collectionReady.release();

            if(bp != null)
                return bp;

        } catch (InterruptedException exp) {
            throw new RuntimeException("CardBlueprintLibrary.getCardBlueprint() interrupted: ", exp);
        }

        // Throw exception if card not found in blueprints
        throw new CardNotFoundException("Unable to find card blueprint for id '" + blueprintId1 + "'");
    }

    public String stripBlueprintModifiers(String blueprintId) {
        String blueprintId1 = blueprintId;
        if (blueprintId1.endsWith("*"))
            blueprintId1 = blueprintId1.substring(0, blueprintId1.length() - 1);
        if (blueprintId1.endsWith("T"))
            blueprintId1 = blueprintId1.substring(0, blueprintId1.length() - 1);
        return blueprintId1;
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

    public SetDefinition getSetDefinition(String setId) {
        Map<String, SetDefinition> sets = Collections.unmodifiableMap(_allSets);
        return sets.get(setId);
    }
}