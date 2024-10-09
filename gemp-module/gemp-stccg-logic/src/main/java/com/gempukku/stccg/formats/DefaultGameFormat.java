package com.gempukku.stccg.formats;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.GameFormat;
import com.gempukku.stccg.common.JSONDefs;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.SubDeck;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultGameFormat implements GameFormat {

    private final CardBlueprintLibrary _library;
    private final String _name;
    private final String _game;
    private final String _code;
    private final int _order;
    private final boolean _hallVisible;
    private final boolean _validateShadowFPCount;
    private final int _maximumSameName;
    private final boolean _mulliganRule;
    private final boolean _canCancelRingBearerSkirmish;
    private final boolean _hasRuleOfFour;
    private final boolean _winAtEndOfRegroup;
    private final boolean _discardPileIsPublic;
    private final boolean _winOnControlling5Sites;
    private final int _minimumDrawDeckSize;
    private final int _maximumSeedDeckSize;
    private final int _missions;
    private final List<String> _bannedCards = new ArrayList<>();
    private final List<String> _restrictedCards = new ArrayList<>();
    private final List<String> _validCards = new ArrayList<>();
    private final List<Integer> _validSets = new ArrayList<>();
    private final List<String> _restrictedCardNames = new ArrayList<>();
    private final String _surveyUrl;
    private final boolean _isPlaytest;

    //Additional Hobbit Draft parameters
    private final List<String> _limit2Cards = new ArrayList<>();
    private final List<String> _limit3Cards = new ArrayList<>();
    private final Map<String,String> _errataCardMap = new TreeMap<>();

    public DefaultGameFormat(CardBlueprintLibrary library, JSONDefs.Format def) throws InvalidPropertiesFormatException{
        this(library, def.name, def.game, def.code, def.order, def.surveyUrl,
                def.validateShadowFPCount, def.minimumDrawDeckSize, def.maximumSeedDeckSize, def.missions, def.maximumSameName, def.mulliganRule, def.cancelRingBearerSkirmish,
                def.ruleOfFour, def.winAtEndOfRegroup, def.discardPileIsPublic, def.winOnControlling5Sites, def.playtest, def.hall);

        if(def.set != null)
            def.set.forEach(this::addValidSet);
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

    public DefaultGameFormat(CardBlueprintLibrary library,
                             String name, String game, String code, int order, String surveyUrl,
                             boolean validateShadowFPCount, int minimumDrawDeckSize, int maximumSeedDeckSize, int missions, int maximumSameName, boolean mulliganRule,
                             boolean canCancelRingBearerSkirmish, boolean hasRuleOfFour, boolean winAtEndOfRegroup, boolean discardPileIsPublic,
                             boolean winOnControlling5Sites, boolean isPlayTest, boolean hallVisible) {
        _library = library;
        _name = name;
        _game = game;
        _code = code;
        _order = order;
        _surveyUrl = surveyUrl;
        _validateShadowFPCount = validateShadowFPCount;
        _minimumDrawDeckSize = minimumDrawDeckSize;
        _maximumSameName = maximumSameName;
        _mulliganRule = mulliganRule;
        _canCancelRingBearerSkirmish = canCancelRingBearerSkirmish;
        _hasRuleOfFour = hasRuleOfFour;
        _winAtEndOfRegroup = winAtEndOfRegroup;
        _discardPileIsPublic = discardPileIsPublic;
        _winOnControlling5Sites = winOnControlling5Sites;
        _isPlaytest = isPlayTest;
        _hallVisible = hallVisible;
        _missions = missions;
        _maximumSeedDeckSize = maximumSeedDeckSize;
    }

    @Override
    public String getName() {
        return _name;
    }
    public String getGameType() { return _game; }
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

    @Override
    public List<Integer> getValidSetNums() {
        return Collections.unmodifiableList(_validSets);
    }

    @Override
    public Map<String, String> getValidSets() {
            // For sending to CardFilter
        Map<String, String> sets = new LinkedHashMap<>();
        String allSetsString = _validSets.stream().map(String::valueOf).collect(Collectors.joining(","));
        sets.put(allSetsString, "All " + _name + " sets");
        sets.put("disabled", "disabled");
        Map<String, SetDefinition> librarySets = _library.getSetDefinitions();
        for (Integer setNum : _validSets) {
            sets.put(setNum.toString(), librarySets.get(setNum.toString()).getSetName());
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

    public void addValidSet(int setNo) {
        _validSets.add(setNo);
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

    public void addErrataSet(int setID) throws InvalidPropertiesFormatException {
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
        for (int validSet : _validSets)
            if (blueprintId.startsWith(validSet + "_")
                    || _library.hasAlternateInSet(blueprintId, validSet))
                return true;
        return false;
    }

    @Override
    public String validateDeckForHall(CardDeck deck) {
        List<String> validations = validateDeck(deck);
        if(validations.isEmpty())
            return "";

        String firstValidation = validations.stream().findFirst().orElse(null);
        long count = firstValidation.chars().filter(x -> x == '\n').count();
        if(firstValidation.contains("\n"))
        {
            firstValidation = firstValidation.substring(0, firstValidation.indexOf("\n"));
        }

        return "Deck targets '" + deck.getTargetFormat() + "' format and is incompatible with '" + _name + "'.  Issues include: `"
                + firstValidation + "` and " + (validations.size() - 1 + count - 1) + " other issues.";
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
        for (String card : deck.getDrawDeckCards()){
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

        for (String blueprintId : deck.getDrawDeckCards())
            try {
                processCardCounts(blueprintId, cardCountByName, cardCountByBaseBlueprintId);
            } catch(CardNotFoundException exp) {
                result.add("Draw deck contains card of invalid blueprintId '" + blueprintId + "'");
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

    @Override
    public List<String> findBaseCards(String bpID) {
        return _errataCardMap
                .entrySet().stream()
                .filter(x-> x.getValue().equals(bpID))
                .map(Map.Entry::getKey)
                .toList();
    }

    private String validateDeckStructure(CardDeck deck) {
        StringBuilder result = new StringBuilder();
        int drawDeckSize = deck.getSubDeck(SubDeck.DRAW_DECK).size();
        if (drawDeckSize < _minimumDrawDeckSize) {
            result.append("Draw deck contains below minimum number of cards: ")
                    .append(drawDeckSize).append("<").append(_minimumDrawDeckSize).append(".\n");
        }
        if (Objects.equals(_game, "st1e")) {
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
        if (missionsPile.size() != _missions) {
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

    private void increaseCount(Map<String, Integer> counts, String name) {
        counts.merge(name, 1, Integer::sum);
    }

    @Override
    public JSONDefs.Format Serialize() {
        return new JSONDefs.Format() {{
            code = _code;
            game = _game;
            name = _name;
            order = _order;
            surveyUrl = _surveyUrl;
            cancelRingBearerSkirmish = _canCancelRingBearerSkirmish;
            ruleOfFour = _hasRuleOfFour;
            winAtEndOfRegroup = _winAtEndOfRegroup;
            discardPileIsPublic = _discardPileIsPublic;
            winOnControlling5Sites = _winOnControlling5Sites;
            playtest = _isPlaytest;
            validateShadowFPCount = _validateShadowFPCount;
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
        }};
    }

}
