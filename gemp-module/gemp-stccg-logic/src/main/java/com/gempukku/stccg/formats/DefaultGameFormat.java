package com.gempukku.stccg.formats;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.SetDefinition;
import com.gempukku.stccg.common.filterable.GameType;
import com.gempukku.stccg.common.filterable.SubDeck;

import java.util.*;

@JsonPropertyOrder({ "gameType", "code", "name", "order", "discardPileIsPublic", "playtest", "minimumDrawDeckSize",
        "maximumSeedDeckSize", "missions", "maximumSameName", "hall"
})
public class DefaultGameFormat implements GameFormat {

    private static final int DEFAULT_MIN_VALUE = 999;

    @JsonProperty("name")
    private final String _name;

    @JsonProperty("code")
    private final String _code;

    @JsonProperty("order")
    private final int _order;

    @JsonProperty("hall")
    private final boolean _hallVisible;

    @JsonProperty("maximumSameName")
    private final int _maximumSameName;

    @JsonProperty("discardPileIsPublic")
    private final boolean _discardPileIsPublic;
    @JsonProperty("minimumDrawDeckSize")
    private final int _minimumDrawDeckSize;
    @JsonProperty("maximumSeedDeckSize")
    private final int _maximumSeedDeckSize;
    private final int _missions; // If missions is -1, there is no restriction on the number of missions
    private final List<String> _bannedCards = new ArrayList<>();
    private final List<String> _restrictedCards = new ArrayList<>();
    private final List<String> _validCards = new ArrayList<>();
    private final List<String> _validSets = new ArrayList<>();
    private final List<String> _restrictedCardNames = new ArrayList<>();

    private final boolean _isPlaytest;
    private final List<String> _limit2Cards = new ArrayList<>();
    private final List<String> _limit3Cards = new ArrayList<>();
    private final Map<String,String> _errataCardMap = new TreeMap<>();
    private final GameType _gameType;
    private final int _minPlanetMissions;
    private final int _minSpaceMissions;
    private final boolean _misSeedsAllowed;

    @JsonCreator
    private DefaultGameFormat(@JacksonInject CardBlueprintLibrary blueprintLibrary,
                              @JsonProperty("gameType") String gameType,
                              @JsonProperty("excludedSets") @JsonSetter(nulls = Nulls.AS_EMPTY)
                                  @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                  List<String> excludedSets,
                              @JsonProperty("name") String name,
                              @JsonProperty("code") String code,
                              @JsonProperty("minimumDrawDeckSize") Integer minDrawDeck,
                              @JsonProperty("maximumSeedDeckSize") Integer maxSeedDeck,
                              @JsonProperty("bannedCards") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> bannedCards,
                              @JsonProperty("missions") Integer missions,
                              @JsonProperty("hall") Boolean hallVisible,
                              @JsonProperty("minPlanetMissions") Integer minPlanetMissions,
                              @JsonProperty("minSpaceMissions") Integer minSpaceMissions,
                              @JsonProperty("misSeedsAllowed") Boolean misSeedsAllowed) {
        _name = name;
        _code = code;
        _order = 1000;
        _minimumDrawDeckSize = Objects.requireNonNullElse(minDrawDeck, 60);
        _minPlanetMissions = Objects.requireNonNullElse(minPlanetMissions, DEFAULT_MIN_VALUE);
        _minSpaceMissions = Objects.requireNonNullElse(minSpaceMissions, DEFAULT_MIN_VALUE);
        _misSeedsAllowed = Objects.requireNonNullElse(misSeedsAllowed, true);
        _maximumSameName = DEFAULT_MIN_VALUE;
        _discardPileIsPublic = false;
        _isPlaytest = false;
        _hallVisible = Objects.requireNonNullElse(hallVisible, true);
        _missions = Objects.requireNonNullElse(missions, 6);
        _maximumSeedDeckSize = Objects.requireNonNullElse(maxSeedDeck, 30);
        _gameType =
                Enum.valueOf(GameType.class, gameType.toUpperCase().replaceAll("[ '\\-.]", "_"));
        for (SetDefinition set : blueprintLibrary.getSetDefinitions().values()) {
            if (set.getGameType() == _gameType && !excludedSets.contains(set.getSetId())) {
                addValidSet(Integer.parseInt(set.getSetId()));
            }
        }
        if (!bannedCards.isEmpty()) {
            bannedCards.forEach(this::addBannedCard);
        }
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String getCode() {
        return _code;
    }
    @JsonProperty("order")
    @Override
    public int getOrder() {
        return _order;
    }
    public boolean hallVisible() {
        return _hallVisible;
    }

    @Override
    public boolean discardPileIsPublic() { return _discardPileIsPublic; }

    @Override
    public boolean isPlaytest() {
        return _isPlaytest;
    }

    public Map<String, String> getValidSetsAndTheirCards(CardBlueprintLibrary library) {
            // For sending to CardFilter
        Map<String, String> sets = new LinkedHashMap<>();
        StringJoiner joiner = new StringJoiner(",");
        for (String validSet : _validSets) {
            joiner.add(validSet);
        }
        String allSetsString = joiner.toString();
        sets.put(allSetsString, "All " + _name + " sets");
        sets.put("disabled", "disabled");
        Map<String, SetDefinition> librarySets = library.getSetDefinitions();
        for (String setId : _validSets) {
            sets.put(setId, librarySets.get(setId).getSetName());
        }
        return sets;
    }

    @JsonIgnore
    @Override
    public List<String> getBannedCards() {
        return Collections.unmodifiableList(_bannedCards);
    }

    @JsonIgnore
    @Override
    public List<String> getRestrictedCardNames() {
        return Collections.unmodifiableList(_restrictedCardNames);
    }

    @JsonIgnore
    @Override
    public List<String> getValidCards() {
        return Collections.unmodifiableList(_validCards);
    }

    @JsonIgnore
    @Override
    public Map<String,String> getErrataCardMap() {
        return Collections.unmodifiableMap(_errataCardMap);
    }

    @JsonIgnore
    @Override
    public int getHandSize() {
        return 7;
    }

    public void addBannedCard(String baseBlueprintId) {
        if (baseBlueprintId.contains("-")) {
            String[] parts = baseBlueprintId.split("_");
            String set = parts[0];
            int from = Integer.parseInt(parts[1].split("-")[0]);
            int to = Integer.parseInt(parts[1].split("-")[1]);
            for (int i = from; i <= to; i++)
                _bannedCards.add(set + "_" + i);
        } else
            _bannedCards.add(baseBlueprintId);
    }

    public void addRestrictedCard(String baseBlueprintId) {
        if (baseBlueprintId.contains("-")) {
            String[] parts = baseBlueprintId.split("_");
            String set = parts[0];
            int from = Integer.parseInt(parts[1].split("-")[0]);
            int to = Integer.parseInt(parts[1].split("-")[1]);
            for (int i = from; i <= to; i++)
                _restrictedCards.add(set + "_" + i);
        } else
            _restrictedCards.add(baseBlueprintId);
    }

    public void addValidCard(String baseBlueprintId) {
        if (baseBlueprintId.contains("-")) {
            String[] parts = baseBlueprintId.split("_");
            String set = parts[0];
            int from = Integer.parseInt(parts[1].split("-")[0]);
            int to = Integer.parseInt(parts[1].split("-")[1]);
            for (int i = from; i <= to; i++)
                _validCards.add(set + "_" + i);
        } else
            _validCards.add(baseBlueprintId);
    }

    private void addValidSet(int setNo) {
        _validSets.add(String.valueOf(setNo));
    }

    //Additional Hobbit Draft card lists
    public void addLimit2Card(String baseBlueprintId) {
        _limit2Cards.add(baseBlueprintId);
    }

    public void addLimit3Card(String baseBlueprintId) {
        _limit3Cards.add(baseBlueprintId);
    }

    public void addRestrictedCardName(String cardName) {
        _restrictedCardNames.add(cardName);
    }

    private void addErrataSet(CardBlueprintLibrary library, int setID) throws InvalidPropertiesFormatException {
        //Valid errata sets:
        // 50-69 are live errata versions of sets 0-19
        // 70-89 are playtest errata versions of sets 0-19
        // 150-199 are playtest versions of sets V0-V49
        if(setID < 50 || (setID >= 90 && setID <= 149) || setID > 199)
            throw new InvalidPropertiesFormatException("Errata sets must be 50-69, 70-89, or 150-159.  Received: " + setID);

        //maps 69 to 19, and also 151 to 101
        int ogSet = setID - 50;
        //playtest sets are offset by 20 more
        if(setID >= 70 && setID <=89)
            ogSet = setID - 70;

        var cards = library.getBaseCards().keySet().stream().filter(x -> x.startsWith(String.valueOf(setID))).toList();
        for(String errataBP : cards) {
            String cardID = errataBP.split("_")[1];

            addCardErrata(ogSet + "_" + cardID, errataBP);
        }
    }
    public void addCardErrata(String baseBlueprintId, String errataBaseBlueprint) {
        _errataCardMap.put(baseBlueprintId, errataBaseBlueprint);
    }

    public boolean isCardAllowedInFormat(CardBlueprint blueprint, CardBlueprintLibrary library) {
        String blueprintId = library.getBaseBlueprintId(blueprint.getBlueprintId());
        try {
            library.getCardBlueprint(blueprintId);
            if (_validCards.contains(blueprintId) || _errataCardMap.containsValue(blueprintId)) {
                return true;
            } else if (!_validSets.isEmpty() && !isValidInSets(library, blueprintId)) {
                return false;
            }

            // Banned cards
            Set<String> allAlternates = library.getAllAlternates(blueprintId);
            for (String bannedBlueprintId : _bannedCards) {
                if (bannedBlueprintId.equals(blueprintId) ||
                        (allAlternates != null && allAlternates.contains(bannedBlueprintId))) {
                    return false;
                }
            }

            // Errata
            for (String originalBlueprintId : _errataCardMap.keySet()) {
                if (originalBlueprintId.equals(blueprintId) ||
                        (allAlternates != null && allAlternates.contains(originalBlueprintId))) {
                    return false;
                }
            }

        } catch (CardNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public String validateCard(CardBlueprintLibrary library, String blueprintId) {
        blueprintId = library.getBaseBlueprintId(blueprintId);
        try {
            library.getCardBlueprint(blueprintId);
            if (_validCards.contains(blueprintId) || _errataCardMap.containsValue(blueprintId))
                return null;

            if (!_validSets.isEmpty() && !isValidInSets(library, blueprintId))
                return "Deck contains card not from valid set: " + library.getCardFullName(blueprintId);

            // Banned cards
            Set<String> allAlternates = library.getAllAlternates(blueprintId);
            for (String bannedBlueprintId : _bannedCards) {
                if (bannedBlueprintId.equals(blueprintId) ||
                        (allAlternates != null && allAlternates.contains(bannedBlueprintId)))
                    return "Deck contains a copy of an X-listed card: " +
                            library.getCardBlueprint(bannedBlueprintId).getFullName();
            }

            // Errata
            for (String originalBlueprintId : _errataCardMap.keySet()) {
                if (originalBlueprintId.equals(blueprintId) ||
                        (allAlternates != null && allAlternates.contains(originalBlueprintId)))
                    return "Deck contains cards that have been replaced with errata: " +
                            library.getCardBlueprint(originalBlueprintId).getFullName();
            }

        } catch (CardNotFoundException e) {
            return null;
        }

        return null;
    }


    private boolean isValidInSets(CardBlueprintLibrary library, String blueprintId)  {
        for (String setId : _validSets)
            if (blueprintId.startsWith(setId + "_")
                    || library.hasAlternateInSet(blueprintId, setId))
                return true;
        return false;
    }

    @Override
    public CardDeck applyErrata(CardBlueprintLibrary library, CardDeck deck) {
        CardDeck deckWithErrata = new CardDeck(deck);
        Map<SubDeck, List<String>> newSubDecks = deckWithErrata.getSubDecks();
        for (Map.Entry<SubDeck, List<String>> entry : newSubDecks.entrySet()) {
            List<String> newList = new ArrayList<>();
            List<String> v = entry.getValue();
            for (String item : v) {
                newList.add(applyErrata(library, item));
            }
            newSubDecks.put(entry.getKey(), newList);
        }
        deckWithErrata.setSubDecks(newSubDecks);
        return deckWithErrata;
    }

    private String applyErrata(CardBlueprintLibrary library, String original) {
        var base = library.getBaseBlueprintId(original);
        var errata = _errataCardMap.getOrDefault(base, base);
        if (original.endsWith("*") && !errata.endsWith("*")) {
            errata += "*";
        }

        if(errata.equals(base))
            return original;

        return errata;
    }


    public GameType getGameType() { return _gameType; }

    @JsonProperty("gameType")
    private String getGameTypeName() { return _gameType.getHumanReadable(); }

    @Override
    public int getMissions() {
        return _missions;
    }

    public String toString() {
        return getName();
    }

    @JsonIgnore
    public int getMinimumDrawDeckSize() {
        return _minimumDrawDeckSize;
    }

    @JsonIgnore
    public int getMaximumSeedDeckSize() {
        return _maximumSeedDeckSize;
    }

    @Override
    public Integer getSameNameLimit() {
        return _maximumSameName;
    }

}