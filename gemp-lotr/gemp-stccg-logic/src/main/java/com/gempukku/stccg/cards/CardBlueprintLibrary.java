package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.common.JSONDefs;
import com.gempukku.stccg.common.JsonUtils;
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
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Semaphore;

public class CardBlueprintLibrary {
    private static final Logger LOGGER = LogManager.getLogger(CardBlueprintLibrary.class);

    private final String[] _packageNames =
            new String[]{
                    "", ".dwarven", ".dunland", ".elven", ".fallenRealms", ".gandalf", ".gollum", ".gondor", ".isengard", ".men", ".orc",
                    ".raider", ".rohan", ".moria", ".wraith", ".sauron", ".shire", ".site", ".uruk_hai",

                    //Additional Hobbit Draft packages
                    ".esgaroth", ".gundabad", ".smaug", ".spider", ".troll"
            };
    private final Map<String, CardBlueprint> _blueprints = new HashMap<>();
    private final Map<String, String> _blueprintMapping = new HashMap<>();
    private final Map<String, Set<String>> _fullBlueprintMapping = new HashMap<>();
    private final Map<String, SetDefinition> _allSets = new LinkedHashMap<>();

    private final CardBlueprintFactory cardBlueprintBuilder = new CardBlueprintFactory();

    private final Semaphore collectionReady = new Semaphore(1);
    private final File _cardPath;
    private final File _mappingsPath;
    private final File _setDefsPath;

    private final List<ICallback> _refreshCallbacks = new ArrayList<>();

    public CardBlueprintLibrary() {
        this(AppConfig.getCardsPath(), AppConfig.getMappingsPath(), AppConfig.getSetDefinitionsPath());
    }

    public CardBlueprintLibrary(File cardsPath, File mappingsPath, File setDefinitionPath) {
        _cardPath = cardsPath;
        _mappingsPath = mappingsPath;
        _setDefsPath = setDefinitionPath;
        LOGGER.info("Locking blueprint library in constructor");
        //This will be released after the library has been init'd; until then all functional uses should block
        collectionReady.acquireUninterruptibly();
        LOGGER.info("Unlocking blueprint library in constructor");

        loadSets();
        loadMappings();
        loadCards(_cardPath, true);
        cacheAllJavaBlueprints();
        collectionReady.release();
    }

    public void SubscribeToRefreshes(ICallback callback) {
        if(_refreshCallbacks.contains(callback))
            return;

        _refreshCallbacks.add(callback);

    }

    public boolean UnsubscribeFromRefreshes(ICallback callback) {
        if(!_refreshCallbacks.contains(callback))
            return false;

        _refreshCallbacks.remove(callback);

        return true;
    }

    public Map<String, SetDefinition> getSetDefinitions() {
        return Collections.unmodifiableMap(_allSets);
    }

    public void reloadAllDefinitions() {
        reloadSets();
        reloadMappings();
        reloadCards();
        errataMappings = null;
        getErrata();

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
        try {
            collectionReady.acquire();
            loadCards(_cardPath, false);
            collectionReady.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadSets() {
        try {
            final InputStreamReader reader = new InputStreamReader(new FileInputStream(_setDefsPath), StandardCharsets.UTF_8);
            try {
                String json = JsonValue.readHjson(reader).toString();
                JSONParser parser = new JSONParser();
                JSONArray object = (JSONArray) parser.parse(json);
                for (Object setDefinitionObj : object) {
                    JSONObject setJsonDef = (JSONObject) setDefinitionObj;

                    String setId = (String) setJsonDef.get("setId");
                    String setName = (String) setJsonDef.get("setName");
//                    String rarityFile = (String) setJsonDef.get("rarityFile");

                    Set<String> flags = new HashSet<>();
                    determineOriginalSetFlag(setJsonDef, flags);
                    determineMerchantableFlag(setJsonDef, flags);
                    determineNeedsLoadingFlag(setJsonDef, flags);

                    DefaultSetDefinition setDefinition = new DefaultSetDefinition(setId, setName, flags);

/*                    BufferedReader bufferedReader =
                            new BufferedReader(new InputStreamReader(AppConfig.getResourceStream(
                                    "rarities/" + rarityFile), StandardCharsets.UTF_8));
                    try {
                        String line;

                        while ((line = bufferedReader.readLine()) != null) {
                            String blueprintId = setId + "_" + line.substring(setId.length() + 1);
                            if (!line.startsWith(setId))
                                throw new IllegalStateException("Seems the setDefinition is for some other set");
                            String cardRarity = line.substring(setId.length(), setId.length() + 1);
                            setDefinition.addCard(blueprintId, cardRarity);
                        }
                    } finally {
                        IOUtils.closeQuietly(bufferedReader);
                    } */

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
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(_mappingsPath), StandardCharsets.UTF_8))) {
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
            final JSONObject cardsFile = (JSONObject) parser.parse(json);
            final Set<Map.Entry<String, JSONObject>> cardsInFile = cardsFile.entrySet();
            for (Map.Entry<String, JSONObject> cardEntry : cardsInFile) {
                String blueprintId = cardEntry.getKey();
                if (validateNew)
                    if (_blueprints.containsKey(blueprintId))
                        LOGGER.error(blueprintId + " - Replacing existing card definition!");
                final JSONObject cardDefinition = cardEntry.getValue();
                try {
                    final CardBlueprint cardBlueprint = cardBlueprintBuilder.buildFromJson(blueprintId, cardDefinition);
                    _blueprints.put(blueprintId, cardBlueprint);
                } catch (InvalidCardDefinitionException exp) {
                    LOGGER.error("Unable to load card " + blueprintId, exp);
                }
            }
        } catch (FileNotFoundException exp) {
            LOGGER.error("Failed to find file " + file.getAbsolutePath(), exp);
        } catch (IOException exp) {
            LOGGER.error("Error while loading file " + file.getAbsolutePath(), exp);
        } catch (ParseException exp) {
            LOGGER.error("Failed to parse file " + file.getAbsolutePath(), exp);
        }
        catch (Exception exp) {
            LOGGER.error("Unexpected error while parsing file " + file.getAbsolutePath(), exp);
        }
        LOGGER.debug("Loaded JSON card file " + file.getName());
    }

    private void cacheAllJavaBlueprints() {
        for (SetDefinition setDefinition : _allSets.values()) {
            if (setDefinition.hasFlag("needsLoading")) {
                LOGGER.debug("Loading Java cards for set " + setDefinition.getSetId());
                final Set<String> allCards = setDefinition.getAllCards();
                for (String blueprintId : allCards) {
                    if (getBaseBlueprintId(blueprintId).equals(blueprintId)) {
                        if (!_blueprints.containsKey(blueprintId)) {
                            try {
                                // Ensure it's loaded
                                CardBlueprint blueprint = findJavaBlueprint(blueprintId);
                                _blueprints.put(blueprintId, blueprint);
                            } catch (CardNotFoundException exp) {
                                throw new RuntimeException("Unable to start the server, due to invalid (missing) card definition - " + blueprintId);
                            }
                        }
                    }
                }
            }
            LOGGER.debug("Java cards for set " + setDefinition.getSetId() + " successfully loaded");
        }
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

    public CardBlueprint getCardBlueprint(CardItem item) throws CardNotFoundException {
        return getCardBlueprint(item.getBlueprintId());
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

            return findJavaBlueprint(blueprintId);
        } catch (InterruptedException exp) {
            throw new RuntimeException("CardBlueprintLibrary.getCardBlueprint() interrupted: ", exp);
        }
    }

    public String stripBlueprintModifiers(String blueprintId) {
        if (blueprintId.endsWith("*"))
            blueprintId = blueprintId.substring(0, blueprintId.length() - 1);
        if (blueprintId.endsWith("T"))
            blueprintId = blueprintId.substring(0, blueprintId.length() - 1);
        return blueprintId;
    }

    private CardBlueprint findJavaBlueprint(String blueprintId) throws CardNotFoundException {
        if (_blueprintMapping.containsKey(blueprintId))
            return getCardBlueprint(_blueprintMapping.get(blueprintId));

        String[] blueprintParts = blueprintId.split("_");

        String setNumber = blueprintParts[0];
        String cardNumber = blueprintParts[1];

        for (String packageName : _packageNames) {
            CardBlueprint blueprint;
            try {
                blueprint = tryLoadingFromPackage(packageName, setNumber, cardNumber);
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {
                throw new CardNotFoundException(blueprintId);
            }
            if (blueprint != null)
                return blueprint;
        }

        throw new CardNotFoundException(blueprintId);
    }

    private CardBlueprint tryLoadingFromPackage(String packageName, String setNumber, String cardNumber) throws IllegalAccessException, InstantiationException, NoSuchMethodException {
        try {
            Class clazz = Class.forName("com.gempukku.stccg.cards.set" + setNumber + packageName + ".Card" + setNumber + "_" + normalizeId(cardNumber));
            return (CardBlueprint) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InvocationTargetException e) {
            // Ignore
            return null;
        }
    }

    private String normalizeId(String blueprintPart) {
        int id = Integer.parseInt(blueprintPart);
        if (id < 10)
            return "00" + id;
        else if (id < 100)
            return "0" + id;
        else
            return String.valueOf(id);
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

}
