package com.gempukku.stccg;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.ComparatorType;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.*;
import com.gempukku.stccg.game.DefaultGame;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.util.*;

public class FilterBlueprintDeserializationTest {
//    private final static String OR_WITH_NO_PARENTHESES = "\\s+OR\\s+(?![^\\(]*\\))";
//    private final static String AND_WITH_NO_PARENTHESES = "\\s+\\+\\s+(?![^\\(]*\\))";
    private final static String OR_WITH_NO_PARENTHESES = "\\sOR\\s";
    private final static String AND_WITH_NO_PARENTHESES = "\\s\\+\\s";

    private final Map<String, FilterBlueprint> simpleFilters;
    private final Map<String, FilterableSourceProducer> parameterFilters = new HashMap<>();
    private final ObjectMapper _mapper = new ObjectMapper();

    public FilterBlueprintDeserializationTest() throws InvalidCardDefinitionException {
        FilterBlueprintDeserializer currentDeserializer = new FilterBlueprintDeserializer();
        simpleFilters = currentDeserializer.getSimpleFilters();
        loadParameterFilters();
    }

//    @Test
    public void deserializeTest() throws Exception {
//        FilterBlueprint blueprint = parseSTCCGFilter("personnel + (Empathy OR Diplomacy OR name=Morn OR characteristic=Scotty) + presentWithThisCard");
        String sampleString = "personnel + (Empathy OR Diplomacy OR name=Morn OR characteristic(Scotty)) + name(Jean-Luc Picard) + presentWithThisCard";
//        String sampleString = "personnel + (Empathy OR (Diplomacy + SCIENCE)) + other stuff";
        FilterBlueprint blueprint = createFilterBlueprint(sampleString);
    }

    private FilterBlueprint createFilterBlueprint(String initialText) throws InvalidCardDefinitionException {
        Map<String, List<String>> result = breakOutParentheticals(initialText);
        String newString = Iterables.getOnlyElement(result.keySet());
        List<String> subStrings = Iterables.getOnlyElement(result.values());
        if (newString.split(OR_WITH_NO_PARENTHESES).length > 1) {
            Collection<FilterBlueprint> filters = new ArrayList<>();
            for (String filter : newString.split(OR_WITH_NO_PARENTHESES)) {
                String textToUse = filter;
                if (filter.startsWith("{") && filter.endsWith("}")) {
                    int indexNum = Integer.parseInt(filter.replace("{", "").replace("}",""));
                    textToUse = subStrings.get(indexNum);
                }
                filters.add(createFilterBlueprint(textToUse));
            }
            return new OrFilterBlueprint(filters);
        } else if (newString.split(AND_WITH_NO_PARENTHESES).length > 1) {
            Collection<FilterBlueprint> filters = new ArrayList<>();
            for (String filter : newString.split(AND_WITH_NO_PARENTHESES)) {
                String textToUse = filter;
                if (filter.startsWith("{") && filter.endsWith("}")) {
                    int indexNum = Integer.parseInt(filter.replace("{", "").replace("}",""));
                    textToUse = subStrings.get(indexNum);
                }
                filters.add(createFilterBlueprint(textToUse));
            }
            return new AndFilterBlueprint(filters);
        } else if (isParameterizedFilter(newString)) {
//            return createParameterizedFilter();
            return null;
        }
        return null;
    }

    private boolean isParameterizedFilter(String text) {
        return (text.indexOf("(") > text.indexOf(" ") && text.endsWith(")")) ||
                (!text.contains(" ") && text.contains("="));
    }

    private Map<String, List<String>> breakOutParentheticals(String fullString) {
        Integer openParenIndex = null;
        Integer closingParenIndex = null;
        int openParensFound = 0;
        int closingParensFound = 0;
        List<String> parentheticals = new ArrayList<>();

        String newString = fullString;
        String initialString;
        int loopsCompleted = 0;

        do {
            initialString = newString;

            for (int i = 0; i < initialString.length(); i++) {
                if (initialString.charAt(i) == '(') {
                    openParensFound++;
                    if (openParenIndex == null &&
                            (i == 0 || initialString.charAt(i - 1) == ' ')
                    ) {
                        openParenIndex = i;
                    } else if (openParenIndex == null) {
                        String truncString = initialString.substring(0, i);
                        openParenIndex = Math.max(truncString.lastIndexOf(' ')+1, 0);
                    }
                } else if (initialString.charAt(i) == ')') {
                    closingParensFound++;
                    if (openParensFound == closingParensFound) {
                        closingParenIndex = i;
                        break;
                    }
                }
            }
            if (openParenIndex != null && closingParenIndex != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(initialString.substring(0, openParenIndex));
                sb.append("{").append(parentheticals.size()).append("}");
                String parentheticalToAdd = initialString.substring(openParenIndex, closingParenIndex+1);
                if (parentheticalToAdd.startsWith("(") && parentheticalToAdd.endsWith(")")) {
                    parentheticalToAdd = parentheticalToAdd.substring(1, parentheticalToAdd.length() - 1);
                }
                parentheticals.add(parentheticalToAdd);
                sb.append(initialString.substring(closingParenIndex + 1));
                newString = sb.toString();
            }
            openParenIndex = null;
            closingParenIndex = null;
            openParensFound = 0;
            closingParensFound = 0;
            loopsCompleted++;
        } while (!newString.equals(initialString));

        Map<String, List<String>> result = new HashMap<>();
        result.put(newString, parentheticals);
        return result;
    }

    private void loadParameterFilters() {
        parameterFilters.put("and",
                (parameter) -> {
                    final String[] filters = splitIntoFilters(parameter);
                    FilterBlueprint[] filterables = new FilterBlueprint[filters.length];
                    for (int i = 0; i < filters.length; i++)
                        filterables[i] = generateFilter(filters[i]);
                    return new AndFilterBlueprint(filterables);
                });
        parameterFilters.put("affiliation", (parameter) -> {
            final Affiliation affiliation = Affiliation.findAffiliation(parameter);
            if (affiliation == null)
                throw new InvalidCardDefinitionException("Unable to find affiliation for: " + parameter);
            return (cardGame, actionContext) -> new AffiliationFilter(affiliation);
        });
        parameterFilters.put("bottomcardsofyourdiscardpile", (parameter) -> {
            final String[] filters = splitIntoFilters(parameter);
            int cardCount = Integer.parseInt(filters[0]);
            FilterBlueprint additionalFilter = parseSTCCGFilter(filters[1]);
            return (cardGame, actionContext) -> new BottomCardsOfDiscardFilter(actionContext.yourName(),
                    cardCount, additionalFilter.getFilterable(cardGame, actionContext));
        });
        parameterFilters.put("memory",
                (parameter) -> new FilterBlueprint() {
                    @Override
                    public CardFilter getFilterable(DefaultGame cardGame, GameTextContext actionContext) {
                        Collection<Integer> cardIds = actionContext.getCardIdsFromMemory(parameter);
                        List<Integer> cardIdList = cardIds.stream().toList();
                        return new InCardListFilter(cardIdList);
                    }
                });
        parameterFilters.put("name",
                (parameter) -> {
                    String name = Sanitize(parameter);
                    return (cardGame, actionContext) -> new TitleFilter(name);
                });
        parameterFilters.put("not",
                (parameter) -> {
                    final FilterBlueprint filterBlueprint = generateFilter(parameter);
                    return (cardGame, actionContext) -> Filters.not(filterBlueprint.getFilterable(cardGame, actionContext));
                });
        parameterFilters.put("or",
                (parameter) -> {
                    final String[] filters = splitIntoFilters(parameter);
                    FilterBlueprint[] filterables = new FilterBlueprint[filters.length];
                    for (int i = 0; i < filters.length; i++)
                        filterables[i] = generateFilter(filters[i]);
                    return new FilterBlueprint() {
                        @Override
                        public CardFilter getFilterable(DefaultGame cardGame, GameTextContext actionContext) {
                            Filterable[] filters1 = new Filterable[filterables.length];
                            for (int i = 0; i < filterables.length; i++)
                                filters1[i] = filterables[i].getFilterable(cardGame, actionContext);

                            return Filters.or(filters1);
                        }
                    };
                });
        parameterFilters.put("title",parameterFilters.get("name"));
        parameterFilters.put("zone",
                (parameter) -> {
                    final Zone zone = _mapper.readValue(parameter, Zone.class);
                    return (cardGame, actionContext) -> Filters.changeToFilter(zone);
                });

    }

    public FilterBlueprint deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode object = jp.getCodec().readTree(jp);
        if (object != null && object.isTextual() && object.textValue() != null) {
            try {
                return parseSTCCGFilter(object.textValue());
            } catch(InvalidCardDefinitionException exp) {
                return generateFilter(object.textValue());
            }
        }
        else throw new InvalidCardDefinitionException("Unable to deserialize filter blueprint");
    }

    private FilterBlueprint parseSTCCGFilter(String value) throws InvalidCardDefinitionException {
        System.out.println(value); // Very useful for debugging
        if (simpleFilters.get(Sanitize(value)) != null) {
            return simpleFilters.get(Sanitize(value));
        }
        if (value.startsWith("bottomCardsOfYourDiscardPile(") && value.endsWith(")")) {
            String remainingText = value.substring("bottomCardsOfYourDiscardPile(".length(), value.length() - 1);
            final String[] filters = splitIntoFilters(remainingText);
            int cardCount = Integer.parseInt(filters[0]);
            FilterBlueprint additionalFilter = parseSTCCGFilter(filters[1]);
            return (cardGame, actionContext) -> new BottomCardsOfDiscardFilter(actionContext.yourName(),
                    cardCount, additionalFilter.getFilterable(cardGame, actionContext));
        }
        if (value.split(OR_WITH_NO_PARENTHESES).length > 1)
            return createOrFilter(value);
        if (value.split(AND_WITH_NO_PARENTHESES).length > 1)
            return createAndFilter(value);
        if (value.startsWith("(") && value.endsWith(")")) {
            return parseSTCCGFilter(value.substring(1, value.length() - 1));
        }
        if (value.startsWith("locationName(") && value.endsWith(")")) {
            String locationName = value.substring("locationName(".length(), value.length() - 1);
            return new LocationNameFilterBlueprint(locationName);
        }
        if (value.startsWith("not(") && value.endsWith(")")) {
            FilterBlueprint filterBlueprint = parseSTCCGFilter(value.substring(4, value.length() - 1));
            return (cardGame, actionContext) -> Filters.not(filterBlueprint.getFilterable(cardGame, actionContext));
        }
        if (value.startsWith("name(") && value.endsWith(")")) {
            String cardTitle = value.substring(5, value.length() - 1);
            return new CardTitleFilterBlueprint(cardTitle);
        }
        if (value.startsWith("memoryId=")) {
            String memoryId = value.substring("memoryId=".length());
            return (cardGame, actionContext) -> {
                Collection<Integer> memoryCardIds = actionContext.getCardIdsFromMemory(memoryId);
                return new InCardListFilter(memoryCardIds.stream().toList());
            };
        }
        if (value.startsWith("title(") && value.endsWith(")")) {
            String cardTitle = value.substring(6, value.length() - 1);
            return new CardTitleFilterBlueprint(cardTitle);
        }
        if (value.startsWith("facilityEngineerRequirement(") && value.endsWith(")")) {
            String engineerFilter = value.substring("facilityEngineerRequirement(".length(), value.length() - 1);
            FilterBlueprint engineerBlueprint = parseSTCCGFilter(engineerFilter);
            return new FacilityEngineerRequirementFilterBlueprint(engineerBlueprint);
        }
        if (value.startsWith("highestTotalAttributes(") && value.endsWith(")")) {
            String otherFilterText = value.substring("highestTotalAttributes(".length(), value.length() - 1);
            FilterBlueprint otherFilterBlueprint = parseSTCCGFilter(otherFilterText);
            return (cardGame, actionContext) -> {
                CardFilter otherFilter = otherFilterBlueprint.getFilterable(cardGame, actionContext);
                return new HighestTotalAttributeCardFilter(otherFilter);
            };
        }
        if (value.startsWith("integrity>")) {
            int integrityAmount = Integer.valueOf(value.substring("integrity>".length()));
            return (cardGame, actionContext) -> Filters.integrityGreaterThan(integrityAmount);
        }
        if (value.startsWith("affiliation=")) {
            String affiliationName = value.substring(12);
            Affiliation affiliation = Affiliation.findAffiliation(affiliationName);
            return (cardGame, actionContext) -> Filters.and(affiliation);
        }
        if (value.startsWith("characteristic=")) {
            String characteristicName = value.substring("characteristic=".length());
            Characteristic characteristic = Characteristic.findCharacteristic(characteristicName);
            return (cardGame, actionContext) -> Filters.and(characteristic);
        }
        if (value.startsWith("classification=")) {
            String skillName = value.substring("classification=".length());
            SkillName skill = SkillName.valueOf(skillName.toUpperCase(Locale.ROOT));
            return (cardGame, actionContext) -> new ClassificationFilter(skill);
        }
        if (value.startsWith("skill-dots<=")) {
            String[] stringSplit = value.split("<=");
            int skillDotCount = Integer.parseInt(stringSplit[1]);
            return (cardGame, actionContext) ->
                    new SkillDotFilter(skillDotCount, ComparatorType.LESS_THAN_OR_EQUAL_TO);
        }
        if (value.startsWith("sd-icons=")) {
            String[] stringSplit = value.split("=");
            int iconCount = Integer.parseInt(stringSplit[1]);
            return (cardGame, actionContext) -> new SpecialDownloadIconCountFilter(iconCount, ComparatorType.EQUAL_TO);
        }
        FilterBlueprint result = simpleFilters.get(Sanitize(value));
        if (result == null)
            throw new InvalidCardDefinitionException("Unknown filter: " + value);
        else return result;
    }

    private FilterBlueprint createOrFilter(String value) throws InvalidCardDefinitionException {
        String[] stringSplit = value.split(OR_WITH_NO_PARENTHESES);
        List<FilterBlueprint> filterBlueprints = new LinkedList<>();
        for (String string : stringSplit)
            filterBlueprints.add(parseSTCCGFilter(string));
        return new OrFilterBlueprint(filterBlueprints);
    }

    private FilterBlueprint createAndFilter(String value) throws InvalidCardDefinitionException {
        String[] stringSplit = value.split(AND_WITH_NO_PARENTHESES);
        List<FilterBlueprint> filterBlueprints = new LinkedList<>();
        for (String string : stringSplit)
            filterBlueprints.add(parseSTCCGFilter(string));
        return (cardGame, actionContext) -> {
            List<Filterable> filterables = new LinkedList<>();
            for (FilterBlueprint filterBlueprint : filterBlueprints)
                filterables.add(filterBlueprint.getFilterable(cardGame, actionContext));
            return Filters.and(filterables.toArray(new Filterable[0]));
        };
    }

    private static String Sanitize(String input)
    {
        return input
                .toLowerCase()
                .replace(" ", "")
                .replace("_", "");
    }

    private FilterBlueprint generateFilter(String value) throws
            InvalidCardDefinitionException {
        try {
            if (value == null)
                throw new InvalidCardDefinitionException("Filter not specified");
            String[] filterStrings = splitIntoFilters(value);
            if (filterStrings.length == 0)
                return (cardGame, actionContext) -> Filters.any;
            if (filterStrings.length == 1)
                return createFilter(filterStrings[0]);

            FilterBlueprint[] filters = new FilterBlueprint[filterStrings.length];
            for (int i = 0; i < filters.length; i++)
                filters[i] = createFilter(filterStrings[i]);
            return (cardGame, actionContext) -> {
                Filterable[] filter = new Filterable[filters.length];
                for (int i = 0; i < filter.length; i++) {
                    filter[i] = filters[i].getFilterable(cardGame, actionContext);
                }

                return Filters.and(filter);
            };
        } catch(JsonProcessingException exp) {
            throw new InvalidCardDefinitionException(exp.getMessage());
        }
    }

    private String[] splitIntoFilters(String value) throws InvalidCardDefinitionException {
        List<String> parts = new LinkedList<>();
        final char[] chars = value.toCharArray();

        int depth = 0;
        StringBuilder sb = new StringBuilder();
        for (char ch : chars) {
            if (depth > 0) {
                if (ch == ')')
                    depth--;
                if (ch == '(')
                    depth++;
                sb.append(ch);
            } else {
                if (ch == ',') {
                    parts.add(sb.toString());
                    sb = new StringBuilder();
                } else {
                    if (ch == ')')
                        throw new InvalidCardDefinitionException("Invalid filter definition: " + value);
                    if (ch == '(')
                        depth++;
                    sb.append(ch);
                }
            }
        }

        if (depth != 0)
            throw new InvalidCardDefinitionException("Not matching number of opening and closing brackets: " + value);

        parts.add(sb.toString());

        return parts.toArray(new String[0]);
    }

    private FilterBlueprint createFilter(String filterString) throws InvalidCardDefinitionException, JsonProcessingException {
        if (filterString.contains("(") && filterString.endsWith(")")) {
            String filterName = filterString.substring(0, filterString.indexOf("("));
            String filterParameter =
                    filterString.substring(filterString.indexOf("(") + 1, filterString.lastIndexOf(")"));
            return lookupFilter(Sanitize(filterName), Sanitize(filterParameter));
        }
        return lookupFilter(Sanitize(filterString), null);
    }



    private FilterBlueprint lookupFilter(String name, String parameter)
            throws InvalidCardDefinitionException, JsonProcessingException {
        if (parameter == null) {
            FilterBlueprint result = simpleFilters.get(Sanitize(name));
            if (result != null)
                return result;
        }

        final FilterableSourceProducer filterableSourceProducer = parameterFilters.get(Sanitize(name));
        if (filterableSourceProducer == null)
            throw new InvalidCardDefinitionException("Unable to find filter: " + name);

        return filterableSourceProducer.createFilterableSource(parameter);
    }


}