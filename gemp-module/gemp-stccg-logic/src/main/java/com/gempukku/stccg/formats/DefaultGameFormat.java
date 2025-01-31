package com.gempukku.stccg.formats;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.SetDefinition;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.JSONData;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.GameType;
import com.gempukku.stccg.common.filterable.SubDeck;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultGameFormat implements GameFormat {

    private final CardBlueprintLibrary _library;

    @JsonProperty("name")
    private final String _name;

    @JsonProperty("code")
    private final String _code;

    @JsonProperty("order")
    private final int _order;
    private final boolean _hallVisible;
    private final int _maximumSameName;
    private final boolean _mulliganRule;
    private final boolean _hasRuleOfFour;
    private final boolean _discardPileIsPublic;
    private final int _minimumDrawDeckSize;
    private final int _maximumSeedDeckSize;
    private final int _missions; // If missions is -1, there is no restriction on the number of missions
    private final List<String> _bannedCards = new ArrayList<>();
    private final List<String> _restrictedCards = new ArrayList<>();
    private final List<String> _validCards = new ArrayList<>();
    private final List<String> _validSets = new ArrayList<>();
    private final List<String> _restrictedCardNames = new ArrayList<>();

    @JsonProperty("surveyUrl")
    private final String _surveyUrl;
    private final boolean _isPlaytest;
    private final boolean _noShuffle;
    private final List<String> _limit2Cards = new ArrayList<>();
    private final List<String> _limit3Cards = new ArrayList<>();
    private final Map<String,String> _errataCardMap = new TreeMap<>();
    private final boolean _firstPlayerFixed;
    private final GameType _gameType;

    DefaultGameFormat(CardBlueprintLibrary library, JSONData.Format def)
            throws InvalidPropertiesFormatException, JsonParseException {
        _library = library;
        _name = def.name;
        _code = def.code;
        _order = def.order;
        _surveyUrl = def.surveyUrl;
        _minimumDrawDeckSize = def.minimumDrawDeckSize;
        _maximumSameName = def.maximumSameName;
        _mulliganRule = def.mulliganRule;
        _hasRuleOfFour = def.ruleOfFour;
        _discardPileIsPublic = def.discardPileIsPublic;
        _isPlaytest = def.playtest;
        _hallVisible = def.hall;
        _missions = def.missions;
        _maximumSeedDeckSize = def.maximumSeedDeckSize;
        _noShuffle = def.noShuffle;
        _firstPlayerFixed = def.firstPlayerFixed;
        _gameType = JsonUtils.getEnum(GameType.class, def.gameType);


        if (def.set == null) {
            for (SetDefinition set : library.getSetDefinitions().values()) {
                if (set.getGameType() == _gameType) {
                    addValidSet(Integer.parseInt(set.getSetId()));
                }
            }
        } else {
            def.set.forEach(this::addValidSet);
        }

        if(def.banned != null)
            def.banned.forEach(this::addBannedCard);
        if(def.restricted != null)
            def.restricted.forEach(this::addRestrictedCard);
        if(def.valid != null)
            def.valid.forEach(this::addValidCard);
        if(def.limit2 != null)
            def.limit2.forEach(this::addLimit2Card);
        if(def.limit3 != null)
            def.limit3.forEach(this::addLimit3Card);
        if(def.restrictedName != null)
            def.restrictedName.forEach(this::addRestrictedCardName);
        if(def.errataSets != null) {
            for (Integer errataSet : def.errataSets) {
                addErrataSet(errataSet);
            }
        }
        if(def.errata != null)
            def.errata.forEach(this::addCardErrata);
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String getCode() {
        return _code;
    }
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

    public List<String> getValidSetIdsAsStrings() {
        return new LinkedList<>(_validSets);
    }

    @Override
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

    @Override
    public List<String> getBannedCards() {
        return Collections.unmodifiableList(_bannedCards);
    }

    @Override
    public List<String> getRestrictedCardNames() {
        return Collections.unmodifiableList(_restrictedCardNames);
    }

    @Override
    public List<String> getValidCards() {
        return Collections.unmodifiableList(_validCards);
    }

    @Override
    public Map<String,String> getErrataCardMap() {
        return Collections.unmodifiableMap(_errataCardMap);
    }

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

    private void addErrataSet(int setID) throws InvalidPropertiesFormatException {
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

        var cards = _library.getBaseCards().keySet().stream().filter(x -> x.startsWith(String.valueOf(setID))).toList();
        for(String errataBP : cards) {
            String cardID = errataBP.split("_")[1];

            addCardErrata(ogSet + "_" + cardID, errataBP);
        }
    }
    public void addCardErrata(String baseBlueprintId, String errataBaseBlueprint) {
        _errataCardMap.put(baseBlueprintId, errataBaseBlueprint);
    }

    @Override
    public String validateCard(String blueprintId) {
        blueprintId = _library.getBaseBlueprintId(blueprintId);
        try {
            _library.getCardBlueprint(blueprintId);
            if (_validCards.contains(blueprintId) || _errataCardMap.containsValue(blueprintId))
                return null;

            if (!_validSets.isEmpty() && !isValidInSets(blueprintId))
                return "Deck contains card not from valid set: " + _library.getCardFullName(blueprintId);

            // Banned cards
            Set<String> allAlternates = _library.getAllAlternates(blueprintId);
            for (String bannedBlueprintId : _bannedCards) {
                if (bannedBlueprintId.equals(blueprintId) ||
                        (allAlternates != null && allAlternates.contains(bannedBlueprintId)))
                    return "Deck contains a copy of an X-listed card: " +
                            _library.getCardBlueprint(bannedBlueprintId).getFullName();
            }

            // Errata
            for (String originalBlueprintId : _errataCardMap.keySet()) {
                if (originalBlueprintId.equals(blueprintId) ||
                        (allAlternates != null && allAlternates.contains(originalBlueprintId)))
                    return "Deck contains cards that have been replaced with errata: " +
                            _library.getCardBlueprint(originalBlueprintId).getFullName();
            }

        } catch (CardNotFoundException e) {
            return null;
        }

        return null;
    }

    private boolean isValidInSets(String blueprintId)  {
        for (String setId : _validSets)
            if (blueprintId.startsWith(setId + "_")
                    || _library.hasAlternateInSet(blueprintId, setId))
                return true;
        return false;
    }

    @Override
    public List<String> validateDeck(CardDeck deck) {
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> errataResult = new ArrayList<>();
        String valid;

        // Additional deck checks in validateDeckStructure
        valid = validateDeckStructure(deck);
        if(!valid.isEmpty()) {
            result.add(valid);
        }

        String newLine;
        for (String card : deck.getAllCards()){
            newLine = validateCard(card);
            if(newLine == null || newLine.isEmpty())
                continue;

            if (newLine.toLowerCase().contains("errata")) {
                errataResult.add(newLine);
            } else {
                result.add(newLine);
            }
        }

        // Card count in deck
        Map<String, Integer> cardCountByName = new HashMap<>();
        Map<String, Integer> cardCountByBaseBlueprintId = new HashMap<>();

        for (String blueprintId : deck.getAllCards())
            try {
                processCardCounts(blueprintId, cardCountByName, cardCountByBaseBlueprintId);
            } catch(CardNotFoundException exp) {
                result.add("Deck contains card of invalid blueprintId '" + blueprintId + "'");
            }

        for (Map.Entry<String, Integer> count : cardCountByName.entrySet()) {
            if (count.getValue() > _maximumSameName) {
                result.add("Deck contains more of the same card than allowed - " + count.getKey() + " (" + count.getValue() + ">" + _maximumSameName + "): " + count.getKey());
            }
        }

        try {
            // Restricted cards
            for (String blueprintId : _restrictedCards) {
                Integer count = cardCountByBaseBlueprintId.get(blueprintId);
                if (count != null && count > 1) {
                    result.add("Deck contains more than one copy of an R-listed card: " +
                            _library.getCardFullName(blueprintId));
                }
            }

            // New Hobbit Draft restrictions
            for (String blueprintId : _limit2Cards) {
                Integer count = cardCountByBaseBlueprintId.get(blueprintId);
                if (count != null && count > 2)
                    result.add("Deck contains more than two copies of a 2x limited card: " +
                            _library.getCardFullName(blueprintId));
            }
            for (String blueprintId : _limit3Cards) {
                Integer count = cardCountByBaseBlueprintId.get(blueprintId);
                if (count != null && count > 3)
                    result.add("Deck contains more than three copies of a 3x limited card: " +
                            _library.getCardFullName(blueprintId));
            }
        }
        catch(CardNotFoundException ex)
        {
            //By this point all the cards in the deck have been pulled from the blueprint library multiple times, and
            // adding the error to the list is just going to add more spam for no reason.
        }

        for (String restrictedCardName : _restrictedCardNames) {
            Integer count = cardCountByName.get(restrictedCardName);
            if (count != null && count > 1)
                result.add("Deck contains more than one copy of a card restricted by name: " + restrictedCardName);
        }

        result.addAll(errataResult);

        return result.stream()
                .filter(x -> x != null && !x.isEmpty())
                .collect(Collectors.toList());

    }
    
    @Override
    public CardDeck applyErrata(CardDeck deck) {
        CardDeck deckWithErrata = new CardDeck(deck);
        Map<SubDeck, List<String>> newSubDecks = deckWithErrata.getSubDecks();
        newSubDecks.forEach((k, v) -> v.replaceAll(this::applyErrata));
        deckWithErrata.setSubDecks(newSubDecks);
        return deckWithErrata;
    }

    @Override
    public String applyErrata(String original) {
        var base = _library.getBaseBlueprintId(original);
        var errata = _errataCardMap.getOrDefault(base, base);
        if (original.endsWith("*") && !errata.endsWith("*")) {
            errata += "*";
        }

        if(errata.equals(base))
            return original;

        return errata;
    }

    private String validateDeckStructure(CardDeck deck) {
        StringBuilder result = new StringBuilder();
        int drawDeckSize = deck.getSubDeck(SubDeck.DRAW_DECK).size();
        if (drawDeckSize < _minimumDrawDeckSize) {
            result.append("Draw deck contains below minimum number of cards: ")
                    .append(drawDeckSize).append("<").append(_minimumDrawDeckSize).append(".\n");
        }
        if (_gameType == GameType.FIRST_EDITION) {
            int seedDeckSize = deck.getSubDeck(SubDeck.SEED_DECK).size();
            if (seedDeckSize > _maximumSeedDeckSize) {
                result.append("Seed deck contains more than maximum number of cards: ")
                        .append(seedDeckSize).append(">").append(".\n");
            }
            result.append(validateMissionsPile(deck));
        }
        return result.toString();
    }

    private String validateMissionsPile(CardDeck deck) {
        StringBuilder result = new StringBuilder();
        List<String> missionsPile = deck.getSubDeck(SubDeck.MISSIONS);
        if (_missions > 0 && missionsPile.size() != _missions) {
            result.append("Deck must contain exactly ").append(_missions).append(" missions").append(".\n");
        }
        List<String> uniqueLocations = new LinkedList<>();
        for (String blueprintId : missionsPile) {
            try {
                CardBlueprint blueprint = _library.getCardBlueprint(blueprintId);
                if (blueprint.getCardType() != CardType.MISSION)
                    result.append("Missions pile contains non-mission card: ").append(blueprint.getTitle()).append(".\n");
                else if (!blueprint.isUniversal()) {
                    uniqueLocations.add(blueprint.getLocation());
                }
            } catch(CardNotFoundException exp) {
                result.append("Deck contains card with no valid blueprint: ").append(blueprintId);
            }
        }
        List<String> distinctUniqueLocations = uniqueLocations.stream().distinct().toList();
        for (String location : distinctUniqueLocations) {
            int locationCount = Collections.frequency(uniqueLocations, location);
            if (locationCount > 1)
                result.append("Deck has ").append(locationCount).append(" unique missions at location: ").append(location);
        }
        return result.toString();
    }


    private void processCardCounts(String blueprintId, Map<String, Integer> cardCountByName,
                                   Map<String, Integer> cardCountByBaseBlueprintId) throws CardNotFoundException {
        increaseCount(cardCountByName, _library.getCardBlueprint(blueprintId).getTitle());
        increaseCount(cardCountByBaseBlueprintId, _library.getBaseBlueprintId(blueprintId));
    }

    public GameType getGameType() { return _gameType; }

    private void increaseCount(Map<String, Integer> counts, String name) {
        counts.merge(name, 1, Integer::sum);
    }

    @Override
    public JSONData.Format Serialize() {
        return new JSONData.Format() {{
            code = _code;
            name = _name;
            order = _order;
            surveyUrl = _surveyUrl;
            ruleOfFour = _hasRuleOfFour;
            discardPileIsPublic = _discardPileIsPublic;
            playtest = _isPlaytest;
            minimumDrawDeckSize = _minimumDrawDeckSize;
            maximumSameName = _maximumSameName;
            mulliganRule = _mulliganRule;
            set = null;
            banned = null;
            restricted = null;
            valid = null;
            limit2 = null;
            limit3 = null;
            restrictedName = null;
            errataSets = null;
            errata = null;
            hall = _hallVisible;
            noShuffle = _noShuffle;
            firstPlayerFixed = _firstPlayerFixed;
            gameType = _gameType.getHumanReadable();
        }};
    }

    @Override
    public int getMissions() {
        return _missions;
    }

    @Override
    public boolean hasFixedPlayerOrder() {
        return _firstPlayerFixed;
    }

    @Override
    public boolean isNoShuffle() {
        return _noShuffle;
    }

}