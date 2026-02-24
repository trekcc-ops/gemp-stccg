package com.gempukku.stccg.filters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.ComparatorType;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.Characteristic;
import com.gempukku.stccg.common.filterable.SkillName;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.util.*;

public class FilterBlueprintDeserializer extends StdDeserializer<FilterBlueprint> implements FilterBlueprintMethods {

    private final static String OR_WITH_NO_PARENTHESES = "\\s+OR\\s+(?![^\\(]*\\))";
    private final static String AND_WITH_NO_PARENTHESES = "\\s+\\+\\s+(?![^\\(]*\\))";

    private final Map<String, FilterBlueprint> simpleFilters = new HashMap<>();

    public FilterBlueprintDeserializer() throws InvalidCardDefinitionException {
        this(null);
    }

    public FilterBlueprintDeserializer(Class<?> vc) throws InvalidCardDefinitionException {
        super(vc);
        loadSimpleFilters();
    }

    @Override
    public FilterBlueprint deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode object = jp.getCodec().readTree(jp);
        if (object != null && object.isTextual() && object.textValue() != null) {
            return createFilterBlueprint(object.textValue());
        }
        else throw new InvalidCardDefinitionException("Unable to deserialize filter blueprint");
    }

    private FilterBlueprint createFilterBlueprint(String initialText) throws InvalidCardDefinitionException {
        if (initialText.startsWith("(") && initialText.endsWith(")") &&
                initialText.indexOf("(") == initialText.lastIndexOf("(") &&
                initialText.indexOf(")") == initialText.lastIndexOf(")")) {
            return createFilterBlueprint(initialText.substring(1, initialText.length() - 1));
        }
        if (simpleFilters.get(Sanitize(initialText)) != null) {
            return simpleFilters.get(Sanitize(initialText));
        } else if (isParameterizedFilter(initialText)) {
            return createParameterizedFilter(initialText);
        } else {
            Map<String, List<String>> result = breakOutParentheticals(initialText);
            String newString = Iterables.getOnlyElement(result.keySet());
            List<String> subStrings = Iterables.getOnlyElement(result.values());

            if (newString.split(OR_WITH_NO_PARENTHESES).length > 1) {
                return createOrFilter(newString, subStrings);
            } else if (newString.split(AND_WITH_NO_PARENTHESES).length > 1) {
                return createAndFilter(newString, subStrings);
            }
        }
        throw new InvalidCardDefinitionException("Unable to create filter blueprint from text '" + initialText + "'");
    }

    private boolean isParameterizedFilter(String text) {
        if (!text.contains(" ") && (text.contains("=") || text.contains(">") || text.contains("<"))) {
            // No spaces expected in filter blueprint if it is using comparator symbols
            return true;
        } else if (text.endsWith(")")) {
            // Otherwise, verify that the entirety of the filter blueprint is enclosed in a function-like text pattern
            // Example: yourFunction(something + somethingElse)
            // If it has multiple pieces, it will return false, for example if it is something like:
            //      yourFunction(something) + yourOtherFunction(somethingElse)
            int openParensFound = 0;
            int closingParensFound = 0;

            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) == '(') {
                    openParensFound++;
                } else if (text.charAt(i) == ')') {
                    closingParensFound++;
                    if (openParensFound == closingParensFound && i != text.length() - 1) {
                        return false;
                    }
                } else if (text.charAt(i) == ' ') {
                    if (openParensFound == 0) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private FilterBlueprint createParameterizedFilter(String text) throws InvalidCardDefinitionException {
        String type = "";
        String parameter = "";
        ComparatorType comparatorType = null;
        if (!text.contains(" ") && !text.contains("(")) {
            if (text.contains("=")) {
                String[] stringSplit = text.split("=");
                type = stringSplit[0];
                parameter = stringSplit[1];
                comparatorType = ComparatorType.EQUAL_TO;
            } else if (text.contains("<")) {
                String[] stringSplit = text.split("<");
                type = stringSplit[0];
                parameter = stringSplit[1];
                comparatorType = ComparatorType.LESS_THAN;
            } else if (text.contains(">")) {
                String[] stringSplit = text.split(">");
                type = stringSplit[0];
                parameter = stringSplit[1];
                comparatorType = ComparatorType.GREATER_THAN;
            }
        } else {
            type = text.substring(0, text.indexOf("("));
            parameter = text.substring(text.indexOf("(") + 1, text.length() - 1);
            comparatorType = ComparatorType.EQUAL_TO;
        }

        if (type.isEmpty() || parameter.isEmpty()) {
            throw new InvalidCardDefinitionException("Unable to identify parameters in filter blueprint '" + text + "'");
        }

        return switch(type) {
            case "affiliation" -> {
                Affiliation affiliation = Affiliation.findAffiliation(parameter);
                yield (cardGame, actionContext) -> Filters.changeToFilter(affiliation);
            }
            case "bottomCardsOfYourDiscardPile" -> {
                final String[] filters = splitIntoFilters(parameter);
                int cardCount = Integer.parseInt(filters[0]);
                FilterBlueprint additionalFilter = createFilterBlueprint(filters[1]);
                yield (cardGame, actionContext) -> new BottomCardsOfDiscardFilter(actionContext.yourName(),
                        cardCount, additionalFilter.getFilterable(cardGame, actionContext));
            }
            case "characteristic" -> {
                Characteristic characteristic = Characteristic.findCharacteristic(parameter);
                yield (cardGame, actionContext) -> Filters.changeToFilter(characteristic);
            }
            case "classification" -> {
                SkillName classification = SkillName.valueOf(parameter.toUpperCase());
                yield (cardGame, actionContext) -> new ClassificationFilter(classification);
            }
            case "CUNNING" -> {
                int cunningAmount = Integer.parseInt(parameter);
                ComparatorType comparator = comparatorType;
                yield (cardGame, actionContext) ->
                        new AttributeFilter(CardAttribute.INTEGRITY, comparator, cunningAmount);
            }
            case "facilityEngineerRequirement" -> {
                FilterBlueprint engineerBlueprint = createFilterBlueprint(parameter);
                yield new FacilityEngineerRequirementFilterBlueprint(engineerBlueprint);
            }
            case "highestTotalAttributes" -> {
                FilterBlueprint otherFilterBlueprint = createFilterBlueprint(parameter);
                yield (cardGame, actionContext) -> {
                    CardFilter otherFilter = otherFilterBlueprint.getFilterable(cardGame, actionContext);
                    return new HighestTotalAttributeCardFilter(otherFilter);
                };
            }
            case "highestStrength" -> {
                FilterBlueprint otherFilterBlueprint = createFilterBlueprint(parameter);
                yield (cardGame, actionContext) -> {
                    CardFilter otherFilter = otherFilterBlueprint.getFilterable(cardGame, actionContext);
                    return new HighestStrengthCardFilter(otherFilter);
                };
            }
            case "integrity" -> {
                int integrityAmount = Integer.parseInt(parameter);
                if (comparatorType == ComparatorType.GREATER_THAN) {
                    yield (cardGame, actionContext) -> Filters.integrityGreaterThan(integrityAmount);
                } else {
                    throw new InvalidCardDefinitionException(
                            "Cannot process integrity filter blueprint using comparator type '" + comparatorType + "'");
                }
            }
            case "locationName" -> new LocationNameFilterBlueprint(parameter);
            case "memoryId" -> {
                String memoryId = parameter;
                yield (cardGame, actionContext) -> {
                    Collection<Integer> memoryCardIds = actionContext.getCardIdsFromMemory(memoryId);
                    return new InCardListFilter(memoryCardIds.stream().toList());
                };
            }
            case "name" -> new CardTitleFilterBlueprint(parameter);
            case "not" -> {
                FilterBlueprint filterBlueprint = createFilterBlueprint(parameter);
                yield (cardGame, actionContext) -> Filters.not(filterBlueprint.getFilterable(cardGame, actionContext));
            }
            case "sd-icons" -> {
                int iconCount = Integer.parseInt(parameter);
                ComparatorType comparatorToUse = comparatorType;
                yield (cardGame, actionContext) -> new SpecialDownloadIconCountFilter(iconCount, comparatorToUse);
            }
            case "skill-dots" -> {
                int skillDotCount = Integer.parseInt(parameter);
                ComparatorType comparatorToUse = comparatorType;
                yield (cardGame, actionContext) -> new SkillDotFilter(skillDotCount, comparatorToUse);
            }
            case "textInLore" -> {
                String loreText = parameter;
                yield (cardGame, actionContext) ->
                        (CardFilter) (game, physicalCard) -> physicalCard.getLore().contains(loreText);
            }
            case "title" -> new CardTitleFilterBlueprint(parameter);
            default -> throw new InvalidCardDefinitionException(
                    "Unable to parse parameterized filter blueprint with type '" + type + "' and parameter '" + parameter + "'");
        };
    }

    private Map<String, List<String>> breakOutParentheticals(String fullString) {
        Integer openParenIndex = null;
        Integer closingParenIndex = null;
        int openParensFound = 0;
        int closingParensFound = 0;
        List<String> parentheticals = new ArrayList<>();

        String newString = fullString;
        String initialString;

        do {
            initialString = newString;

            for (int i = 0; i < initialString.length(); i++) {
                if (initialString.charAt(i) == '(') {
                    openParensFound++;
                    if (openParenIndex == null &&
                            (i == 0 || initialString.charAt(i - 1) == ' ')
                    ) {
                        openParenIndex = i;
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
        } while (!newString.equals(initialString));

        Map<String, List<String>> result = new HashMap<>();
        result.put(newString, parentheticals);
        return result;
    }
    
    private FilterBlueprint createOrFilter(String text, List<String> substitutions)
            throws InvalidCardDefinitionException {
        Collection<FilterBlueprint> filters = new ArrayList<>();
        for (String filter : text.split(OR_WITH_NO_PARENTHESES)) {
            String textToUse = filter;
            if (filter.startsWith("{") && filter.endsWith("}")) {
                int indexNum = Integer.parseInt(filter.replace("{", "").replace("}", ""));
                textToUse = substitutions.get(indexNum);
            }
            filters.add(createFilterBlueprint(textToUse));
        }
        return new OrFilterBlueprint(filters);
    }

    private FilterBlueprint createAndFilter(String text, List<String> substitutions) throws InvalidCardDefinitionException {
        Collection<FilterBlueprint> filters = new ArrayList<>();
        for (String filter : text.split(AND_WITH_NO_PARENTHESES)) {
            String textToUse = filter;
            if (filter.startsWith("{") && filter.endsWith("}")) {
                int indexNum = Integer.parseInt(filter.replace("{", "").replace("}", ""));
                textToUse = substitutions.get(indexNum);
            }
            filters.add(createFilterBlueprint(textToUse));
        }
        return new AndFilterBlueprint(filters);
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


    @Override
    public Map<String, FilterBlueprint> getSimpleFilters() {
        return simpleFilters;
    }
}